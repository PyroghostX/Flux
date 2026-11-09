package com.mskd.flux.system

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.core.net.toUri
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.report.Trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AndroidMediaFileRemover(private val context: Context) : MediaFileRemover {

    override suspend fun delete(file: UserFile): Boolean = withContext(Dispatchers.IO) {
        when (file.source) {
            FileSource.SAF -> deleteDocument(file.path.toUri())
            FileSource.LOCAL -> deleteLocal(file)
        }
    }

    /**
     * Folders picked before deleting existed were kept with read access only. The picker's grant also
     * allows write, so ask for it once and retry before giving up.
     */
    private fun deleteDocument(uri: Uri): Boolean {
        if (tryDelete(uri)) return true
        return try {
            val tree = DocumentsContract.buildTreeDocumentUri(uri.authority, DocumentsContract.getTreeDocumentId(uri))
            context.contentResolver.takePersistableUriPermission(
                tree,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            tryDelete(uri)
        } catch (e: Exception) {
            Trace.error(tag = TAG, message = "No write access to the folder of $uri", e)
            false
        }
    }

    private fun tryDelete(uri: Uri): Boolean = try {
        DocumentsContract.deleteDocument(context.contentResolver, uri)
    } catch (e: Exception) {
        Trace.error(tag = TAG, message = "Delete failed for $uri", e)
        false
    }

    private fun deleteLocal(file: UserFile): Boolean {
        val target = File(file.realPath).takeIf { it.isAbsolute }
            ?: File(Environment.getExternalStorageDirectory(), file.realPath)
        return target.delete() || !target.exists()
    }

    private companion object {
        const val TAG = "MediaFileRemover"
    }
}
