package com.kaushalpanjee.common

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.util.Base64
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.kaushalpanjee.BuildConfig
import com.kaushalpanjee.R
import com.kaushalpanjee.common.model.UidaiKycRequest
import com.kaushalpanjee.common.model.UidaiResp
import com.kaushalpanjee.common.model.WrappedList
import com.kaushalpanjee.common.model.request.AadhaarCheckReq
import com.kaushalpanjee.common.model.request.GetLoginIdNdPassReq
import com.kaushalpanjee.common.model.request.UserCreationReq
import com.kaushalpanjee.common.model.response.IntentModel
import com.kaushalpanjee.common.model.response.IntentResponse
import com.kaushalpanjee.core.basecomponent.BaseFragment
import com.kaushalpanjee.core.util.AESCryptography
import com.kaushalpanjee.core.util.AppConstant
import com.kaushalpanjee.core.util.AppConstant.Constants.LANGUAGE
import com.kaushalpanjee.core.util.AppConstant.Constants.PRODUCTION
import com.kaushalpanjee.core.util.AppUtil
import com.kaushalpanjee.core.util.DownloadHelper
import com.kaushalpanjee.core.util.Resource
import com.kaushalpanjee.core.util.copyToClipboard
import com.kaushalpanjee.core.util.decodeBase64
import com.kaushalpanjee.core.util.gone
import com.kaushalpanjee.core.util.log
import com.kaushalpanjee.core.util.onRightDrawableClicked
import com.kaushalpanjee.core.util.setRightDrawablePassword
import com.kaushalpanjee.core.util.setUnderline
import com.kaushalpanjee.core.util.toastLong
import com.kaushalpanjee.core.util.toastShort
import com.kaushalpanjee.core.util.visible
import com.kaushalpanjee.databinding.FragmentEkyBinding
import com.kaushalpanjee.model.kyc_resp_pojo.XstreamCommonMethods
import com.kaushalpanjee.model.kyc_resp_pojo.XstreamCommonMethods.respDecodedXmlToPojoAuth
import com.kaushalpanjee.security.SecurityUtils
import com.kaushalpanjee.uidai.capture.CaptureResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.Locale

const val CAMERA_REQUEST = 101

@AndroidEntryPoint
class EKYCFragment : BaseFragment<FragmentEkyBinding>(FragmentEkyBinding::inflate) {
    private val commonViewModel: CommonViewModel by viewModels()

    private var stateList: MutableList<WrappedList> = mutableListOf()

    private var selectedState = ""
    private var showPassword = true
    private var aadhaarValidate = false
    private var name = ""
    private var dob = ""
    private var gender = ""
    private var careOf = ""
    private var state = ""
    private var dist = ""
    private var block = ""
    private var po = ""
    private var pinCode = ""
    private var street = ""
    private var isSpeaking = false
    private var tokenGen = ""
    private var tokenViaCreate = ""
    private var village = ""
    private var photo = ""
    private var selectedStateCode = ""
    private var selectedStateLgdCode = ""
    private lateinit var tts: TextToSpeech

    private lateinit var layoutManager : LinearLayoutManager


    private val stateAdaptor by lazy {
        StateAdaptor(object : StateAdaptor.ItemClickListener {
            override fun onItemClick(position: Int) {
                selectedState = stateList[position].stateName
                binding.tvWelcome.text = getString(R.string.slected_state)
                binding.tvWelcomeMsg.text = selectedState
                binding.tvWelcomeMsg.setUnderline(selectedState)
                selectedStateCode = stateList[position].stateCode
                selectedStateLgdCode = stateList[position].lgdStateCode
                binding.progressButton.root.visible()
                binding.tvWelcomeMsg.visible()
                layoutManager.scrollToPosition(0)
            }
        })
    }

    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private lateinit var progressBar: ProgressBar
    private lateinit var downloadHelper: DownloadHelper

    private var isStateSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        WindowInsetsControllerCompat(
            requireActivity().window,
            requireActivity().window.decorView
        ).isAppearanceLightStatusBars = false


    }

    override fun onDestroy() {
        super.onDestroy()
        if (::tts.isInitialized) {
            tts.stop()
            binding.consentImage.setImageResource(R.drawable.speaker)
            tts.shutdown()
        }
        super.onDestroyView()

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressButton.root.gone()
        init()


    }

    private fun init() {

        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    println("Language not supported")
                }
            }
        }

        listener()
        commonViewModel.getToken(AppUtil.getAndroidId(requireContext()), BuildConfig.VERSION_NAME)
        collectTokenResponse()
        setUI()
        collectStateResponse()
        collectAadharResponse()
        collectUserCreationResponse()
        addTextWatchers()
        commonViewModel.getStateListApi()
        initEKYC()

        //  startAppDownload()


        onBackPressedCallback = object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {

                if (isStateSelected) {
                    binding.progressButton.root.gone()
                    binding.tvWelcomeMsg.visible()
                    binding.recyclerView.visible()
                    binding.etAadhaar.gone()
                    binding.aadhaarVerifyButton.root.gone()
                    isStateSelected = false
                } else if (findNavController().currentBackStackEntry != null) {

                    findNavController().navigateUp()

                } else {
                    requireActivity().onBackPressed()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )


    }

    private fun setUI() {

        layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = stateAdaptor

        binding.etAadhaar.setRightDrawablePassword(
            false,
            ContextCompat.getDrawable(requireContext(), R.drawable.icon_aadhaar), null,
            ContextCompat.getDrawable(requireContext(), R.drawable.close_eye), null
        )
    }



    private fun listener() {


        binding.btnConscentAudio.setOnClickListener {
            if (!isSpeaking) {
                val text = getString(R.string.consent)
                if (text.isNotEmpty()) {
                    speakOut(text)

                    binding.consentImage.setImageResource(R.drawable.pause)

                    isSpeaking = true
                }
            } else {
                tts.stop()
                binding.consentImage.setImageResource(R.drawable.speaker)

                isSpeaking = false
            }
        }


        if (!isSpeaking) {
            val text = getString(R.string.consent)
            if (text.isNotEmpty()) {
                speakOut(text)

                isSpeaking = true
            }
        } else {
            tts.stop()
            isSpeaking = false
        }





        formatCheckBoxText(binding.chipAware)

        binding.aadhaarVerifyButton.centerButton.setOnClickListener {

            if (aadhaarValidate) {


                val encryptedAadhaarString =   AESCryptography.encryptIntoBase64String(binding.etAadhaar.text.toString(),
                    AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)

                if (selectedStateCode!=""){


                    commonViewModel.getAadhaarCheck(AadhaarCheckReq(BuildConfig.VERSION_NAME,encryptedAadhaarString))


                }
                else
                    showSnackBar("Please Select State first")




            } else {

                showSnackBar("Please enter valid aadhaar number")
            }

        }
        binding.chipAware.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show the button when the chip is checked
                binding.aadhaarVerifyButton.root.visibility = View.VISIBLE
            } else {
                // Hide the button when the chip is unchecked
                binding.aadhaarVerifyButton.root.visibility = View.GONE
            }
        }

        binding.etAadhaar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val aadhaar = s.toString()
                if (aadhaar.length == 12) {
                    if (AadhaarValidator.isValidAadhaar(aadhaar)) {
                        binding.etAadhaar.error = null
                        aadhaarValidate=true

                    } else {
                        binding.etAadhaar.error = "❌ Invalid Aadhaar Number"  // ❌ Show error
                        aadhaarValidate=false
                        binding.chipAware.gone()
                        binding.btnConscentAudio.gone()

                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        )

        binding.progressButton.centerButton.setOnClickListener {

            showStateDialog()

        }

        binding.tvWelcomeMsg.setOnClickListener {
            binding.recyclerView.visible()
            binding.chipAware.gone()
            binding.btnConscentAudio.gone()
            binding.tvWelcomeMsg.text = selectedState
            binding.etAadhaar.gone()
            binding.progressButton.root.visible()
            binding.etAadhaar.gone()
            binding.aadhaarVerifyButton.root.gone()
        }

        binding.etAadhaar.onRightDrawableClicked {

            log("onRightDrawableClicked", "onRightDrawableClicked")

            if (showPassword) {
                showPassword = false
                binding.etAadhaar.setRightDrawablePassword(
                    true,
                    ContextCompat.getDrawable(requireContext(), R.drawable.icon_aadhaar), null,
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_open_eye), null
                )
            } else {
                showPassword = true

                binding.etAadhaar.setRightDrawablePassword(
                    false,
                    ContextCompat.getDrawable(requireContext(), R.drawable.icon_aadhaar), null,
                    ContextCompat.getDrawable(requireContext(), R.drawable.close_eye), null
                )

            }

        }


    }





    private fun speakOut(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }


    private fun collectStateResponse() {
        lifecycleScope.launch {
            collectLatestLifecycleFlow(commonViewModel.getStateList) {
                when (it) {
                    is Resource.Loading -> {
                        showProgressBar()
                    }

                    is Resource.Error -> {
                        hideProgressBar()
                        it.error?.let { baseErrorResponse ->
                            showSnackBar(baseErrorResponse.message)
                        }
                    }

                    is Resource.Success -> {
                        hideProgressBar()
                        it.data?.let { getStateResponse ->
                            if (getStateResponse.responseCode == 200) {
                                stateList = getStateResponse.stateList


                                stateAdaptor.setData(stateList)
                            } else if (getStateResponse.responseCode == 301) {

                                showSnackBar("Please Update from PlayStore")
                            } else showSnackBar("Something went wrong")

                        } ?: showSnackBar("Internal Sever Error")
                    }
                }
            }
        }
    }

    private fun addTextWatchers() {

        binding.etAadhaar.doOnTextChanged { text, _, _, _ ->

            if (text?.length == 12) {
                binding.chipAware.visible()
                binding.btnConscentAudio.visible()

              //  binding.aadhaarVerifyButton.root.visible()
            } else{
                binding.btnConscentAudio.gone()
                binding.chipAware.gone()
            }



        }

    }

    private var intentResponse: IntentResponse? = null
    private val neededPermissions = arrayOf(Manifest.permission.CAMERA)
    private var startTime: Long = 0
    private var userPhotoUIADI: Bitmap? = null
    private var ekycImage: String = ""

    private fun initEKYC() {


        intentResponse = IntentResponse(
            kycStatus = false,
            faceAuthStatus = false,
            partialKycStatus = false,
            uidaiStatusCode = "",
            txnId = "",
            kycTimeStamp = "",
            faceAuthTimeStamp = "",
            partialKycTimeStamp = "",
            similarity = 0.0,
            BuildConfig.VERSION_NAME
        )
        var request = requireArguments().getString("pmayg_request")

        if (request == null) {
            request = requireActivity().intent.getStringExtra("request")
        }

        val intentModel = Gson().fromJson(
            request?.let {
                AESCryptography.decryptIntoString(
                    it,
                    SecurityUtils.getCryptId(),SecurityUtils.getCryptIv()
                )
            },
            IntentModel::class.java
        )

     //   collectFaceAuthResponse()
        // setConsentText()
    }

    private val startUidaiAuthResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            try {
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data

                    if (intent != null) {
                        val captureResponse =
                            intent.getStringExtra(AppConstant.Constants.CAPTURE_INTENT_RESPONSE_DATA)

                        if (!captureResponse.isNullOrEmpty()) {
                            log("handleCaptureResponse", captureResponse)
                            handleCaptureResponse(captureResponse)
                        } else {
                            log("handleCaptureResponse", "Capture response data is null or empty.")
                            toastShort("Capture response is empty.")
                        }
                    } else {
                        log("handleCaptureResponse", "Intent data is null.")
                        toastShort("Failed to get capture response data.")
                    }
                } else {
                    toastLong("Failed to capture data.")
                    log("handleCaptureResponse", "Activity result code: ${result.resultCode}")
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
                toastShort("Error: Missing data in result.")
                log("startUidaiAuthResult", "NullPointerException: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                toastShort("An error occurred while processing the result.")
                log("startUidaiAuthResult", "Exception: ${e.message}")
            }
        }



    private fun getTransactionID(): String {
        val secureRandom = SecureRandom()
        return secureRandom.nextInt(9999).toString()
    }


    private fun invokeCaptureIntent() {

        try {
            val intent1 = Intent(AppConstant.Constants.CAPTURE_INTENT)
            intent1.putExtra(
                AppConstant.Constants.CAPTURE_INTENT_REQUEST,
                createPidOptions(getTransactionID(), "auth")
            )
            startUidaiAuthResult.launch(intent1)

            // val packageName = "com.example.otherapp" // Replace with the target app's package name
            val intent =
                requireContext().packageManager.getLaunchIntentForPackage(AppConstant.Constants.CAPTURE_INTENT)
            intent?.putExtra(
                AppConstant.Constants.CAPTURE_INTENT_REQUEST,
                createPidOptions(getTransactionID(), "auth")
            )
            if (intent != null) {
                startActivity(intent)
            }
        } catch (exp: Exception) {
            log("EKYCDATA", exp.toString())
        }

    }

    private fun createPidOptions(txnId: String, purpose: String): String {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<PidOptions ver=\"1.0\" env=\"${PRODUCTION}\">\n" + "   <Opts fCount=\"\" fType=\"\" iCount=\"\" iType=\"\" pCount=\"\" pType=\"\" format=\"\" pidVer=\"2.0\" timeout=\"\" otp=\"\" wadh=\"${SecurityUtils.getWadhKey()}\" posh=\"\" />\n" + "   <CustOpts>\n" + "      <Param name=\"txnId\" value=\"${txnId}\"/>\n" + "      <Param name=\"purpose\" value=\"$purpose\"/>\n" + "      <Param name=\"language\" value=\"$LANGUAGE}\"/>\n" + "   </CustOpts>\n" + "</PidOptions>"
    }


    private fun handleCaptureResponse(captureResponse: String) {
        try {

            // Check if camera permission is granted
            checkCameraPermission()

            // Parse the capture response XML to an object
            val response = CaptureResponse.fromXML(captureResponse)

            if (response.isSuccess) {
                showProgressBar()
                // Process the response to generate the PoiType or other required fields
                val poiType = XstreamCommonMethods.processPidBlockEkyc(
                    response.toXML(),
                    binding.etAadhaar.text.toString(),
                    false,
                    requireContext()
                )

                // Define Pre-Production URL (use a constant or environment configuration in production)
              //  val authURL = "http://10.247.252.95:8080/NicASAServer/ASAMain" //preProd
                val authURL = "http://10.247.252.93:8080/NicASAServer/ASAMain"  //Prod

                // Record the start time for elapsed time computation
                startTime = SystemClock.elapsedRealtime()

                // Post the processed data for Face Authentication
                commonViewModel.postOnAUAFaceAuthNREGA(
                    AppConstant.StaticURL.FACE_AUTH_UIADI,
                    UidaiKycRequest(poiType, authURL)
                )

                collectFaceAuthResponse()

                // Handle Aadhaar authentication or additional processing here if required
            } else {
                toastLong(getString(R.string.kyc_failed_msg))
            }


        } catch (e: SecurityException) {
            // Handle camera permission-related issues
            hideProgressBar()
            e.printStackTrace()
            toastShort("Camera permission is required for this feature.")
            log("EKYCDATA", "SecurityException: ${e.message}")
        } catch (e: IllegalArgumentException) {
            // Handle cases where the response parsing might fail
            hideProgressBar()
            e.printStackTrace()
            toastShort("Invalid Capture Response format.")
            log("EKYCDATA", "IllegalArgumentException: ${e.message}")
        } catch (e: Exception) {
            // Catch all other exceptions
            hideProgressBar()
            e.printStackTrace()
            toastShort("An error occurred while processing the response.")
            log("EKYCDATA", "Exception: ${e.message}")
            // e.message?.copyToClipboard(requireContext())
        }
    }



    private fun checkCameraPermission(): Boolean {
        val permissionsNotGranted = java.util.ArrayList<String>()
        for (permission in neededPermissions) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsNotGranted.add(permission)
            }
        }
        if (permissionsNotGranted.isNotEmpty()) {
            var shouldShowAlert = false
            for (permission in permissionsNotGranted) {
                shouldShowAlert =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        permission
                    )
            }
            if (shouldShowAlert) {
                showPermissionAlert(permissionsNotGranted.toTypedArray())
            } else {
                requestPermissions(permissionsNotGranted.toTypedArray())
            }
            return false
        }
        return true
    }

    private fun showPermissionAlert(permissions: Array<String>) {
        val alertBuilder = AlertDialog.Builder(requireActivity())
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle("Permission Required")
        alertBuilder.setMessage("You must grant permission to access camera to run this application")
        alertBuilder.setPositiveButton(
            android.R.string.yes
        ) { _, _ -> requestPermissions(permissions) }
        val alert = alertBuilder.create()
        alert.show()
    }

    private fun requestPermissions(permissions: Array<String>) {
        ActivityCompat.requestPermissions(
            requireActivity(),
            permissions,
            CAMERA_REQUEST
        )
    }


    private fun collectFaceAuthResponse() {

        lifecycleScope.launch {
            try {
                collectLatestLifecycleFlow(commonViewModel.postOnAUAFaceAuthNREGA) { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                        }

                        is Resource.Error -> {
                            hideProgressBar()
                            resource.error?.let { errorResponse ->
                                toastShort(errorResponse.message)
                                log("EKYCDATA", errorResponse.message ?: "Unknown error message")
                            } ?: run {
                                toastShort("Nothing to show pls try again")
                            }
                        }

                        is Resource.Success -> {

                            resource.data?.body()?.let { uidaiData: UidaiResp ->
                                try {
                                    val kycResp = XstreamCommonMethods.respDecodedXmlToPojoEkyc(
                                        uidaiData.PostOnAUA_Face_authResult
                                    )

                                    //  uidaiData.PostOnAUA_Face_authResult.copyToClipboard(requireContext())

                                    log("EKYCDATA", kycResp.toString())

                                    if (kycResp.isSuccess) {
                                        val bytes: ByteArray =
                                            Base64.decode(kycResp.uidData.pht, Base64.DEFAULT)
                                        val bitmap =
                                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                                        userPhotoUIADI = bitmap
                                        ekycImage = kycResp.uidData.pht ?: ""

                                        name = kycResp.uidData.poi.name ?: "N/A"
                                        photo = kycResp.uidData.pht ?: "N/A"
                                        gender = kycResp.uidData.poi.gender ?: "N/A"
                                        dob = kycResp.uidData.poi.dob ?: "N/A"
                                        careOf = kycResp.uidData.poa.co ?: "N/A"
                                        state = kycResp.uidData.poa.state ?: "N/A"
                                        dist = kycResp.uidData.poa.dist ?: "N/A"
                                        block = kycResp.uidData.poa.subdist ?: "N/A"

                                        village = kycResp.uidData.poa.vtc ?: "N/A"
                                        street = kycResp.uidData.poa.loc ?: "N/A"
                                        po = kycResp.uidData.poa.po ?: "N/A"
                                        pinCode = kycResp.uidData.poa.pc ?: "N/A"


                                        // Get the last 4 digits
                                        val input = binding.etAadhaar.text.toString()
                                        val lastFourDigits = input.takeLast(4)
                                        // Create the masked string
                                        val maskedString =
                                            "*".repeat(input.length - 4) + lastFourDigits

                                        // Output the result
                                        lifecycleScope.launch {
                                            val email = AppUtil.getSavedEmailPreference(requireContext())
                                            val phone =AppUtil.getSavedMobileNoPreference(requireContext())

                                            userPreferences.updateUserStateLgdCode(null)
                                            userPreferences.updateUserStateLgdCode(selectedStateLgdCode)

                                            AppUtil.saveStateCode(requireContext(),selectedStateCode)


                                            val encryptedAadhaarString =   AESCryptography.encryptIntoBase64String(binding.etAadhaar.text.toString(), AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedName =   AESCryptography.encryptIntoBase64String(name, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedGender =   AESCryptography.encryptIntoBase64String(gender, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedDob =   AESCryptography.encryptIntoBase64String(dob, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedState =   AESCryptography.encryptIntoBase64String(state, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedSelectedStateCode =   AESCryptography.encryptIntoBase64String(selectedStateCode, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedDist =   AESCryptography.encryptIntoBase64String(dist, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedBlock =   AESCryptography.encryptIntoBase64String(block, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedPo =   AESCryptography.encryptIntoBase64String(po, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedVillage =   AESCryptography.encryptIntoBase64String(village, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedPinCode =   AESCryptography.encryptIntoBase64String(pinCode, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedPhone = phone.let {
                                                AESCryptography.encryptIntoBase64String(
                                                    it, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            }
                                            val encryptedEmail = email?.let {
                                                AESCryptography.encryptIntoBase64String(
                                                    it, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            }

                                            val encryptedCareOf =   AESCryptography.encryptIntoBase64String(careOf, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedStreet =   AESCryptography.encryptIntoBase64String(street, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedLdgdCode =   AESCryptography.encryptIntoBase64String(selectedStateLgdCode, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)


                                            if (selectedStateCode!=""){

                                                commonViewModel.getCreateUserAPI(UserCreationReq(
                                                    encryptedAadhaarString,
                                                    encryptedName,
                                                    encryptedGender,
                                                    encryptedDob,
                                                    encryptedState,
                                                    encryptedSelectedStateCode,
                                                    encryptedDist,
                                                    encryptedBlock,
                                                    encryptedPo,
                                                    encryptedVillage,
                                                    encryptedPinCode,
                                                    encryptedPhone,
                                                    encryptedEmail,
                                                    encryptedCareOf,
                                                    encryptedStreet,
                                                    BuildConfig.VERSION_NAME,
                                                    photo,
                                                    AppUtil.getAndroidId(requireContext()),encryptedLdgdCode,true
                                                )
                                                )


                                            }

                                          else
                                              toastLong("Please select state")


                                        }

                                        hideProgressBar()

                                        // toastShort("Ekyc Completed")
                                    } else {
                                        hideProgressBar()
                                        val decodedRar = decodeBase64(kycResp.rar)
                                        decodedRar?.let { decodedRarParsed ->
                                            val authRes = respDecodedXmlToPojoAuth(decodedRarParsed)
                                            val errorDesc =
                                                XstreamCommonMethods.getAuthErrorDescription(authRes.info)
                                            log("EKYCDATA", errorDesc)

                                            binding.recyclerView.gone()
                                            toastShort("EKYCDATA: Failed")
                                        } ?: toastShort("Getting Error")
                                    }
                                } catch (e: Exception) {
                                    hideProgressBar()
                                    e.printStackTrace()
                                    log("EKYCDATA", "Error processing KYC response: ${e.message}")
                                    toastShort("Error processing KYC response")
                                    binding.recyclerView.gone()

                                }
                            } ?: toastShort("Server error from uidai. Please try again.")

                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                hideProgressBar()
                log("EKYCDATA", "Unhandled error: ${e.message}")
                toastShort("An unexpected error occurred. Please try again.")
                binding.recyclerView.gone()

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Make sure to remove the callback to avoid memory leaks
        onBackPressedCallback.remove()
    }

   @SuppressLint("SuspiciousIndentation")
   private fun showBottomSheet(
       image: Bitmap,
       name: String,
       gender: String,
       dateOfBirth: String,
       careOf: String
   ) {
       val bottomSheetDialog = BottomSheetDialog(requireContext())

       // Inflate the layout
       val view = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
       bottomSheetDialog.setContentView(view)

       // Prevent closing when tapping outside
       bottomSheetDialog.setCanceledOnTouchOutside(false)

       // Find views
       val imageView = view.findViewById<ImageView>(R.id.circleImageView)
       val nameView = view.findViewById<TextView>(R.id.eKYCCandidateName)
       val genderView = view.findViewById<TextView>(R.id.eKYCGender)
       val dobView = view.findViewById<TextView>(R.id.eKYCDob)
       val careOfView = view.findViewById<TextView>(R.id.eKYCCareOf)
       val okButton = view.findViewById<TextView>(R.id.tvLogin)

       // Set data
       imageView.setImageBitmap(image)
       nameView.text = name
       genderView.text = gender
       dobView.text = dateOfBirth
       careOfView.text = careOf

       // Handle OK button click
       okButton.setOnClickListener {

           tokenGen= AppUtil.getSavedTokenPreference(requireContext())

          // if (tokenGen==(tokenViaCreate)){
               AppUtil.saveLoginStatus(requireContext(), true)

               val navController = findNavController()

               // Pop previous fragments and navigate to home
               navController.popBackStack(R.id.loginFragment, true)
               navController.popBackStack(R.id.registerFragment, true)
               navController.popBackStack(R.id.ekycFragment, true)
               navController.navigate(R.id.mainHomePage)

               bottomSheetDialog.dismiss()

           //}
          // else toastShort("Please Wait")

       }

       // Handle back button press
       bottomSheetDialog.setOnKeyListener { dialog, keyCode, event ->
           if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
               // Show a confirmation dialog before closing
               AlertDialog.Builder(requireContext())
                   .setTitle("Exit")
                   .setMessage("Do you want to close this screen?")
                   .setPositiveButton("Yes") { _, _ ->
                       bottomSheetDialog.dismiss()
                   }
                   .setNegativeButton("No", null)
                   .show()
               return@setOnKeyListener true
           }
           false
       }

       // Show the BottomSheetDialog
       bottomSheetDialog.show()
   }
    private fun collectAadharResponse() {
        lifecycleScope.launch {
            collectLatestLifecycleFlow(commonViewModel.getAadhaarCheck) {
                when (it) {
                    is Resource.Loading -> {}
                    is Resource.Error -> {
                        it.error?.let { baseErrorResponse ->
                            showSnackBar(baseErrorResponse.message)
                            toastShort("error in create Api")
                        }
                    }

                    is Resource.Success -> {
                        it.data?.let { getAadhaarCheck ->
                            if (getAadhaarCheck.responseCode == 200) {
                                showSnackBar(getAadhaarCheck.responseDesc)

                                invokeCaptureIntent()

                            }
                            else if (getAadhaarCheck.responseCode == 301) {


                                //Update app
                                showUpdateDialog()

                            }

                            else if (getAadhaarCheck.responseCode == 333) {

                                showSnackBar(getAadhaarCheck.responseDesc)
                            }
                        } ?: showSnackBar("Internal Sever Error")
                    }
                }
            }
        }
    }
    private fun showUpdateDialog() {
        val builder = AlertDialog.Builder(requireContext()) // 🔥 use requireContext() inside Fragment
        builder.setTitle("Update Available")
        builder.setMessage("A new version of the app is available. Please update to continue.")

        builder.setPositiveButton("Update") { dialog, _ ->
            val appPackageName = "com.kaushalpanjee"
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
                intent.setPackage("com.android.vending")
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName&hl=en_IN"))
                startActivity(intent)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.setCancelable(false)
        builder.create().show()
    }


    private fun showStateDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Alert")
        builder.setMessage(R.string.please_select_your_state_carefully_this_selection_cannot_be_changed_once_your_account_created)

        builder.setPositiveButton("ok") { dialog, _ ->
            binding.recyclerView.gone()
            binding.progressButton.root.gone()
            binding.etAadhaar.visible()
            if (binding.etAadhaar.text.isNotEmpty()) {
                binding.etAadhaar.setText("")
            }
            isStateSelected = true
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.setCancelable(false)
        builder.create().show()
    }



    private fun collectUserCreationResponse() {
        lifecycleScope.launch {
            collectLatestLifecycleFlow(commonViewModel.getuserCreation) {
                when (it) {
                    is Resource.Loading -> {}
                    is Resource.Error -> {
                        it.error?.let { baseErrorResponse ->
                            showSnackBar(baseErrorResponse.message)
                            toastShort("error in create Api")
                        }
                    }

                    is Resource.Success -> {
                        it.data?.let { getUserCreationRes ->
                            if (getUserCreationRes.responseCode == 200) {
                                for (x in getUserCreationRes.wrappedList) {
                                    userPreferences.updateUserId(null)
                                    userPreferences.updateUserId(x.userId)
                                    tokenViaCreate= x.appCode

                                }
                                userPhotoUIADI?.let { it1 -> showBottomSheet(it1, name, gender, dob, careOf) }

                                toastLong("Your username and password have been sent to your email.")

                            }
                            else if (getUserCreationRes.responseCode == 301) {

                                showUpdateDialog()
                            }

                            else if (getUserCreationRes.responseCode == 333) {

                                showSnackBar(getUserCreationRes.responseDesc)
                            }
                            else
                                showSnackBar(getUserCreationRes.responseDesc)

                        } ?: showSnackBar("Internal Sever Error")
                    }
                }
            }
        }
    }
    object AadhaarValidator {
        private val multiplicationTable = arrayOf(
            intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
            intArrayOf(1, 2, 3, 4, 0, 6, 7, 8, 9, 5),
            intArrayOf(2, 3, 4, 0, 1, 7, 8, 9, 5, 6),
            intArrayOf(3, 4, 0, 1, 2, 8, 9, 5, 6, 7),
            intArrayOf(4, 0, 1, 2, 3, 9, 5, 6, 7, 8),
            intArrayOf(5, 9, 8, 7, 6, 0, 4, 3, 2, 1),
            intArrayOf(6, 5, 9, 8, 7, 1, 0, 4, 3, 2),
            intArrayOf(7, 6, 5, 9, 8, 2, 1, 0, 4, 3),
            intArrayOf(8, 7, 6, 5, 9, 3, 2, 1, 0, 4),
            intArrayOf(9, 8, 7, 6, 5, 4, 3, 2, 1, 0)
        )

        private val permutationTable = arrayOf(
            intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
            intArrayOf(1, 5, 7, 6, 2, 8, 3, 0, 9, 4),
            intArrayOf(5, 8, 0, 3, 7, 9, 6, 1, 4, 2),
            intArrayOf(8, 9, 1, 6, 0, 4, 3, 5, 2, 7),
            intArrayOf(9, 4, 5, 3, 1, 2, 6, 8, 7, 0),
            intArrayOf(4, 2, 8, 6, 5, 7, 3, 9, 0, 1),
            intArrayOf(2, 7, 9, 3, 8, 0, 6, 4, 1, 5),
            intArrayOf(7, 0, 4, 6, 9, 1, 3, 2, 5, 8)
        )

        private val inverseTable = intArrayOf(0, 4, 3, 2, 1, 5, 6, 7, 8, 9)

        fun isValidAadhaar(aadhaar: String): Boolean {
            if (aadhaar.length != 12 || !aadhaar.all { it.isDigit() }) return false

            var checksum = 0
            aadhaar.reversed().forEachIndexed { index, char ->
                checksum = multiplicationTable[checksum][permutationTable[index % 8][char - '0']]
            }
            return checksum == 0
        }
    }
    private fun collectTokenResponse() {
        lifecycleScope.launch {
            collectLatestLifecycleFlow(commonViewModel.getToken) {
                when (it) {
                    is Resource.Loading -> showProgressBar()
                    is Resource.Error -> {
                        hideProgressBar()
                        it.error?.let { baseErrorResponse ->
                            toastShort(baseErrorResponse.message)
                        }
                    }

                    is Resource.Success -> {
                        hideProgressBar()
                        it.data?.let { getToken ->
                            when (getToken.responseCode) {
                                200 -> {

                                    AppUtil.saveTokenPreference(requireContext(),"Bearer "+getToken.authToken)


                                }


                                301-> {
                                    showSnackBar("Please Update from PlayStore")

                                }
                                else -> {
                                    showSnackBar("Something went wrong")
                                }
                            }
                        } ?: showSnackBar("Internal Server Error")
                    }
                }
            }
        }
    }

    private fun formatCheckBoxText(checkBox: CheckBox) {
        val text = getString(R.string.consent)


        val spannable = SpannableString(text)

        // Bold specific words
        val boldWords = listOf("Unified IT Platform for DDUGKY and RSETI", "National Informatics Centre")

        for (word in boldWords) {
            val startIndex = text.indexOf(word)
            if (startIndex != -1) {
                spannable.setSpan(StyleSpan(Typeface.BOLD), startIndex, startIndex + word.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        checkBox.text = spannable
    }


}
