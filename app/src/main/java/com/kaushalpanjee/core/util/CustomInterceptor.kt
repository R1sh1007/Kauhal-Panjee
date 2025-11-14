package com.kaushalpanjee.core.util

import android.content.Context
import android.os.Build
import com.google.gson.Gson
import com.kaushalpanjee.BuildConfig
import com.utilize.core.domain.model.response.RefreshTokenResponse
import com.kaushalpanjee.core.util.AppUtil.getTimeZone
import com.kaushalpanjee.security.SecurityUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import javax.inject.Inject


const val CODE_ACCESS_TOKEN_EXPIRED = 498
const val CODE_ACCESS_TOKEN_BLACKLISTED = 403
const val CODE_USER_DELETED = 401

class CustomInterceptor @Inject constructor(
    private val isPostLogin: Boolean = true,
    private val userPreferences: UserPreferences,
    private val isAuthenticationRequired: Boolean,
    private val context: Context? = null

) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        lateinit var response: Response
        val request = chain.request()

        response = if (isAuthenticationRequired)
            if (request.url.toUrl().toString().contains("X-Amz-Algorithm"))
                chain.proceed(request.newBuilder().build())
            else
                chain.proceed(addHeaderInRequest(request.newBuilder(), request.method).build())
        else chain.proceed(request.newBuilder().build())


        if (response.code == CODE_ACCESS_TOKEN_EXPIRED) {
            synchronized(this) {
                log("CustomInterceptor", "intercept: Auth token expired, refreshing")
                response.close()
                val requestBody =
                    JSONObject().accumulate("refreshToken", userPreferences.getRefreshToken())
                       // .accumulate("accessToken", userPreferences.getAccessToken())

                val requestBuilder = Request.Builder()
                requestBuilder.addHeader("Accept", "application/json")
                requestBuilder.addHeader("timezone", getTimeZone())
                requestBuilder.addHeader(
                    "user-agent",
                    "App/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE})"
                )
               // requestBuilder.addHeader("client_secret", BuildConfig.CLIENT_SECRET_KEY)
                requestBuilder.addHeader("client_secret", SecurityUtils.getClientSecretKey())
                //requestBuilder.url(BuildConfig.REFRESH_TOKEN_URL)
                requestBuilder.url(SecurityUtils.getRefreshTokenUrl())

                requestBuilder.post(
                    RequestBody.create(
                        "application/json".toMediaTypeOrNull(),
                        requestBody.toString()
                    )
                )
                val refreshTokenResponse = chain.proceed(requestBuilder.build())

                if (refreshTokenResponse.code == 200) {

                    log("CustomInterceptor", "intercept: Successfully refreshed Auth token")
                    val refreshTokenResponseData = Gson().fromJson(
                        refreshTokenResponse.body?.string(),
                        RefreshTokenResponse::class.java
                    )

                    runBlocking {
                        refreshTokenResponseData?.data?.let {
                            userPreferences.saveAccessToken(it.accessToken)
                            userPreferences.saveRefreshToken(it.refreshToken)
                            delay(1500)
                        }

                        response = chain.proceed(addHeaderInRequest(request.newBuilder(),request.method ).build())
                    }
                } else {
                    log(
                        "CustomInterceptor",
                        "intercept: logging out due failure to refresh token, HTTP Status code: ${refreshTokenResponse.code}"
                    )
                    logout()
                }
            }
        }

        else if (response.code == CODE_ACCESS_TOKEN_BLACKLISTED || response.code == CODE_USER_DELETED) {

            if (response.code == CODE_ACCESS_TOKEN_BLACKLISTED){
                log(
                    "CustomInterceptor",
                    "intercept: logging out due to HTTP Status code: $CODE_ACCESS_TOKEN_BLACKLISTED"
                )
            }else{
                log(
                    "CustomInterceptor",
                    "intercept: logging out due to HTTP Status code: $CODE_USER_DELETED"
                )
            }

            logout()
        }

        return response
    }

    private fun logout() {
        runBlocking {
            context?.let {
                userPreferences.logout(it)
            }
        }

    }

    private fun addHeaderInRequest(reqBuilder: Request.Builder, method: String): Request.Builder {
        log("AccessToken", userPreferences.getAccessToken())
        log("Method: ", method)
        reqBuilder.addHeader("Accept", "application/json")
        reqBuilder.addHeader(
            "user-agent",
            "App/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE})"
        )
        reqBuilder.addHeader("timezone", getTimeZone())
        if (isPostLogin) {
            reqBuilder.addHeader("authorization", userPreferences.getAccessToken())
        } else {
            //reqBuilder.addHeader("client_secret", BuildConfig.CLIENT_SECRET_KEY)
            reqBuilder.addHeader("client_secret", SecurityUtils.getClientSecretKey())
        }

    /*    if (method == "GET") {
            if (AppUtil.isNetworkAvailable(context))
                reqBuilder.addHeader("Cache-Control", "public, max-age=" + 5).build()
            else
                reqBuilder.addHeader(
                    "Cache-Control",
                    "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7
                ).build()
        }*/

        return reqBuilder
    }

}