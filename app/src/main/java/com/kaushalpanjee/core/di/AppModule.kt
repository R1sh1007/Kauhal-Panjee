package com.kaushalpanjee.core.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.kaushalpanjee.BuildConfig
import com.kaushalpanjee.core.data.local.database.AppDatabase
import com.kaushalpanjee.core.data.remote.AppLevelApi
import com.kaushalpanjee.core.util.ApiConstant
import com.kaushalpanjee.core.util.AppConstant
import com.kaushalpanjee.core.util.CustomInterceptor
import com.kaushalpanjee.core.util.NetworkLoggingInterceptor
import com.kaushalpanjee.core.util.UserPreferences

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class PreLoginOkHttpClient

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class PostLoginOkHttpClient

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class PreLoginAppLevelApi

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class PostLoginAppLevelApi

    @Provides
    @Singleton
    fun providesRoomDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, "Kaushal_panjee_db"
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
    }

    @Provides
    @PreLoginOkHttpClient
    fun providesRetrofitForPreLogin(
        userPreferences: UserPreferences,
        @ApplicationContext context: Context
    ): Retrofit {
        return Retrofit.Builder()
         .baseUrl(AppConstant.StaticURL.baseUrl)
        //.baseUrl(AppConstant.StaticURL.localUrl)
            .client(
                getRetrofitClient(
                    null, userPreferences = userPreferences,
                    isPostLogin = false, context = context
                )
            )
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @PostLoginOkHttpClient
    fun providesRetrofitForPostLogin(
        userPreferences: UserPreferences,
        @ApplicationContext context: Context
    ): Retrofit {
        return Retrofit.Builder()
           .baseUrl(AppConstant.StaticURL.baseUrl)
          // .baseUrl(AppConstant.StaticURL.localUrl)
            .client(getRetrofitClient(null, userPreferences = userPreferences, context = context))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private fun getRetrofitClient(
        authenticator: Authenticator? = null,
        userPreferences: UserPreferences,
        isPostLogin: Boolean = true,
        isAuthenticationRequired: Boolean = true,
        context: Context
    ): OkHttpClient {
        val cacheSize = (5 * 1024 * 1024).toLong()
        val myCache = Cache(context.cacheDir, cacheSize)
        return OkHttpClient.Builder().apply {
            cache(myCache)
            connectTimeout(ApiConstant.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(ApiConstant.WRITE_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(ApiConstant.READ_TIMEOUT, TimeUnit.SECONDS)
            if (BuildConfig.DEBUG) {
                addNetworkInterceptor(ChuckerInterceptor(context))
                val logging = HttpLoggingInterceptor { message ->
                    Log.d("API_LOG---->", message)
                }.apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                addNetworkInterceptor(logging)
            }
            addInterceptor(CustomInterceptor(isPostLogin, userPreferences, isAuthenticationRequired, context))
            authenticator?.let { authenticator(it) }
        }.build()
    }


    @Provides
    @PreLoginAppLevelApi
    fun providePreLoginAppLevelApi(@PreLoginOkHttpClient retrofit: Retrofit) : AppLevelApi = retrofit.create(
        AppLevelApi::class.java)

    @Provides
    @PostLoginAppLevelApi
    fun providePostLoginAppLevelApi(@PostLoginOkHttpClient retrofit: Retrofit) : AppLevelApi = retrofit.create(
        AppLevelApi::class.java)


}