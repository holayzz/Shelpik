package com.holayzz.github.shelpik

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog as MaterialAlertDialogBuilder

class ActionConfigurator(
    private val context: Context,
    private val triggerType: String,
    private val action: String,
    private val targetApp: String,
    private val onCreateMacro: (String, String, String, String?) -> Unit
) {

    fun configure() {
        when (action) {
            ActionTypes.CALL_NUMBER -> showNumberInputDialog()
            ActionTypes.OPEN_WEBSITE -> showWebsiteInputDialog()
            ActionTypes.SHOW_NOTIFICATION -> showNotificationInputDialog()
            ActionTypes.TOGGLE_SETTINGS -> showSettingsSelectionDialog()
            ActionTypes.VIBRATE -> showVibrateInputDialog()
            ActionTypes.SEND_BROADCAST -> showBroadcastInputDialog()
            else -> createMacro()
        }
    }

    private fun showNumberInputDialog() {
        showInputDialog("Enter Phone Number", "Phone number", InputType.TYPE_CLASS_PHONE) { number ->
            createMacro(number)
        }
    }

    private fun showWebsiteInputDialog() {
        showInputDialog("Enter Website URL", "https://", InputType.TYPE_TEXT_VARIATION_URI) { url ->
            createMacro(url)
        }
    }

    private fun showNotificationInputDialog() {
        showInputDialog("Enter Notification Message", "Message") { message ->
            createMacro(message)
        }
    }

    private fun showVibrateInputDialog() {
        showInputDialog("Vibration Duration", "500", InputType.TYPE_CLASS_NUMBER) { duration ->
            createMacro(duration)
        }
    }

    private fun showBroadcastInputDialog() {
        showInputDialog("Broadcast Action", "com.example.CUSTOM_ACTION") { actionName ->
            createMacro(actionName)
        }
    }

    private fun showSettingsSelectionDialog() {
        val settings = arrayOf("WiFi", "Bluetooth")
        AlertDialog.Builder(context)
            .setTitle("Select Setting")
            .setItems(settings) { _, which ->
                val setting = when (which) {
                    0 -> "wifi"
                    1 -> "bluetooth"
                    else -> "wifi"
                }
                createMacro(setting)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showInputDialog(
        title: String,
        hint: String,
        inputType: Int = InputType.TYPE_CLASS_TEXT,
        onConfirm: (String) -> Unit
    ) {
        val input = EditText(context).apply {
            setHint(hint)
            setText(if (hint.startsWith("http") || hint == "500") hint else "")
            this.inputType = inputType
        }

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val text = input.text.toString().trim()
                if (text.isNotBlank()) {
                    onConfirm(text)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createMacro(extraData: String? = null) {
        onCreateMacro(triggerType, action, targetApp, extraData)
    }
}