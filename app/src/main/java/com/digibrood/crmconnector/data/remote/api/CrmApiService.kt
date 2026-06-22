package com.digibrood.crmconnector.data.remote.api

import com.digibrood.crmconnector.data.remote.dto.CallSyncRequest
import com.digibrood.crmconnector.data.remote.dto.CallSyncResponse
import com.digibrood.crmconnector.data.remote.dto.ChangeNumberRequest
import com.digibrood.crmconnector.data.remote.dto.ConfirmRequest
import com.digibrood.crmconnector.data.remote.dto.ConfirmResponse
import com.digibrood.crmconnector.data.remote.dto.ContactLookupResponse
import com.digibrood.crmconnector.data.remote.dto.DeviceStatusResponse
import com.digibrood.crmconnector.data.remote.dto.HeartbeatRequest
import com.digibrood.crmconnector.data.remote.dto.HeartbeatResponse
import com.digibrood.crmconnector.data.remote.dto.LoginRequest
import com.digibrood.crmconnector.data.remote.dto.LoginResponse
import com.digibrood.crmconnector.data.remote.dto.PresignRequest
import com.digibrood.crmconnector.data.remote.dto.PresignResponse
import com.digibrood.crmconnector.data.remote.dto.RefreshRequest
import com.digibrood.crmconnector.data.remote.dto.RefreshResponse
import com.digibrood.crmconnector.data.remote.dto.RegisterDeviceRequest
import com.digibrood.crmconnector.data.remote.dto.RegisterDeviceResponse
import com.digibrood.crmconnector.data.remote.dto.RemarkRequest
import com.digibrood.crmconnector.data.remote.dto.RemarkResponse
import com.digibrood.crmconnector.data.remote.dto.SettingsResponse
import com.digibrood.crmconnector.data.remote.dto.StatsResponse
import com.digibrood.crmconnector.data.remote.dto.BrandingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit definition of the CRM mobile API.
 *
 * Base URL is https://<crm-origin>/api/mobile/v1/ — the origin is swapped at
 * runtime by [com.digibrood.crmconnector.data.remote.interceptor.DynamicBaseUrlInterceptor]
 * based on the CRM URL the user enters at login. Endpoint paths below match the
 * documented API contract exactly.
 */
interface CrmApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): Response<RefreshResponse>

    @POST("device/register")
    suspend fun registerDevice(@Body body: RegisterDeviceRequest): Response<RegisterDeviceResponse>

    @POST("device/change-number")
    suspend fun changeNumber(@Body body: ChangeNumberRequest): Response<RegisterDeviceResponse>

    @GET("device/status")
    suspend fun getDeviceStatus(@Query("device_id") deviceId: String): Response<DeviceStatusResponse>

    @POST("heartbeat")
    suspend fun heartbeat(@Body body: HeartbeatRequest): Response<HeartbeatResponse>

    @POST("calls/sync")
    suspend fun syncCalls(@Body body: CallSyncRequest): Response<CallSyncResponse>

    @GET("contacts/lookup")
    suspend fun lookupContact(@Query("phone") phoneNumber: String): Response<ContactLookupResponse>

    @POST("calls/remark")
    suspend fun saveRemark(@Body body: RemarkRequest): Response<RemarkResponse>

    @POST("recordings/presign")
    suspend fun presignRecording(@Body body: PresignRequest): Response<PresignResponse>

    @POST("recordings/confirm")
    suspend fun confirmRecording(@Body body: ConfirmRequest): Response<ConfirmResponse>

    @GET("recordings/trace")
    suspend fun traceRecording(
        @Query("client_call_id") clientCallId: String
    ): Response<com.digibrood.crmconnector.data.remote.dto.RecordingTraceResponse>

    @GET("settings")
    suspend fun getSettings(): Response<SettingsResponse>

    @GET("meta")
    suspend fun getMeta(): Response<com.digibrood.crmconnector.data.remote.dto.MetaResponse>

    @GET("branding")
    suspend fun getBranding(): Response<BrandingResponse>

    @GET("stats")
    suspend fun getStats(): Response<StatsResponse>

    @POST("whitelist/propose")
    suspend fun proposeWhitelist(
        @Body body: com.digibrood.crmconnector.data.remote.dto.WhitelistProposeRequest
    ): Response<com.digibrood.crmconnector.data.remote.dto.WhitelistProposeResponse>
}
