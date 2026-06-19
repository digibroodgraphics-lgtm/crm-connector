package com.digibrood.crmconnector.data.remote.dto

import com.squareup.moshi.Json

/** POST /auth/login request body. */
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

/** POST /auth/login response body. */
data class LoginResponse(
    @Json(name = "access_token") val accessToken: String?,
    @Json(name = "refresh_token") val refreshToken: String?,
    @Json(name = "token_type") val tokenType: String? = "Bearer",
    @Json(name = "expires_in") val expiresIn: Long? = null,
    @Json(name = "device_status") val deviceStatus: String? = null,
    @Json(name = "registered_number") val registeredNumber: String? = null,
    @Json(name = "activated_at") val activatedAt: String? = null
)

/** POST /auth/refresh request body. */
data class RefreshRequest(
    @Json(name = "refresh_token") val refreshToken: String
)

/** POST /auth/refresh response body. */
data class RefreshResponse(
    @Json(name = "access_token") val accessToken: String?,
    @Json(name = "refresh_token") val refreshToken: String?,
    @Json(name = "expires_in") val expiresIn: Long? = null
)

/**
 * Standard error envelope returned by the CRM for non-2xx responses.
 * The [errorCode] field carries machine-readable values such as
 * APP_LOGIN_DISABLED and INVALID_CREDENTIALS. The DIGIBROOD CRM returns this in
 * a field named "code".
 */
data class ApiError(
    @Json(name = "code") val errorCode: String? = null,
    @Json(name = "message") val message: String? = null
)
