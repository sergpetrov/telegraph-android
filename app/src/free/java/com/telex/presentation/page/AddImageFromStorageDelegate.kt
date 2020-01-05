package com.telex.presentation.page

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telex.R
import com.telex.model.source.local.AppData
import com.telex.utils.ViewUtils

/**
 * @author Sergey Petrov
 */
class AddImageFromStorageDelegate(
    private val context: Context
) : BaseAddImageFromStorageDelegate() {

    override fun showAlert(endAction: () -> Unit) {
        val appData = AppData(context)
        if (appData.needShowMultipleImagesUploadingDialog()) {
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle(R.string.insert_image)
            builder.setNegativeButton(R.string.cancel, null)
            builder.setPositiveButton(R.string.upgrade_to_pro, null)
            builder.setMessage(context.getString(R.string.insert_multiple_images_in_pro))

            val dialog = builder.create()
            dialog.setOnShowListener {
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.upgrade_to_pro)) { _, _ -> }
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    appData.putNeedShowMultipleImagesUploadingDialog(need = false)
                    ViewUtils.openPro(context as Activity)
                    dialog.dismiss()
                }
                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.cancel)) { _, _ -> }
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                    appData.putNeedShowMultipleImagesUploadingDialog(need = false)
                    dialog.dismiss()
                    endAction.invoke()
                }
            }

            dialog.show()
        } else {
            endAction.invoke()
        }
    }
}
