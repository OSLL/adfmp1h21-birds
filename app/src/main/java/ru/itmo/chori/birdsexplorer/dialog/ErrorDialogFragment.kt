package ru.itmo.chori.birdsexplorer.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

typealias Callback = () -> Unit

class ErrorDialogFragment(
    private val message: String,
    private val onFinish: Callback? = null
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity()).apply {
            setMessage(message)
            setPositiveButton(android.R.string.ok) { _, _ ->
                onFinish?.invoke()
            }
        }.create()
    }
}