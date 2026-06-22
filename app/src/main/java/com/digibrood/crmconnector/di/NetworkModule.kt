package com.digibrood.crmconnector.di

import com.digibrood.crmconnector.BuildConfig
import com.digibrood.crmconnector.data.remote.api.CrmApiService
import com.digibrood.crmconnector.data.remote.interceptor.AuthInterceptor
import com.digibrood.crmconnector.data.remote.interceptor.DynamicBaseUrlInterceptor
import com.digibrood.crmconnector.data.remote.interceptor.HttpsEnforcementInterceptor
import com.digibrood.crmconnector.data.remote.interceptor.SessionGuardInterceptor
import com.digibrood.crmconnector.data.remote.interceptor.TokenAuthenticator
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Provides Moshi, the OkHttp clients and the Retrofit-backed [CrmApiService].
 *
 * Three OkHttp clients exist for distinct purposes:
 *  - [MainClient]    : authenticated CRM calls (dynamic base + JWT + auto-refresh)
 *  - [RefreshClient] : token refresh only (no authenticator -> no recursion)
 *  - [UploadClient]  : direct presigned uploads to R2 (no CRM interceptors)
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(com.digibrood.crmconnector.data.remote.dto.WhitelistItemAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    @MainClient
    fun provideMainOkHttpClient(
        https: HttpsEnforcementInterceptor,
        dynamicBaseUrl: DynamicBaseUrlInterceptor,
        auth: AuthInterceptor,
        sessionGuard: SessionGuardInterceptor,
        authenticator: TokenAuthenticator,
        logging: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(https)
        .addInterceptor(dynamicBaseUrl)
        .addInterceptor(auth)
        .addInterceptor(sessionGuard)
        .addInterceptor(logging)
        .authenticator(authenticator)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    @Provides
    @Singleton
    @RefreshClient
    fun provideRefreshOkHttpClient(
        https: HttpsEnforcementInterceptor,
        dynamicBaseUrl: DynamicBaseUrlInterceptor,
        logging: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(https)
        .addInterceptor(dynamicBaseUrl)
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @UploadClient
    fun provideUploadOkHttpClient(
        https: HttpsEnforcementInterceptor,
        logging: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(https)
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .retryOnConnectionFailure(true)
        .build()

    @Provides
    @Singleton
    @MainClient
    fun provideMainRetrofit(
        @MainClient client: OkHttpClient,
        moshi: Moshi
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.DEFAULT_BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideCrmApiService(@MainClient retrofit: Retrofit): CrmApiService =
        retrofit.create(CrmApiService::class.java)

    @Provides
    @Singleton
    @RefreshClient
    fun provideRefreshRetrofit(
        @RefreshClient client: OkHttpClient,
        moshi: Moshi
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.DEFAULT_BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    @RefreshClient
    fun provideRefreshApiService(@RefreshClient retrofit: Retrofit): CrmApiService =
        retrofit.create(CrmApiService::class.java)
}
