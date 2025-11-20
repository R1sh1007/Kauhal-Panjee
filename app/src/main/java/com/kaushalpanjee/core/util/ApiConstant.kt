package com.kaushalpanjee.core.util

object ApiConstant {


    /**
     * BASE_URL_DEV="https://nrlm.gov.in/"
     * BASE_URL_STAGE="https://nrlm.gov.in/"
     * BASE_URL_PROD=""
     *
     */

    const val CONNECT_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 10L
    const val READ_TIMEOUT = 60L

    const val DEVICE_TYPE = 2 // android , 1-> ios
    const val DEVICE_TYPE_NAME = "android"



    private const val API_VERSION = "v1/"

    private const val API_FOLDER_BACKEND = "kaushalpanjee/"
    private const val API_FOLDER_CITIZEN = "panjeeapi/"
     const val  API_SMS_OTP= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}generateMobileOtp"
     const val  API_CREATE_USER= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}createUser"
     const val  API_EMAIL_OTP= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}generateMailOtp"
     const val  API_STATE= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}stateList"
     const val  API_DISTRICT= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}districtList"
     const val  API_BLOCK= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}blockList"
     const val  API_GP= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}gramPanchayatList"
     const val  API_VILLAGE= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}villageList"
     const val  API_TECH_EDUCATION= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}technicalQualification"
     const val  API_TECH_EDUCATION_DOMAIN= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}techDomain"
     const val  API_WHERE_HAVE_U_HEARD= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}surveyList"
     const val  API_AADHAAR_DETAILS= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}userDetails"
     const val  API_SECC_DETAILS= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}seccDetails"
     const val  API_SECTION_PERCENTAGE= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}sectionStatus"
     const val  API_INSERT= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}insertDetails"
     const val  BANK_DETAILS= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}bankDetails"
     const val  LANGUAGE_LIST= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}languageList"
     const val  API_LOGIN= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}login"
     const val  API_SECTOR= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}sectorList"
     const val  API_TRADE= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}tradeList"
     const val  API_TRAINING_SEARCH= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}searchCenter"
     const val  API_TRAINING_LIST= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}trainingCenter"
    const val  API_GET_SEARCH_TRAINING= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}getCenter"
    const val  API_CANDIDATE_DETAILS= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}candidateDetails"
    const val  API_CHANGE_IMAGE_CANDIDATE= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}changeProfilePhoto"
    const val  API_CHANGE_PASSWORD= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}changePassword"
    const val  Forgot_PASSWORD_OTP= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}forgetIdPassword"
    const val  GET_LOGINID_PASS= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}getLoginIdPassword"
    const val  GET_TOKEN_GENERATE= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}generateToken"
    const val  API_OTP_VALIDATE= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}validateOtp"
    const val  API_OTP_checkUserExistance= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}checkUserExistance"
    const val  API_OTP_getBanner= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}getBanner"
    const val  API_UPDATE_FACE= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}updateFaceRegistred"
    const val  API_UPDATE_Aadhaar= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}updateAdhaarDetails"
    const val  API_Aadhaar_FORGOT= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}forgetIdPassword"
    const val  API_Aadhaar_FORGOT_Update= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}updatePassword"
    const val  API_Update_CANDIDATE_EMAIL= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}updateCandidateEmailId"
    const val  API_ULB= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}ulbList"
    const val  API_WARD= "${API_FOLDER_BACKEND}${API_FOLDER_CITIZEN}wardList"

}