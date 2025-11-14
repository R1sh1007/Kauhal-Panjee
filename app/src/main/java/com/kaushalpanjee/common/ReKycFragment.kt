package com.kaushalpanjee.common

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.kaushalpanjee.BuildConfig
import com.kaushalpanjee.R
import com.kaushalpanjee.common.EKYCFragment.AadhaarValidator
import com.kaushalpanjee.common.model.UidaiKycRequest
import com.kaushalpanjee.common.model.UidaiResp
import com.kaushalpanjee.common.model.request.AadhaarRekycReq
import com.kaushalpanjee.common.model.request.UserCreationReq
import com.kaushalpanjee.common.model.response.IntentModel
import com.kaushalpanjee.common.model.response.IntentResponse
import com.kaushalpanjee.core.basecomponent.BaseFragment
import com.kaushalpanjee.core.util.AESCryptography
import com.kaushalpanjee.core.util.AppConstant
import com.kaushalpanjee.core.util.AppConstant.Constants.LANGUAGE
import com.kaushalpanjee.core.util.AppConstant.Constants.PRODUCTION
import com.kaushalpanjee.core.util.AppUtil
import com.kaushalpanjee.core.util.Resource
import com.kaushalpanjee.core.util.decodeBase64
import com.kaushalpanjee.core.util.gone
import com.kaushalpanjee.core.util.log
import com.kaushalpanjee.core.util.onRightDrawableClicked
import com.kaushalpanjee.core.util.setRightDrawablePassword
import com.kaushalpanjee.core.util.toastLong
import com.kaushalpanjee.core.util.toastShort
import com.kaushalpanjee.core.util.visible
import com.kaushalpanjee.databinding.FragmentRekycLayoutBinding
import com.kaushalpanjee.model.kyc_resp_pojo.XstreamCommonMethods
import com.kaushalpanjee.model.kyc_resp_pojo.XstreamCommonMethods.respDecodedXmlToPojoAuth
import com.kaushalpanjee.security.SecurityUtils
import com.kaushalpanjee.uidai.capture.CaptureResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.security.SecureRandom


@AndroidEntryPoint
class ReKycFragment  : BaseFragment<FragmentRekycLayoutBinding>(FragmentRekycLayoutBinding::inflate)  {

    private var showPassword = true
    private var aadhaarValidate = false
    private var aadhaarNumber = ""
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
    private var village = ""
    private var photo = ""
    private var userPhotoUIADI: Bitmap? = null
    private var ekycImage: String = ""
    private var intentResponse: IntentResponse? = null
    private val neededPermissions = arrayOf(Manifest.permission.CAMERA)
    private var startTime: Long = 0
    private val commonViewModel: CommonViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        init()
        initEKYC()

    }

    private fun init(){

        listener()

    }
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
                    SecurityUtils.getCryptId(),
                    SecurityUtils.getCryptIv()
                )
            },
            IntentModel::class.java
        )


    }

    private  fun listener(){


                invokeCaptureIntent()

    }



    @SuppressLint("SuspiciousIndentation")
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


                                    log("EKYCDATA", kycResp.toString())

                                    if (kycResp.isSuccess) {
                                        val bytes: ByteArray =
                                            Base64.decode(kycResp.uidData.pht, Base64.DEFAULT)
                                        var bitmap =
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



                                        // Output the result
                                        lifecycleScope.launch {




                                            val encryptedAadhaarString =   AESCryptography.encryptIntoBase64String( AppUtil.getSavedAadhaarPreference(requireContext()), AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedName =   AESCryptography.encryptIntoBase64String(name, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedGender =   AESCryptography.encryptIntoBase64String(gender, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedDob =   AESCryptography.encryptIntoBase64String(dob, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedState =   AESCryptography.encryptIntoBase64String(state, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedDist =   AESCryptography.encryptIntoBase64String(dist, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedBlock =   AESCryptography.encryptIntoBase64String(block, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedPo =   AESCryptography.encryptIntoBase64String(po, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedVillage =   AESCryptography.encryptIntoBase64String(village, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedPinCode =   AESCryptography.encryptIntoBase64String(pinCode, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedCareOf =   AESCryptography.encryptIntoBase64String(careOf, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val encryptedStreet =   AESCryptography.encryptIntoBase64String(street, AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)





                                                commonViewModel.aadhaarRekycApi(
                                                    AadhaarRekycReq(
                                                        photo,
                                                    encryptedAadhaarString,
                                                        BuildConfig.VERSION_NAME,
                                                        encryptedBlock,
                                                    encryptedName,
                                                        encryptedCareOf,
                                                        encryptedDob,
                                                        encryptedDist,
                                                    encryptedGender,
                                                        encryptedPinCode,
                                                        encryptedPo,
                                                    encryptedState,
                                                        encryptedStreet,
                                                    encryptedVillage,
                                                    AppUtil.getAndroidId(requireContext()),
                                                        userPreferences.getUseID()
                                                )
                                                    ,AppUtil.getSavedTokenPreference(requireContext())
                                                )

                                            collectAadharRekycResponse()


                                        }

                                        hideProgressBar()


                                    }
                                    else {
                                        hideProgressBar()
                                        val decodedRar = decodeBase64(kycResp.rar)
                                        decodedRar?.let { decodedRarParsed ->
                                            val authRes = respDecodedXmlToPojoAuth(decodedRarParsed)
                                            val errorDesc =
                                                XstreamCommonMethods.getAuthErrorDescription(authRes.info)
                                            log("EKYCDATA", errorDesc)

                                            toastShort("EKYCDATA: Failed")
                                            findNavController().navigateUp()
                                        } ?: toastShort("Getting Error")
                                    }
                                } catch (e: Exception) {
                                    hideProgressBar()
                                    toastShort("going back2")
                                    findNavController().navigateUp()
                                    e.printStackTrace()
                                    log("EKYCDATA", "Error processing KYC response: ${e.message}")
                                    toastShort("Error processing KYC response")

                                }
                            } ?: toastShort("Server error from uidai. Please try again.")

                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                hideProgressBar()
                toastShort("going back4")
                findNavController().navigateUp()

                log("EKYCDATA", "Unhandled error: ${e.message}")
                toastShort("An unexpected error occurred. Please try again.")

            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun showBottomSheet(
        image: Bitmap?,
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
            findNavController().navigateUp()

            bottomSheetDialog.dismiss()



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
    private fun getTransactionID(): String {
        val secureRandom = SecureRandom()
        return secureRandom.nextInt(9999).toString()
    }

    private fun createPidOptions(txnId: String, purpose: String): String {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<PidOptions ver=\"1.0\" env=\"$PRODUCTION\">\n" + "   <Opts fCount=\"\" fType=\"\" iCount=\"\" iType=\"\" pCount=\"\" pType=\"\" format=\"\" pidVer=\"2.0\" timeout=\"\" otp=\"\" wadh=\"${SecurityUtils.getWadhKey()}\" posh=\"\" />\n" + "   <CustOpts>\n" + "      <Param name=\"txnId\" value=\"${txnId}\"/>\n" + "      <Param name=\"purpose\" value=\"$purpose\"/>\n" + "      <Param name=\"language\" value=\"$LANGUAGE}\"/>\n" + "   </CustOpts>\n" + "</PidOptions>"
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
                    AppUtil.getSavedAadhaarPreference(requireContext()),
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


    private fun collectAadharRekycResponse() {

        lifecycleScope.launch {
            collectLatestLifecycleFlow(commonViewModel.aadhaarRekycApi) {
                when (it) {
                    is Resource.Loading -> {}
                    is Resource.Error -> {
                        it.error?.let { baseErrorResponse ->
                            showSnackBar(baseErrorResponse.message)
                            toastShort("error in create Api")
                        }
                    }

                    is Resource.Success -> {
                        it.data?.let { getAadhaarRekyc ->
                            if (getAadhaarRekyc.responseCode == 200) {




                                showSnackBar(getAadhaarRekyc.responseDesc)

                          showBottomSheet(userPhotoUIADI, name, gender, dob, careOf)

                                showSnackBar(getAadhaarRekyc.responseMsg)

                            }
                            else if (getAadhaarRekyc.responseCode == 301) {


                                //Update app
                                showUpdateDialog()

                            }
                            else
                                showSnackBar(getAadhaarRekyc.responseMsg)


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

}