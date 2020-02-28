package com.telex.base.presentation.page

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.telex.base.presentation.page.format.ImageFormat
import com.telex.base.utils.ImagePickerHelper

/**
 * @author Sergey Petrov
 */
abstract class BaseAddImageFromStorageDelegate {

    open fun showAlert(endAction: () -> Unit) {
        endAction.invoke()
    }

    open fun startActivityForResult(context: Context, requestCode: Int) {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = ACCEPTED_IMAGE_MIME
        (context as Activity).startActivityForResult(galleryIntent, requestCode)
    }

    open fun convertIntentDataToImageFormats(context: Context, data: Intent): List<ImageFormat> {
        val images = arrayListOf<ImageFormat>()

        val intentData = data.data
        if (intentData != null) {
            images.add(convertUriToImageFormat(context, intentData))
        }

        return images
    }

    protected fun convertUriToImageFormat(context: Context, uri: Uri): ImageFormat {
        val filePath = ImagePickerHelper.getFilePathFromURI(context, uri).toString()
        return ImageFormat(filePath, "")
    }

    companion object {
        const val ACCEPTED_IMAGE_MIME = "image/*"
    }
}
