package com.kaushalpanjee.common

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.transition.TransitionManager
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.kaushalpanjee.BuildConfig
import com.kaushalpanjee.R
import com.kaushalpanjee.common.model.request.AdharDetailsReq
import com.kaushalpanjee.common.model.request.CandidateReq
import com.kaushalpanjee.common.model.request.SectionAndPerReq
import com.kaushalpanjee.common.model.response.Address
import com.kaushalpanjee.common.model.response.AddressDetail
import com.kaushalpanjee.common.model.response.Bank
import com.kaushalpanjee.common.model.response.CandidateDetails
import com.kaushalpanjee.common.model.response.Educational
import com.kaushalpanjee.common.model.response.Employment
import com.kaushalpanjee.common.model.response.Personal
import com.kaushalpanjee.common.model.response.PersonalDetail
import com.kaushalpanjee.common.model.response.Secc
import com.kaushalpanjee.common.model.response.Training
import com.kaushalpanjee.common.model.response.UserDetails
import com.kaushalpanjee.core.basecomponent.BaseFragment
import com.kaushalpanjee.core.util.AESCryptography
import com.kaushalpanjee.core.util.AppConstant
import com.kaushalpanjee.core.util.AppUtil
import com.kaushalpanjee.core.util.Resource
import com.kaushalpanjee.core.util.UserPreferences
import com.kaushalpanjee.core.util.createHalfCircleProgressBitmap
import com.kaushalpanjee.core.util.gone
import com.kaushalpanjee.core.util.setDrawable
import com.kaushalpanjee.core.util.visible
import com.kaushalpanjee.databinding.FragmentViewDetailsBinding
import com.kaushalpanjee.security.SecurityUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class ViewDetailsFragment : BaseFragment<FragmentViewDetailsBinding>(FragmentViewDetailsBinding::inflate) {


    private val commonViewModel: CommonViewModel by activityViewModels()


    //Boolean Values
    private var isPersonalVisible = false
    private var isAddressVisible = false
    private var isEducationalInfoVisible = false
    private var isEmploymentInfoVisible = false
    private var isTrainingInfoVisible = false
    private var isBankingInfoVisible = false
    private var isSeccInfoVisible = false
    private var userAadhaarDetailsList: List<UserDetails> = mutableListOf()
    private var userCandidatePersonalDetailsList: List<Personal> = mutableListOf()
    private var userCandidatePersonalDetailsList2: List<PersonalDetail> = mutableListOf()
    private var userCandidateAddressDetailsList: List<Address> = mutableListOf()
    private var userCandidateAddressDetailsList2: List<AddressDetail> = mutableListOf()
    private var userCandidateSeccDetailsList: List<Secc> = mutableListOf()
    private var userCandidateEducationalDetailsList: List<Educational> = mutableListOf()
    private var userCandidateEmploymentDetailsList: List<Employment> = mutableListOf()
    private var userCandidateTrainingDetailsList: List<Training> = mutableListOf()
    private var userCandidateBankDetailsList: List<Bank> = mutableListOf()

    private var personalStatus = ""
    private var educationalStatus = ""
    private var trainingStatus = ""
    private var seccStatus = ""
    private var addressStatus = ""
    private var employmentStatus = ""
    private var bankingStatus = ""
    private var totalPercentange = 0.0f
    private var voterImage=  ""
    private var pmaygImage=  ""
    private var pipImage=  ""
    private var dlImage=  ""
    private var categoryImage=  ""
    private var minorityImage =  ""
    private var pwdImage =  ""
    private var nregaImage =  ""
    private var shgImage =  ""
    private var antyodyaImage=  ""
    private var rsbyImage =  ""
    private var residenceImage=  ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())

        init()

        collectAadharDetailsResponse()
        collectSetionAndPerResponse()
        collectCandidateDetailsResponse()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Custom behavior when back is pressed

                // Example: Navigate to HomeFragment and clear back stack
                findNavController().navigate(
                    R.id.homeFragment,
                    null,
                    NavOptions.Builder().setPopUpTo(R.id.viewDetailsFragment, true).build()
                )
            }
        })

    }

    private fun init()
    {
        listeners()
        commonViewModel.getSecctionAndPerAPI(SectionAndPerReq(BuildConfig.VERSION_NAME,userPreferences.getUseID(),AppUtil.getAndroidId(requireContext())),AppUtil.getSavedTokenPreference(requireContext()))

        commonViewModel.getAadhaarListAPI(AdharDetailsReq
            (BuildConfig.VERSION_NAME, AppUtil.getAndroidId(requireContext()), userPreferences.getUseID()),AppUtil.getSavedTokenPreference(requireContext())
        )

        commonViewModel.getCandidateDetailsAPI(CandidateReq(BuildConfig.VERSION_NAME,userPreferences.getUseID()),AppUtil.getSavedTokenPreference(requireContext()))
    }

    private fun listeners() {


        binding.progressBackButton.setOnClickListener {

            findNavController().navigateUp()
        }


        binding.profileView.viewDetails.setText(R.string.edit)




        binding.voterIdUpload.setOnClickListener {
            showDocumentDialog(voterImage)
        }

        binding.pmaygUpload.setOnClickListener {
            showDocumentDialog(pmaygImage)
        }


        binding.pipUpload.setOnClickListener {
            showDocumentDialog(pipImage)
        }



        binding.dlIdUpload.setOnClickListener {
            showDocumentDialog(dlImage)
        }


        binding.categoryCertificateUpload.setOnClickListener {
            showDocumentDialog(categoryImage)
        }

        binding.minorityImageUpload.setOnClickListener {
            showDocumentDialog(minorityImage)
        }

        binding.pwdImageUpload.setOnClickListener {
            showDocumentDialog(pwdImage)
        }

        binding.nregaCardUpload.setOnClickListener {
            showDocumentDialog(nregaImage)
        }

        binding.shgIdUpload.setOnClickListener {
            showDocumentDialog(shgImage)
        }

        binding.antyodayaCardUpload.setOnClickListener {
            showDocumentDialog(antyodyaImage)
        }


        binding.rsbyUpload.setOnClickListener {
            showDocumentDialog(rsbyImage)
        }


        binding.uploadResidenceImage.setOnClickListener {
            showDocumentDialog(residenceImage)
        }
        binding.profileView.viewDetails.setOnClickListener {

            findNavController().navigate(
                ViewDetailsFragmentDirections.actionViewDetailsFragmentToHomeFragment(),
                NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, true) // Pops all fragments up to HomeFragment
                    .build()
            )
        }




        binding.llTopPersonal.setOnClickListener {

            if (isPersonalVisible){
                isPersonalVisible = false
                binding.personalExpand.visible()
                binding.viewSecc.visible()

            } else {
                isPersonalVisible = true
                binding.personalExpand.gone()
                binding.viewSecc.gone()
            }
        }

        binding.llTopSecc.setOnClickListener {
            if (isSeccInfoVisible) {

                isSeccInfoVisible = false
                binding.expandSecc.visible()
                binding.viewSeccc.visible()



            } else {
                isSeccInfoVisible = true
                binding.expandSecc.gone()
                binding.viewSeccc.gone()
            }

        }

        binding.llTopAddress.setOnClickListener {

            if (isAddressVisible) {
                isAddressVisible = false
                binding.expandAddress.visible()
                binding.viewAddress.visible()
            } else {
                isAddressVisible = true
                binding.expandAddress.gone()
                binding.viewAddress.gone()
            }
        }

        binding.llTopEducational.setOnClickListener {


            if (isEducationalInfoVisible) {
                isEducationalInfoVisible = false
                binding.expandEducational.visible()
                binding.viewEducational.visible()
            } else {
                isEducationalInfoVisible = true
                binding.expandEducational.gone()
                binding.viewEducational.gone()
            }
        }

        binding.llTopEmployment.setOnClickListener {

            if (isEmploymentInfoVisible) {
                isEmploymentInfoVisible = false
                binding.expandEmployment.visible()
                binding.viewEmployment.visible()
            } else {
                isEmploymentInfoVisible = true
                binding.expandEmployment.gone()
                binding.viewEmployment.gone()
            }
        }

        binding.llTopBanking.setOnClickListener {

            if (isBankingInfoVisible ) {
                isBankingInfoVisible = false
                binding.expandBanking.visible()
                binding.viewBanking.visible()
            } else {
                isBankingInfoVisible = true
                binding.expandBanking.gone()
                binding.viewBanking.gone()
            }
        }

        binding.llTopTraining.setOnClickListener {

            if (isTrainingInfoVisible ) {
                isTrainingInfoVisible = false
                binding.expandTraining.visible()
                binding.viewTraining.visible()
            } else {
                isTrainingInfoVisible = true
                binding.expandTraining.gone()
                binding.viewTraining.gone()
            }
        }

    }

    private fun collectAadharDetailsResponse() {
        lifecycleScope.launch {
            collectLatestLifecycleFlow(commonViewModel.getAadhaarList) {
                when (it) {
                    is Resource.Loading -> showProgressBar()
                    is Resource.Error -> {
                        hideProgressBar()
                        it.error?.let { baseErrorResponse ->
                            showSnackBar(baseErrorResponse.message)
                        }
                    }

                    is Resource.Success -> {
                        hideProgressBar()
                        it.data?.let { getAadharDetailsRes ->
                            if (getAadharDetailsRes.responseCode == 200) {
                                userAadhaarDetailsList = getAadharDetailsRes.wrappedList


                                for (x in userAadhaarDetailsList) {
                                    val decryptedUserName = AESCryptography.decryptIntoString(x.userName,
                                        AppConstant.Constants.ENCRYPT_KEY,
                                        AppConstant.Constants.ENCRYPT_IV_KEY)


                                    val decryptedGender = AESCryptography.decryptIntoString(x.gender,
                                        AppConstant.Constants.ENCRYPT_KEY,
                                        AppConstant.Constants.ENCRYPT_IV_KEY)



                                    val decryptedMobileNo = AESCryptography.decryptIntoString(x.mobileNo,
                                        AppConstant.Constants.ENCRYPT_KEY,
                                        AppConstant.Constants.ENCRYPT_IV_KEY)

                                    val decryptedDob = AESCryptography.decryptIntoString(x.dateOfBirth,
                                        AppConstant.Constants.ENCRYPT_KEY,
                                        AppConstant.Constants.ENCRYPT_IV_KEY)

                                    val decryptedAddress = AESCryptography.decryptIntoString(x.comAddress,
                                        AppConstant.Constants.ENCRYPT_KEY,
                                        AppConstant.Constants.ENCRYPT_IV_KEY)


                                    val decryptedEmail = AESCryptography.decryptIntoString(x.emailId,
                                        AppConstant.Constants.ENCRYPT_KEY,
                                        AppConstant.Constants.ENCRYPT_IV_KEY)

                                    binding.profileView.tvAadhaarName.text = decryptedUserName
                                    binding.profileView.tvAaadharMobile.text = decryptedMobileNo
                                    binding.profileView.tvAaadharGender.text = decryptedGender
                                    binding.profileView.tvAaadharDob.text = decryptedDob
                                    binding.profileView.tvAaadharAddress.text = decryptedAddress
                                    binding.profileView.tvEmailMobile.text = decryptedEmail


                                    if (x.imagePath!=null){


                                        val bytes: ByteArray =
                                            Base64.decode(x.imagePath, Base64.DEFAULT)
                                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        binding.profileView.circleImageView.setImageBitmap(bitmap)
                                    }


                                }
                            }
                            else if (getAadharDetailsRes.responseCode == 301) {
                                showSnackBar("Please Update from PlayStore")
                            }
                            else if (getAadharDetailsRes.responseCode == 401) {
                                AppUtil.showSessionExpiredDialog(findNavController(),requireContext())

                            }
                            else {
                                showSnackBar("Something went wrong")
                            }
                        } ?: showSnackBar("Internal Server Error")
                    }
                }
            }
        }
    }

    private fun collectSetionAndPerResponse() {
        lifecycleScope.launch {
            collectLatestLifecycleFlow(commonViewModel.getSecctionAndPerAPI) {
                when (it) {
                    is Resource.Loading -> showProgressBar()
                    is Resource.Error -> {
                        hideProgressBar()
                        it.error?.let { baseErrorResponse ->
                            showSnackBar(baseErrorResponse.message)
                        }
                    }

                    is Resource.Success -> {
                        hideProgressBar()
                        it.data?.let { getSecctionAndPerAPI ->
                            if (getSecctionAndPerAPI.responseCode == 200) {
                                val percentageList = getSecctionAndPerAPI.wrappedList

                                for (x in percentageList) {

                                    personalStatus= x.personalStatus.toString()
                                    educationalStatus= x.educationalStatus.toString()
                                    trainingStatus= x.trainingStatus.toString()
                                    seccStatus= x.seccStatus .toString()
                                    addressStatus= x.addressStatus.toString()
                                    employmentStatus= x.employmentStatus.toString()
                                    bankingStatus= x.bankingStatus.toString()
                                    totalPercentange= x.totalPercentage

                                }
                                // set dynamic meter

                                binding.profileView.ivProgress.setImageBitmap(
                                    createHalfCircleProgressBitmap(
                                        300, 300, totalPercentange,
                                        ContextCompat.getColor(requireContext(), R.color.color_dark_green),
                                        ContextCompat.getColor(requireContext(), R.color.color_green), 35f, 20f,
                                        ContextCompat.getColor(requireContext(), R.color.black),
                                        ContextCompat.getColor(requireContext(), R.color.color_background)
                                    )
                                )

                                // set section completed drawable

                                if (personalStatus.contains("1")){

                                    binding.tvPersonal.setDrawable(
                                        null, null,
                                        AppCompatResources.getDrawable(requireContext(), R.drawable.ic_verified), null
                                    )

                                }
                                if (addressStatus.contains("1")){

                                    binding.tv.setDrawable(
                                        null, null,
                                        AppCompatResources.getDrawable(requireContext(), R.drawable.ic_verified), null
                                    )

                                }
                                if (seccStatus.contains("1")){

                                    binding.tvSecccon.setDrawable(
                                        null, null,
                                        AppCompatResources.getDrawable(requireContext(), R.drawable.ic_verified), null
                                    )

                                }
                                if (educationalStatus.contains("1")){

                                    binding.tvEducational.setDrawable(
                                        null, null,
                                        AppCompatResources.getDrawable(requireContext(), R.drawable.ic_verified), null
                                    )

                                }

                                if (employmentStatus.contains("1")){

                                    binding.tvEmployment.setDrawable(
                                        null, null,
                                        AppCompatResources.getDrawable(requireContext(), R.drawable.ic_verified), null
                                    )

                                }

                                if (trainingStatus.contains("1")){

                                    binding.tvTraining.setDrawable(
                                        null, null,
                                        AppCompatResources.getDrawable(requireContext(), R.drawable.ic_verified), null
                                    )

                                }

                                if (bankingStatus.contains("1")){

                                    binding.tvBanking.setDrawable(
                                        null, null,
                                        AppCompatResources.getDrawable(requireContext(), R.drawable.ic_verified), null
                                    )

                                }


                            } else if (getSecctionAndPerAPI.responseCode == 301) {
                                showSnackBar("Please Update from PlayStore")
                            }  else if (getSecctionAndPerAPI.responseCode == 401) {
                                AppUtil.showSessionExpiredDialog(findNavController(),requireContext())

                            }else {
                                showSnackBar("Something went wrong")
                            }
                        } ?: showSnackBar("Internal Server Error")
                    }
                }
            }
        }
    }

    private fun collectCandidateDetailsResponse() {
        lifecycleScope.launch {
            try {
                collectLatestLifecycleFlow(commonViewModel.getCandidateDetailsAPI) {
                    when (it) {
                        is Resource.Loading -> showProgressBar()
                        is Resource.Error -> {
                            hideProgressBar()
                            it.error?.let { baseErrorResponse ->
                                showSnackBar(baseErrorResponse.message ?: "Unknown error occurred")
                            } ?: showSnackBar("Unknown error occurred")
                        }

                        is Resource.Success -> {
                            hideProgressBar()
                            try {
                                it.data?.let { getCandidateDetailsAPI ->
                                    if (getCandidateDetailsAPI.responseCode == 200) {
                                        // Extract details from the API response
                                        userCandidatePersonalDetailsList = getCandidateDetailsAPI.personalList
                                        userCandidateAddressDetailsList = getCandidateDetailsAPI.addressList
                                        userCandidateSeccDetailsList = getCandidateDetailsAPI.seccList
                                        userCandidateEducationalDetailsList = getCandidateDetailsAPI.educationalList
                                        userCandidateEmploymentDetailsList = getCandidateDetailsAPI.employementList
                                        userCandidateTrainingDetailsList = getCandidateDetailsAPI.trainingList
                                        userCandidateBankDetailsList = getCandidateDetailsAPI.bankList

                                        for (x in userCandidatePersonalDetailsList) {
                                            userCandidatePersonalDetailsList2 = x.personaldetails

                                            try {
                                                // Decode and display images
                                                 categoryImage = x.categoryCertPath

                                                 minorityImage = x.minorityCertPath

                                                 pwdImage = x.disablityCertPath

                                                 dlImage = x.dlImagePath

                                                 nregaImage = x.naregaCardPath
                                                 shgImage = x.shgImage

                                                 rsbyImage = x.rsbyCardPath

                                                voterImage = x.VoterImagePath

                                                pmaygImage = x.pmaygAttachment

                                                pipImage = x.pipCert


                                                antyodyaImage = x.rationCardPath
                                            } catch (e: Exception) {
                                                showSnackBar("Error decoding image: ${e.message}")
                                            }
                                        }

                                        for (x in userCandidatePersonalDetailsList2) {
                                            try {
                                                // Populate the UI
                                                binding.etGName.setText(x.guardianName)
                                                binding.etGNumber.setText(x.guardianMobilNo)
                                                binding.etMotherName.setText(x.motherName)
                                                binding.etFIncome.setText(x.annualFamilyIncome)
                                                binding.etllVoterId.setText(x.voterId)
                                                binding.etdrivingId.setText(x.dlNo)
                                                binding.llCategory.setText(x.castCategory)
                                                binding.llMarital.setText(x.maritalStatus)
                                                binding.etShgValidate.setText(x.shgNo)

                                                val minorityStatus = x.isMinority
                                                val pwdStatus = x.isDisablity
                                                val nregaJobCardStatus = x.isNarega
                                                val shgStatus = x.isSHG
                                                val antyodayaStatus = x.antyodaya
                                                val rsbyStatus = x.isRSBY
                                                val pipStatus = x.isPIP
                                                val pmaygStatus = x.isPmayg

                                                // Set the UI based on conditions
                                                handleStatus(binding.optionMinorityYesSelect, binding.optionMinorityNoSelect, minorityStatus)
                                                handleStatus(binding.optionPwdYesSelect, binding.optionPwdNoSelect, pwdStatus)
                                                handleStatus(binding.optionNregaJobYesSelect, binding.optionNregaJobNoSelect, nregaJobCardStatus)
                                                handleStatus(binding.optionShgYesSelect, binding.optionShgNoSelect, shgStatus)
                                                handleStatus(binding.optionAntyodayaYesSelect, binding.optionAntyodayaNoSelect, antyodayaStatus)
                                                handleStatus(binding.optionllRsbyYesSelect, binding.optionllRsbyNoSelect, rsbyStatus)
                                                handleStatus(binding.optionPipYesSelect, binding.optionPipNoSelect, pipStatus)
                                                handleStatus(binding.optionPmayYesSelect, binding.optionPmayNoSelect, pmaygStatus)


                                                if (nregaJobCardStatus=="No"){

                                                    binding.nregaJobCardView.gone()
                                                    binding.nregaCardUpload.gone()


                                                }
                                                if (shgStatus=="No"){

                                                    binding.etShgValidate.gone()

                                                }

                                                if (pmaygStatus=="No"){

                                                    binding.pmaygUpload.gone()

                                                }

                                                if (pipStatus=="No"){

                                                    binding.pipUpload.gone()

                                                }

                                            } catch (e: Exception) {
                                                showSnackBar("Error setting data: ${e.message}")
                                            }
                                        }

                                        for (x in userCandidateAddressDetailsList ){

                                            userCandidateAddressDetailsList2=   x.addressDetails

                                            try {

                                                residenceImage = x.residenceCertPath

                                            } catch (e: Exception) {
                                                showSnackBar("Error decoding image: ${e.message}")
                                            }


                                        }


                                        for (x in userCandidateAddressDetailsList2){






                                            if (x.permanentLocality == "URBAN") {
                                                binding.lluw.visibility = View.VISIBLE
                                                binding.llbgv.visibility = View.GONE
                                            } else {
                                                binding.lluw.visibility = View.GONE
                                                binding.llbgv.visibility = View.VISIBLE
                                            }


                                            val constraintSet1 = ConstraintSet()
                                            constraintSet1.clone(binding.expandAddress) // Must be the parent ConstraintLayout

                                            constraintSet1.clear(binding.llAdressLine.id, ConstraintSet.TOP)

                                            val targetId1 = if (x.permanentLocality == "URBAN") {
                                                binding.lluw.id
                                            } else {
                                                binding.llbgv.id
                                            }

                                            constraintSet1.connect(
                                                binding.llAdressLine.id,
                                                ConstraintSet.TOP,
                                                targetId1,
                                                ConstraintSet.BOTTOM
                                            )

                                            constraintSet1.connect(
                                                binding.llAdressLine.id,
                                                ConstraintSet.START,
                                                ConstraintSet.PARENT_ID,
                                                ConstraintSet.START
                                            )
                                            constraintSet1.connect(
                                                binding.llAdressLine.id,
                                                ConstraintSet.END,
                                                ConstraintSet.PARENT_ID,
                                                ConstraintSet.END
                                            )

                                            TransitionManager.beginDelayedTransition(binding.expandAddress)
                                            constraintSet1.applyTo(binding.expandAddress)




                                            // 1. Update visibility
                                            if (x.presentLocality == "URBAN") {
                                                binding.llPresentAddressuw.visibility = View.VISIBLE
                                                binding.llPresentAddressbgv.visibility = View.GONE
                                            }
                                            else {
                                                binding.llPresentAddressuw.visibility = View.GONE
                                                binding.llPresentAddressbgv.visibility = View.VISIBLE
                                            }

                                            val constraintSet = ConstraintSet()
                                            constraintSet.clone(binding.expandAddress) // Make sure all involved views are children of expandAddress

                                            constraintSet.clear(binding.llPresentAddressAdressLine.id, ConstraintSet.TOP)

                                            val targetId = if (x.presentLocality == "URBAN") {
                                                binding.llPresentAddressuw.id
                                            }
                                            else {
                                                binding.llPresentAddressbgv.id
                                            }

                                            constraintSet.connect(
                                                binding.llPresentAddressAdressLine.id,
                                                ConstraintSet.TOP,
                                                targetId,
                                                ConstraintSet.BOTTOM
                                            )

                                            constraintSet.connect(
                                                binding.llPresentAddressAdressLine.id,
                                                ConstraintSet.START,
                                                ConstraintSet.PARENT_ID,
                                                ConstraintSet.START
                                            )
                                            constraintSet.connect(
                                                binding.llPresentAddressAdressLine.id,
                                                ConstraintSet.END,
                                                ConstraintSet.PARENT_ID,
                                                ConstraintSet.END
                                            )

                                            TransitionManager.beginDelayedTransition(binding.expandAddress)
                                            constraintSet.applyTo(binding.expandAddress)






                                            binding.statespinn.setText(x.permanentStateName)
                                            binding.etDist.setText(x.permanentDistrictName)
                                            binding.etBlock.setText(x.permanentBlockName)
                                            binding.etGp.setText(x.permanentGPName)
                                            binding.etVillage.setText(x.permanentVillageName)
                                            binding.etAdressLine.setText(x.permanentStreet1)
                                            binding.etAdressLine2.setText(x.permanentStreet2)
                                            binding.etPinCode.setText(x.permanentPinCode)

                                            binding.etUlb.setText(x.permanentulbName)
                                            binding.etWard.setText(x.permanentWardName)
                                            binding.etType.setText(x.permanentLocality)

                                            binding.etPrUlb.setText(x.presentUlbName)
                                            binding.etPrWard.setText(x.presentWardName)
                                            binding.etPrType.setText(x.presentLocality)


                                            val adreessStatus = x.isPresentAddressSame

                                            binding.etPrState.setText(x.presentStateName)
                                            binding.etPrDist.setText(x.presentDistrictName)
                                            binding.etPrBlock.setText(x.presentBlockName)
                                            binding.etPrGp.setText(x.presentGPName)
                                            binding.etPrVillage.setText(x.presentVillageName)
                                            binding.etPresentAddressAdressLine.setText(x.presentStreet1)
                                            binding.etPresentLine2.setText(x.presentStreet2)
                                            binding.etPresentPinCode.setText(x.presentPinCode)




                                            handleStatus(binding.optionllSamePermanentYesSelect, binding.optionSamePermanentNoSelect, adreessStatus)





                                        }

                                        for (x in userCandidateSeccDetailsList){

                                            binding.stateesecc.setText(x.seccStateName)
                                            binding.etSeccDistrict.setText(x.seccDistrictName)
                                            binding.etBloch.setText(x.seccBlockName)
                                            binding.etSeccgp.setText(x.seccGPName)
                                            binding.etSeccVillage.setText(x.seccVillageName)
                                            binding.etATINNo.setText(x.seccAHLTIN)



                                        }

                                        for (x in userCandidateEducationalDetailsList ){

                                            binding.ethighestEdu.setText(x.highesteducation)
                                            binding.tvClickYearOfPassing.setText(x.monthYearOfPassing)
                                            binding.tvLanguages.setText(x.language)
                                            binding.etTechEduc.setText(x.techQualification)
                                            binding.etTechDomain.setText(x.techDomain)
                                            binding.tvClickYearOfPassingTech.setText(x.passingTechYear)
                                        }

                                        for (x in userCandidateEmploymentDetailsList ){

                                            val currentluempo = x.isEmployeed
                                            val natureEmp = x.empNature

                                            if (natureEmp.contains("Self Employed")){
                                                binding.optionnatureOfEmplYesSelect.setBackgroundResource(R.drawable.card_background_selected)
                                                binding.optionnatureOfEmpldNoSelect.setBackgroundResource(R.drawable.card_background)
                                            }
                                            else if (natureEmp.contains("Salary")){

                                                binding.optionnatureOfEmpldNoSelect.setBackgroundResource(R.drawable.card_background_selected)
                                                binding.optionnatureOfEmplYesSelect.setBackgroundResource(R.drawable.card_background)
                                            }

                                            handleStatus(binding.optionCurentlyEmployedYesSelect, binding.optionCurentlyEmployedNoSelect, currentluempo)


                                            binding.etIntrestedIn.setText(x.intrestedIn)
                                            binding.etEmploPref.setText(x.empPreference)
                                            binding.etJobLoc.setText(x.preferJobLocation)
                                            binding.etCurrentEarning.setText(x.monthlyEarning)
                                            binding.etExpectationSalary.setText(x.expectedSalary)


                                        }

                                        for (x in userCandidateTrainingDetailsList){

                                            val recievedTrainingBeforeStatus = x.isPreTraining
                                            val heardStatus = x.hearedAboutScheme

                                            // Set the UI based on conditions
                                            handleStatus(binding.optionrecievedAnyTrainingBeforeYesSelect, binding.optioRecievedAnyTrainingBeforeNoSelect, recievedTrainingBeforeStatus)
                                            handleStatus(binding.optionHaveYouHeardYes, binding.optionHaveYouHeardNo, heardStatus)

                                            binding.etPrevComTra.setText(x.preCompTraining)
                                            binding.etWhereHaveHeard.setText(x.hearedFrom)
                                            binding.etSector.setText(x.sectorName)
                                            binding.etTrade.setText(x.trade)


                                            if (heardStatus=="No"){


                                                binding.etWhereHaveHeard.gone()

                                            }


                                            if (recievedTrainingBeforeStatus=="No"){


                                                binding.etPrevComTra.gone()

                                            }

                                        }


                                        for (x in userCandidateBankDetailsList){


                                            val DecIfscCode = AESCryptography.decryptIntoString(x.ifscCode,AppConstant.Constants.ENCRYPT_KEY,AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val DecBankName = AESCryptography.decryptIntoString(x.bankName,AppConstant.Constants.ENCRYPT_KEY,AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val DecbankBranchName = AESCryptography.decryptIntoString(x.bankBranchName,AppConstant.Constants.ENCRYPT_KEY,AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val DecbankbankAccNumber = AESCryptography.decryptIntoString(x.bankAccNumber,AppConstant.Constants.ENCRYPT_KEY,AppConstant.Constants.ENCRYPT_IV_KEY)
                                            val DecpanNo = AESCryptography.decryptIntoString(x.panNo,AppConstant.Constants.ENCRYPT_KEY,AppConstant.Constants.ENCRYPT_IV_KEY)

                                            val maskedAccountNo= maskString(DecbankbankAccNumber)


                                            binding.etIfsc.setText(DecIfscCode)
                                            binding.etBankName.setText(DecBankName)
                                            binding.etBranchName.setText(DecbankBranchName)
                                            binding.etBankAcNo.setText(maskedAccountNo)
                                            binding.etPanNumber.setText(DecpanNo)
                                        }

                                    } else if (getCandidateDetailsAPI.responseCode == 301) {
                                        showSnackBar("Please Update from PlayStore")
                                    }
                                    else if (getCandidateDetailsAPI.responseCode==401){
                                        AppUtil.showSessionExpiredDialog(findNavController(),requireContext())
                                    }else {
                                        showSnackBar("Something went wrong")
                                    }
                                } ?: showSnackBar("Internal Server Error")
                            } catch (e: Exception) {
                                showSnackBar("Error processing response: ${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                hideProgressBar()
                showSnackBar("Unexpected error occurred: ${e.message}")
            }
        }
    }

    // Helper function to handle status UI
    private fun handleStatus(viewYes: View, viewNo: View, status: String) {
        if (status.contains("Yes", ignoreCase = true)) {
            viewYes.setBackgroundResource(R.drawable.card_background_selected)
            viewNo.setBackgroundResource(R.drawable.card_background)
        } else if (status.contains("No", ignoreCase = true)) {
            viewYes.setBackgroundResource(R.drawable.card_background)
            viewNo.setBackgroundResource(R.drawable.card_background_selected)
        }
    }


    private fun decodeBase64Image(base64String: String?): Pair<Bitmap?, String?> {
        if (base64String.isNullOrEmpty()) return Pair(null, null)

        return try {
            val cleanedBase64String = base64String.substringAfter(",")
            val bytes = Base64.decode(cleanedBase64String, Base64.DEFAULT)

            // Check file type (PDF or Image)
            val fileType = getFileType(bytes)

            if (fileType == "pdf") {
                return Pair(null, "pdf")
            } else {
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                Pair(bitmap, "image")
            }
        } catch (e: IllegalArgumentException) {
            Log.e("DecodeError", "Invalid Base64 string: ${e.message}")
            Pair(null, null)
        }
    }

    // Function to determine file type
    private fun getFileType(bytes: ByteArray): String? {
        return when {
            bytes.isNotEmpty() && bytes.size > 4 -> {
                val header = bytes.copyOfRange(0, 4).joinToString("") { "%02X".format(it) }
                when {
                        header.startsWith("25504446") -> "pdf"  // PDF file starts with "%PDF"
                        header.startsWith("FFD8FF") -> "image"  // JPEG
                        header.startsWith("89504E47") -> "image" // PNG
                    else -> null
                }
            }
            else -> null
        }
    }
    // Show Image or Download PDF
    private fun showDocumentDialog(base64String: String?) {
        val (bitmap, fileType) = decodeBase64Image(base64String)

        if (fileType == "pdf") {
            downloadPdf(base64String)  // 🔹 Download instead of showing PDF
        } else {
            showImageDialog(bitmap)
        }
    }
    private fun downloadPdf(base64String: String?) {
        if (base64String.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Invalid PDF data", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val fileName = "downloaded_document.pdf"
            val resolver = requireContext().contentResolver

            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val pdfFile = File(downloadsDir, fileName)
                Uri.fromFile(pdfFile)
            }

            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(decodedBytes)
                    outputStream.flush()
                }
                Toast.makeText(requireContext(), "PDF saved successfully!", Toast.LENGTH_LONG).show()
                openDownloadedPdf(it)
            } ?: throw Exception("Failed to create file")

        } catch (e: Exception) {
            Log.e("PDF_ERROR", "Error Saving PDF: ${e.message}")
            Toast.makeText(requireContext(), "Failed to save PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openDownloadedPdf(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No PDF viewer found", Toast.LENGTH_SHORT).show()
        }
    }


    // Show Image Dialog
    private fun showImageDialog(image: Bitmap?) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.layout_view_image)

        val imageView = dialog.findViewById<ImageView>(R.id.imageViewDialog)
        val closeButton = dialog.findViewById<TextView>(R.id.btnClose)

        if (image == null) {
            imageView.setImageResource(R.drawable.no_image)
        } else {
            imageView.setImageBitmap(image)
        }

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    fun maskString(input: String): String {
        return if (input.length <= 4) {
            // If the string is 4 or fewer characters, return it as is
            input
        } else {
            // Mask all but the last 4 characters with '*'
            val maskedPart = "*".repeat(input.length - 4)
            val lastFour = input.takeLast(4)
            return maskedPart + lastFour
        }
    }
}
