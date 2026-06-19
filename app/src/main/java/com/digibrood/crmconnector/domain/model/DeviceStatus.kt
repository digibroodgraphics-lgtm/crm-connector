package com.digibrood.crmconnector.domain.model

/**
 * Approval lifecycle of the device as reported by the CRM backend.
 *
 * Only [APPROVED] permits background sync. Every other state stops all
 * background work (see SyncController).
 */
enum class DeviceStatus(val apiValue: String) {
    PENDING_APPROVAL("pending_approval"),
    APPROVED("approved"),
    DENIED("denied"),
    REVOKED("revoked"),
    INACTIVE("inactive"),
    UNKNOWN("unknown");

    val isActive: Boolean get() = this == APPROVED

    /** True when the device is registered but not yet approved. */
    val isPending: Boolean get() = this == PENDING_APPROVAL

    /** True when background work must be fully stopped. */
    val isStopped: Boolean get() = this == DENIED || this == REVOKED || this == INACTIVE

    companion object {
        fun fromApi(value: String?): DeviceStatus =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}
