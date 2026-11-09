package com.mskd.flux.system

import com.mskd.flux.core.model.files.UserFile

/** Erases a video file from where it is stored (a picked folder or the device storage). */
interface MediaFileRemover {
    /** True once the file is gone. */
    suspend fun delete(file: UserFile): Boolean
}
