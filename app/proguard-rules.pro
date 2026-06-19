# ============================================================================
# CRM Connector - ProGuard / R8 rules
# ============================================================================

# Keep line numbers for readable stack traces, hide original source file name.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations (needed by Moshi, Room, Hilt, Retrofit).
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ---------------------------------------------------------------------------
# Kotlin
# ---------------------------------------------------------------------------
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# ---------------------------------------------------------------------------
# Coroutines
# ---------------------------------------------------------------------------
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { *; }

# ---------------------------------------------------------------------------
# Retrofit
# ---------------------------------------------------------------------------
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# ---------------------------------------------------------------------------
# OkHttp
# ---------------------------------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ---------------------------------------------------------------------------
# Moshi (JSON serialization) - keep DTO model classes and generated adapters
# ---------------------------------------------------------------------------
-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep class com.digibrood.crmconnector.data.remote.dto.** { *; }
-keep class **JsonAdapter { *; }

# ---------------------------------------------------------------------------
# Room
# ---------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# ---------------------------------------------------------------------------
# Hilt / Dagger
# ---------------------------------------------------------------------------
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }
-dontwarn dagger.hilt.**

# ---------------------------------------------------------------------------
# WorkManager
# ---------------------------------------------------------------------------
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# ---------------------------------------------------------------------------
# Keep model/entity classes used across the app
# ---------------------------------------------------------------------------
-keep class com.digibrood.crmconnector.data.local.entity.** { *; }
