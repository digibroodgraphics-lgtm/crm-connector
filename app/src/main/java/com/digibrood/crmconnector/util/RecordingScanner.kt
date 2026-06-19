package com.digibrood.crmconnector.util

import android.content.Context
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/** A candidate call-recording file found on the device. */
data class RecordingFile(
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val mimeType: String,
    val lastModified: Long
)

/**
 * Locates call-recording files using both direct file-system scanning of the
 * common recording folders and a [MediaStore] query (which also surfaces files
 * on removable SD cards). A CRM-supplied path override is honoured.
 */
@Singleton
class RecordingScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Returns all recording files discovered across the default folders, any
     * [extraPaths] (e.g. a CRM-supplied override) and the media store.
     */
    fun scan(extraPaths: List<String> = emptyList()): List<RecordingFile> {
        val found = LinkedHashMap<String, RecordingFile>()

        (Constants.DEFAULT_RECORDING_PATHS + extraPaths)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { dirPath ->
                scanDirectory(File(dirPath)).forEach { found[it.path] = it }
            }

        scanMediaStore().forEach { found.putIfAbsent(it.path, it) }

        return found.values.toList()
    }

    /**
     * Finds the recording most likely to belong to a call that started at
     * [callStart] and ended at [callEnd]. Picks a supported file whose last
     * modified time falls within a tolerance window of the call.
     */
    fun findForCall(
        callStart: Long,
        callEnd: Long,
        extraPaths: List<String> = emptyList(),
        toleranceMs: Long = 2 * 60 * 1000L
    ): RecordingFile? {
        val windowStart = callStart - toleranceMs
        val windowEnd = callEnd + toleranceMs
        return scan(extraPaths)
            .filter { it.lastModified in windowStart..windowEnd }
            .minByOrNull { abs(it.lastModified - callEnd) }
    }

    private fun scanDirectory(dir: File): List<RecordingFile> {
        return try {
            if (!dir.exists() || !dir.isDirectory) return emptyList()
            dir.walkTopDown()
                .maxDepth(2)
                .filter { it.isFile && isSupported(it.name) }
                .map { it.toRecordingFile() }
                .toList()
        } catch (_: SecurityException) {
            emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun scanMediaStore(): List<RecordingFile> {
        val results = mutableListOf<RecordingFile>()
        val projection = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_MODIFIED
        )
        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val dataIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val nameIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val mimeIdx = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
                val dateIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    val name = if (nameIdx >= 0) cursor.getString(nameIdx) ?: "" else ""
                    if (!isSupported(name)) continue
                    val path = if (dataIdx >= 0) cursor.getString(dataIdx) ?: continue else continue
                    val size = if (sizeIdx >= 0) cursor.getLong(sizeIdx) else 0L
                    val mime = if (mimeIdx >= 0) cursor.getString(mimeIdx) else null
                    val dateSec = if (dateIdx >= 0) cursor.getLong(dateIdx) else 0L
                    results.add(
                        RecordingFile(
                            path = path,
                            name = name,
                            sizeBytes = size,
                            mimeType = mime ?: mimeFor(name),
                            lastModified = dateSec * 1000L
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // MediaStore unavailable or permission missing; ignore.
        }
        return results
    }

    private fun File.toRecordingFile() = RecordingFile(
        path = absolutePath,
        name = name,
        sizeBytes = length(),
        mimeType = mimeFor(name),
        lastModified = lastModified()
    )

    private fun isSupported(name: String): Boolean {
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in Constants.SUPPORTED_RECORDING_EXTENSIONS
    }

    fun mimeFor(name: String): String = when (name.substringAfterLast('.', "").lowercase()) {
        "mp3" -> "audio/mpeg"
        "m4a" -> "audio/mp4"
        "wav" -> "audio/wav"
        "amr" -> "audio/amr"
        "3gp" -> "audio/3gpp"
        else -> "application/octet-stream"
    }
}
