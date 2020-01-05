package com.telex.presentation.page

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.telex.presentation.page.format.ImageFormat

/**
 * @author Sergey Petrov
 */
class AddImageFromStorageDelegate(
    private val context: Context
) : BaseAddImageFromStorageDelegate() {

    override fun startActivityForResult(context: Context, requestCode: Int) {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        galleryIntent.type = ACCEPTED_IMAGE_MIME
        (context as Activity).startActivityForResult(galleryIntent, requestCode)
    }

    override fun convertIntentDataToImageFormats(context: Context, data: Intent): List<ImageFormat> {
        val images = arrayListOf<ImageFormat>()

        val clipData = data.clipData
        if (clipData != null) {
            val count = clipData.itemCount
            for (i in 0 until count) {
                val imageUri = clipData.getItemAt(i).uri
                images.add(convertUriToImageFormat(context, imageUri))
            }
        } else if (data.data != null) {
            images.add(convertUriToImageFormat(context, requireNotNull(data.data)))
        }

        return images
    }
}
