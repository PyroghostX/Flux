package com.mskd.flux.features.artwork.domain.usecase

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.system.MediaFileRemover

/** Erases the media's video file, then drops it from the library (and its show or season once empty). */
class DeleteMediaFileUseCase(
    private val remover: MediaFileRemover,
    private val database: DatabaseRepository,
) {

    suspend operator fun invoke(media: Media): Boolean {
        if (!remover.delete(media.file)) return false

        val keptFiles = database.getMedias().map { it.file }.filter { it.path != media.file.path }
        database.deleteMediasNotInFiles(files = keptFiles)
        return true
    }

}
