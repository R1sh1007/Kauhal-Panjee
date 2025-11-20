package com.kaushalpanjee.core.util

import com.kaushalpanjee.security.SecurityUtils



object AppConstant {

    object BundleConstant{
        const val EXTRA_BUNDLE = "EXTRA_BUNDLE"
        const val HEADING = "HEADING"
    }
    object StaticURL{
        const val BUCKET_PROFILE= "profile/"
        const val BUCKET_DOCUMENTS= "documents/"
        const val BUCKET_CATEGORY= "category/"
       // const val FACE_AUTH_UIADI= "https://nregarep2.nic.in/Netnrega/StateServices/Uid_Face_Auth.svc/PostOnAUA_Face_auth"
        const val FACE_AUTH_UIADI= "https://nregarep2.nic.in/uid/stateservices/Uid_Face_Auth_DDUGKY.svc/PostOnAUA_Face_auth"
       //const val baseUrl= "http://10.197.183.177:8001/"                        //local
        const val baseUrl= "https://kaushal.rural.gov.in/backend/"             //live
     // const val baseUrl= "https://kaushal.rural.gov.in/demobackend/"             //demo
      //  const val baseUrl= "https://kaushal.dord.gov.in/demobackend/"             //demo

    }

    object Constants{
        //AADHAAR CONST
        const val CAPTURE_INTENT = "in.gov.uidai.rdservice.face.CAPTURE"
        const val DEVICE_CHECK_INTENT = "in.gov.uidai.rdservice.face.CHECK_DEVICE"
        const val CAPTURE_INTENT_REQUEST = "request"
        const val CAPTURE_INTENT_RESULT = "in.gov.uidai.rdservice.face.CAPTURE_RESULT"
        const val CAPTURE_INTENT_RESPONSE_DATA = "response"
        const val DEVICE_CHECK_INTENT_RESULT = "in.gov.uidai.rdservice.face.CHECK_DEVICE_RESULT"


        var ENVIRONMENT_TAG = "P"//"S"
        var LANGUAGE = "en"
        var ENCRYPT_IV_KEY = SecurityUtils.getEncryptIvKey()
        var ENCRYPT_KEY = SecurityUtils.getEncryptKey()

        const val PRE_PRODUCTION: String = "preProduction"
        const val PRODUCTION: String = "P"
        const val ENGLISH: String = "english"
        const val STAGING_CODE: String = "S"
        const val PRE_PRODUCTION_CODE: String = "PP"
        const val CURRENT_ENVIRONMENT_CODE = PRE_PRODUCTION_CODE;
        const val PRODUCTION_CODE: String = "P"

        const val EXTRA_CLIENT_ID = "client_id"
        const val EXTRA_FETCH_USER_EMBEDDING = "fetch_user_embeddings"
        const val EXTRA_CALL_TYPE = "call_type"
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_USER_IDS = "user_ids_json"
        const val EXTRA_USER_NAME = "user_name"
        const val CALL_TYPE_LOGIN = "LOGIN"
        const val CALL_TYPE_REGISTRATION = "REGISTRATION"
        const val RESULT_STATUS = "status"
        const val RESULT_MESSAGE = "message"
        const val YOUR_CLIENT_ID = "RD_0725"

    }
}
