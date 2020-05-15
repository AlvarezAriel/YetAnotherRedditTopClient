package me.ariel.redditop.actions

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.webkit.MimeTypeMap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import me.ariel.redditop.data.EntriesRepository
import me.ariel.redditop.network.RedditApi
import me.ariel.redditop.utils.ReditopFileUtils
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * Note: This is what you would normally consider a "Service", where the main business logic resides.
 * It's called Actions in order to avoid confusing it with some android.app.Service
 */
class EntryActions(
    val redditApi: RedditApi,
    val entriesRepository: EntriesRepository,
    val context: Context
) {

    fun refreshTopEntries() = redditApi.top()
        .subscribeOn(Schedulers.io())
        .flattenAsFlowable { it.asIterable() }
        .flatMapSingle { remoteEntry ->
            entriesRepository.fetchById(remoteEntry.uid).flatMap { result ->
                val completable = if (result.isFailure) {
                    // if this is a new entry, insert it as new
                    entriesRepository.create(remoteEntry)
                } else {
                    // if we already have this entry, update it but keep local flags
                    val value = result.getOrThrow()
                    entriesRepository.update(
                        remoteEntry.copy(
                            isDismissed = value.isDismissed,
                            isRead = value.isRead
                        )
                    )
                }
                completable.toSingleDefault(remoteEntry.uid)
            }
        }

    fun saveEntryImageToGallery(imageUrl:String):Single<File> {

        return Single.fromCallable {
            Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .fitCenter()
                .submit()
                .get()
        }.map {
            val imageFile = ReditopFileUtils.createOutputImageFile(context)
            FileOutputStream(imageFile).use { out ->
                it.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            imageFile

        }.doOnSuccess {
            val mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(it.extension)
            MediaScannerConnection.scanFile(
                context,
                arrayOf(it.absolutePath),
                arrayOf(mimeType)
            ) { _, uri ->
                Timber.d("Download image %s scanned into media store: %s ", imageUrl, uri)
            }

        }.subscribeOn(Schedulers.io())
    }


}