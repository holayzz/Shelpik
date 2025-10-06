package com.holayzz.github.shelpik

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.widget.Toast

class MacroExecutor(private val context: Context) {

    fun executeMacro(macro: Macro) {
        try {
            when (macro.action) {
                ActionTypes.CLOSE_APP -> closeApp(macro.targetApp)
                ActionTypes.CALL_NUMBER -> callNumber(macro.extraData ?: "")
                ActionTypes.SHOW_NOTIFICATION -> showNotification(macro)
                ActionTypes.OPEN_WEBSITE -> openWebsite(macro.extraData ?: "")
                ActionTypes.LAUNCH_APP -> launchApp(macro.targetApp)
                ActionTypes.TOGGLE_SETTINGS -> toggleSettings(macro.extraData ?: "")
                ActionTypes.VIBRATE -> vibrate(macro.extraData?.toLongOrNull() ?: 500)
                ActionTypes.SEND_BROADCAST -> sendCustomBroadcast(macro.extraData ?: "")
            }
        } catch (e: Exception) {
            Log.e("MacroExecutor", "Error executing macro: ${e.message}")
            Toast.makeText(context, "Failed to execute macro", Toast.LENGTH_SHORT).show()
        }
    }

    private fun closeApp(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun callNumber(phoneNumber: String) {
        if (phoneNumber.isBlank()) {
            Toast.makeText(context, "No phone number specified", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun showNotification(macro: Macro) {
        val message = macro.extraData ?: "Macro executed: ${macro.name}"
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun openWebsite(url: String) {
        if (url.isBlank()) {
            Toast.makeText(context, "No URL specified", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun launchApp(packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        intent?.let {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(it)
        } ?: run {
            Toast.makeText(context, "Cannot launch app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleSettings(setting: String) {
        when (setting) {
            "wifi" -> toggleWifi()
            "bluetooth" -> toggleBluetooth()
            else -> Toast.makeText(context, "Unknown setting: $setting", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleWifi() {
        try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.isWifiEnabled = !wifiManager.isWifiEnabled
            val state = if (wifiManager.isWifiEnabled) "enabled" else "disabled"
            Toast.makeText(context, "WiFi $state", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot toggle WiFi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleBluetooth() {
        try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter.isEnabled) {
                bluetoothAdapter.disable()
                Toast.makeText(context, "Bluetooth disabled", Toast.LENGTH_SHORT).show()
            } else {
                bluetoothAdapter.enable()
                Toast.makeText(context, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot toggle Bluetooth", Toast.LENGTH_SHORT).show()
        }
    }

    private fun vibrate(duration: Long) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private fun sendCustomBroadcast(action: String) {
        if (action.isNotBlank()) {
            val intent = Intent(action)
            context.sendBroadcast(intent)
        }
    }
}