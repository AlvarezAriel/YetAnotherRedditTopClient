package me.ariel.redditop.utils

import android.content.Context
import me.ariel.redditop.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ReditopFileUtils {

    private fun getOutputGalleryDirectory(appContext: Context): File {

        val mediaDir = appContext.externalMediaDirs.firstOrNull()?.let {
            File(it, appContext.resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }

        return if (mediaDir != null && mediaDir.exists())
            mediaDir else appContext.filesDir
    }

    fun createOutputImageFile(context: Context) =
        File(
            getOutputGalleryDirectory(context.applicationContext),
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

}