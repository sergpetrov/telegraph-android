package com.telex.base.presentation.login

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telex.base.R
import com.telex.base.presentation.base.BaseDialogFragment
import com.telex.base.utils.ViewUtils

/**
 * @author Sergey Petrov
 */
@RequiresApi(Build.VERSION_CODES.S)
class ConfigureSupportedLinksDialogFragment : BaseDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle(R.string.configure_supported_links)
        builder.setNegativeButton(R.string.cancel, null)
        builder.setPositiveButton(R.string.open_settings, null)
        builder.setMessage(getString(R.string.configure_supported_links_description))

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.launch_telegram)) { _, _ -> }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                ViewUtils.openByDefaultSettings(requireActivity())
                dialog.dismiss()
            }
        }

        return dialog
    }
}
