package com.telex.base.utils

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * @author Sergey Petrov
 */
object ImagePickerHelper {

    fun getFilePathFromURI(context: Context, contentUri: Uri): String? {
        // copy file and send new file path
        val fileName = getFileName(contentUri)
        if (!TextUtils.isEmpty(fileName)) {
            val copyFile = File(context.cacheDir, fileName)
            copy(context, contentUri, copyFile)
            return copyFile.toURI().toString()
        }
        return null
    }

    fun getFileName(uri: Uri?): String? {
        if (uri == null) return null
        var fileName: String? = null
        val path = uri.path.orEmpty()
        val cut = path.lastIndexOf('/')
        if (cut != -1) {
            fileName = path.substring(cut + 1)
        }
        return fileName
    }

    fun copy(context: Context, srcUri: Uri, dstFile: File) {
        try {
            val inputStream = context.contentResolver.openInputStream(srcUri) ?: return
            val outputStream = FileOutputStream(dstFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
