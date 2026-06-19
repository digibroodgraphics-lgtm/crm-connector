package com.digibrood.crmconnector.util

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves a display name for a phone number from the device's local contacts.
 * Used as a fast, offline fallback for the after-call popup when the CRM lookup
 * is unavailable.
 */
@Singleton
class ContactReader @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun displayNameFor(phoneNumber: String?): String? {
        if (phoneNumber.isNullOrBlank()) return null
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        val uri: Uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        return try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (idx >= 0) cursor.getString(idx) else null
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
