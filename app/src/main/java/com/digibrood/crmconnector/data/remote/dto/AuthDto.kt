package com.digibrood.crmconnector.data.remote.dto

import com.squareup.moshi.Json

/** POST /auth/login request body. */
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

/**
 * POST /auth/login response body.
 *
 * The DIGIBROOD CRM returns the JWT access token in a field named "token"
 * (not "access_token"), the refresh token in "refresh_token", and the lifetime
 * in "expires_in". Device status is fetched separately from /device/status.
 */
data class LoginResponse(
    @Json(name = "token") val accessToken: String?,
    @Json(name = "refresh_token") val refreshToken: String?,
    @Json(name = "expires_in") val expiresIn: Long? = null,
    @Json(name = "status") val deviceStatus: String? = null,
    @Json(name = "registered_number") val registeredNumber: String? = null,
    @Json(name = "activated_at") val activatedAt: String? = null
)

/** POST /auth/refresh request body. */
data class RefreshRequest(
    @Json(name = "refresh_token") val refreshToken: String
)

/** POST /auth/refresh response body. Returns a new access token in "token". */
data class RefreshResponse(
    @Json(name = "token") val accessToken: String?,
    @Json(name = "refresh_token") val refreshToken: String?,
    @Json(name = "expires_in") val expiresIn: Long? = null
)

/**
 * Standard error envelope returned by the CRM for non-2xx responses.
 * The CRM uses a field named "code" for machine-readable error codes such as
 * APP_LOGIN_DISABLED, INVALID_CREDENTIALS, DEVICE_NOT_APPROVED, VALIDATION,
 * INVALID_TOKEN and NO_TOKEN.
 */
data class ApiError(
    @Json(name = "code") val errorCode: String? = null,
    @Json(name = "message") val message: String? = null
)
