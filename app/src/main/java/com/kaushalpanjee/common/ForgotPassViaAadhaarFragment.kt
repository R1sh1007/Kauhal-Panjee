package com.kaushalpanjee.common

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.kaushalpanjee.BuildConfig
import com.kaushalpanjee.R
import com.kaushalpanjee.common.EKYCFragment.AadhaarValidator
import com.kaushalpanjee.common.model.UidaiKycRequest
import com.kaushalpanjee.common.model.UidaiResp
import com.kaushalpanjee.common.model.request.AadhaarRekycReq
import com.kaushalpanjee.common.model.request.UpdatePasswordForReq
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
import com.kaushalpanjee.databinding.FragmentForgotViaAadhaarBinding
import com.kaushalpanjee.model.kyc_resp_pojo.XstreamCommonMethods
import com.kaushalpanjee.model.kyc_resp_pojo.XstreamCommonMethods.respDecodedXmlToPojoAuth
import com.kaushalpanjee.uidai.capture.CaptureResponse
import com.kaushalpanjee.security.SecurityUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.security.SecureRandom

@AndroidEntryPoint
class ForgotPassViaAadhaarFragment  : BaseFragment<FragmentForgotViaAadhaarBinding>(
    FragmentForgotViaAadhaarBinding::inflate) {

    private val commonViewModel: CommonViewModel by viewModels()

    private var aadhaarValidate = false
    private var showPassword = true
    private var candidateId = ""
    private var aadhaarNumber = ""
    private var startTime: Long = 0
    private val neededPermissions = arrayOf(Manifest.permission.CAMERA)
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
    private var newPassword = ""
    private var confirmPassword = ""
    private var userPhotoUIADI: Bitmap? = null
    private var ekycImage: String = ""




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectCheckAadhaarResponse()

        init()

    }

    private fun init(){

        listener()

    }

    private fun listener(){

        binding.progressBackButton.setOnClickListener {
            findNavController().navigateUp()
        }



        binding.tvconfirm.setOnClickListener {


            newPassword= binding.etnewPassword.text.toString()
            confirmPassword= binding.etconfirmPassword.text.toString()



            if ( newPassword.isNotEmpty() && confirmPassword.isNotEmpty()){


                if (newPassword==confirmPassword ){

                    val encryptedPassword =   AESCryptography.encryptIntoBase64String(newPassword,
                        AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)

                    commonViewModel.updatePasswordForget(UpdatePasswordForReq(encryptedPassword,candidateId,BuildConfig.VERSION_NAME))

                    collectUpdatePassResponse()

                }
                else
                    toastShort("confirm Password must be same as new password")

            }
            else
                toastShort("Please enter password")
        }



        binding.etAadhaar.setRightDrawablePassword(
            false,
            ContextCompat.getDrawable(requireContext(), R.drawable.icon_aadhaar), null,
            ContextCompat.getDrawable(requireContext(), R.drawable.close_eye), null
        )



        binding.etAadhaar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val aadhaar = s.toString()
                if (aadhaar.length == 12) {
                    if (AadhaarValidator.isValidAadhaar(aadhaar)) {
                        binding.etAadhaar.error = null
                        aadhaarValidate=true
                        binding.aadhaarVerifyButton.root.visibility = View.VISIBLE


                    } else {
                        binding.etAadhaar.error = "❌ Invalid Aadhaar Number"  // ❌ Show error
                        aadhaarValidate=false
                        binding.aadhaarVerifyButton.root.visibility = View.GONE



                    }
                }
                else
                    binding.aadhaarVerifyButton.root.visibility = View.GONE

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        )

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



        binding.aadhaarVerifyButton.centerButton.setOnClickListener {

            aadhaarNumber = binding.etAadhaar.text.toString()

            if (aadhaarNumber.isNotEmpty()){

                val encryptedAadhaarString =   AESCryptography.encryptIntoBase64String(aadhaarNumber,
                    AppConstant.Constants.ENCRYPT_KEY, AppConstant.Constants.ENCRYPT_IV_KEY)


                commonViewModel.checkAadhaarFor(encryptedAadhaarString,BuildConfig.VERSION_NAME)

            }
            else
                toastShort("Please Enter Aadhaar Number")

        }

    }

    private fun collectCheckAadhaarResponse() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                commonViewModel.checkAadhaarFor.collectLatest { response ->
                    when (response) {
                        is Resource.Loading -> {
                            // Show loader if needed
                        }

                        is Resource.Success -> {
                            response.data?.let { result ->
                                when (result.responseCode) {
                                    200 -> {
                                        candidateId = result.candidateId
                                        invokeCaptureIntent()
                                    }
                                    301 -> toastShort("Kindly Update from Play Store")
                                    404 -> showSnackBar(result.responseDesc)
                                    else -> showSnackBar(result.responseDesc)
                                }
                            } ?: showSnackBar("Internal Server Error")
                        }

                        is Resource.Error -> {
                            response.error?.let {
                                showSnackBar(it.message)
                                toastShort("Error in Aadhaar verification")
                            }
                        }
                    }
                }
            }
        }
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
                    aadhaarNumber,
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


                                                binding.etAadhaar.gone()
                                                binding.llPass.visible()
                                                binding.tvid.visible()
                                            binding.aadhaarVerifyButton.root.gone()
                                            binding.tvid.text= "CandidateId: $candidateId"



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
                                    findNavController().navigateUp()
                                    e.printStackTrace()
                                    log("EKYCDATA", "Error processing KYC response: ${e.message}")
                                    toastShort("Error processing KYC response")

                                }
                            }

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



    private fun collectUpdatePassResponse() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                commonViewModel.updatePasswordForget.collectLatest { response ->
                    when (response) {
                        is Resource.Loading -> {
                            // Show loader if needed
                        }

                        is Resource.Success -> {
                            response.data?.let { result ->
                                when (result.responseCode) {
                                    200 -> {
                                        toastShort(result.responseDesc)
                                        findNavController().navigateUp()
                                    }
                                    301 -> toastShort("Kindly Update from Play Store")
                                    else -> showSnackBar(result.responseDesc)
                                }
                            } ?: showSnackBar("Internal Server Error")
                        }

                        is Resource.Error -> {
                            response.error?.let {
                                showSnackBar(it.message)
                                toastShort("Error in Aadhaar verification")
                            }
                        }
                    }
                }
            }
        }
    }

}

