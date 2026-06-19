package com.digibrood.crmconnector.di

import javax.inject.Qualifier

/**
 * Marks the [com.digibrood.crmconnector.data.remote.api.CrmApiService] instance
 * dedicated to token refresh. It is built from an OkHttp client that does NOT
 * include [com.digibrood.crmconnector.data.remote.interceptor.TokenAuthenticator],
 * which prevents infinite refresh recursion on HTTP 401.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshClient

/** Marks the main authenticated OkHttp client / API service. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainClient

/**
 * Marks a plain OkHttp client used for uploading recordings directly to R2 via a
 * presigned URL. It deliberately has NO dynamic-base-URL or auth interceptors,
 * because the upload target is an external storage host, not the CRM.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UploadClient

/** Marks an application-scoped [kotlinx.coroutines.CoroutineScope]. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
