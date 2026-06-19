package com.digibrood.crmconnector.data.remote

import com.digibrood.crmconnector.data.remote.dto.ApiError
import com.squareup.moshi.Moshi
import retrofit2.Response
import java.io.IOException

/**
 * A typed outcome for every network call. Keeps repositories free of raw
 * Retrofit [Response] handling and gives callers a single place to branch on
 * success, an API error (with machine-readable code) or a transport failure.
 */
sealed interface NetworkResult<out T> {

    data class Success<T>(val data: T) : NetworkResult<T>

    /** Server responded with a non-2xx status. [errorCode] is from the API error envelope. */
    data class ApiFailure(
        val httpCode: Int,
        val errorCode: String? = null,
        val message: String? = null
    ) : NetworkResult<Nothing>

    /** No response was received (offline, timeout, DNS, TLS, malformed body, etc.). */
    data class NetworkError(val throwable: Throwable) : NetworkResult<Nothing>

    val isSuccess: Boolean get() = this is Success
}

/**
 * Wraps a Retrofit suspend call, mapping it to a [NetworkResult]. Parses the
 * standard error envelope so callers can react to specific [ApiError.errorCode]
 * values such as APP_LOGIN_DISABLED. Never throws.
 */
suspend fun <T> safeApiCall(
    moshi: Moshi,
    block: suspend () -> Response<T>
): NetworkResult<T> {
    return try {
        val response = block()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                // Successful but empty body — treat Unit-like responses as success.
                @Suppress("UNCHECKED_CAST")
                NetworkResult.Success(Unit as T)
            }
        } else {
            val apiError = parseError(moshi, response)
            NetworkResult.ApiFailure(
                httpCode = response.code(),
                errorCode = apiError?.errorCode,
                message = apiError?.message ?: response.message()
            )
        }
    } catch (io: IOException) {
        NetworkResult.NetworkError(io)
    } catch (t: Throwable) {
        // Includes JsonDataException / JsonEncodingException from malformed JSON.
        NetworkResult.NetworkError(t)
    }
}

private fun parseError(moshi: Moshi, response: Response<*>): ApiError? {
    return try {
        val raw = response.errorBody()?.string()
        if (raw.isNullOrBlank()) null
        else moshi.adapter(ApiError::class.java).fromJson(raw)
    } catch (t: Throwable) {
        null
    }
}
