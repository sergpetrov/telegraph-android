package com.telex.base.presentation.home

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telex.base.R
import com.telex.base.presentation.base.BaseDialogFragment

/**
 * @author Sergey Petrov
 */
class AddAccountDialogFragment : BaseDialogFragment() {

    private lateinit var onLaunchClickListener: () -> Unit

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle(R.string.add_account)
        builder.setNegativeButton(R.string.cancel, null)
        builder.setPositiveButton(R.string.launch_telegram, null)
        builder.setMessage(getString(R.string.add_account_description, getString(R.string.app_name)))

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.launch_telegram)) { _, _ -> }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                onLaunchClickListener.invoke()
                dialog.dismiss()
            }
        }

        return dialog
    }

    companion object {

        fun newInstance(onLaunchClickListener: () -> Unit): AddAccountDialogFragment {
            val fragment = AddAccountDialogFragment()
            fragment.onLaunchClickListener = onLaunchClickListener
            return fragment
        }
    }
}
