package com.digibrood.crmconnector.domain.model

import android.provider.CallLog

/**
 * Normalised call direction/outcome. Maps both to the platform [CallLog] type
 * constants and to the string value sent to the CRM.
 */
enum class CallType(val apiValue: String) {
    INCOMING("incoming"),
    OUTGOING("outgoing"),
    MISSED("missed"),
    REJECTED("rejected"),
    UNKNOWN("unknown");

    companion object {
        fun fromCallLogType(type: Int): CallType = when (type) {
            CallLog.Calls.INCOMING_TYPE -> INCOMING
            CallLog.Calls.OUTGOING_TYPE -> OUTGOING
            CallLog.Calls.MISSED_TYPE -> MISSED
            CallLog.Calls.REJECTED_TYPE -> REJECTED
            CallLog.Calls.BLOCKED_TYPE -> REJECTED
            else -> UNKNOWN
        }

        fun fromApi(value: String?): CallType =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}
