package com.holayzz.github.shelpik

import android.app.Activity
import android.app.TimePickerDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList

class MacrosActivity : AppCompatActivity() {

    private lateinit var macrosListView: ListView
    private lateinit var btnAddMacro: Button
    private lateinit var btnBack: Button
    private lateinit var btnServiceControl: Button
    private val macrosList = mutableListOf<Macro>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_macros)

        macrosListView = findViewById(R.id.macrosListView)
        btnAddMacro = findViewById(R.id.btnAddMacro)
        btnServiceControl = Button(this).apply {
            text = "Start Service!"
            setOnClickListener { toggleMacroService() }
        }

        // Добавляем кнопку управления сервисом в layout
        // Находим родительский LinearLayout через существующий элемент
        val listView = findViewById<ListView>(R.id.macrosListView)
        val parentLayout = listView.parent as? LinearLayout
        parentLayout?.addView(btnServiceControl, 3)

        setupListView()
        setupClickListeners()
        loadMacros()
        updateServiceButton()
    }

    private fun setupListView() {
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, getMacroDisplayList())
        macrosListView.adapter = adapter
    }

    private fun getMacroDisplayList(): List<String> {
        return macrosList.map { macro ->
            val status = if (macro.isEnabled) "✅" else "❌"
            val triggerEmoji = when (macro.trigger) {
                "app_launch" -> "📱"
                "phone_unlock" -> "🔓"
                "power_connected" -> "🔌"
                "power_disconnected" -> "🔋"
                "screen_on" -> "📲"
                "headset_connected" -> "🎧"
                "headset_disconnected" -> "🎧"
                "specific_time" -> "⏰"
                else -> "⚡"
            }
            "$status $triggerEmoji ${macro.name}\nAction: ${getActionName(macro.action)}"
        }
    }

    private fun getActionName(action: String): String {
        return when (action) {
            "close_app" -> "Close App"
            "call_number" -> "Call Number"
            "show_notification" -> "Show Notification"
            "open_website" -> "Open Website"
            "launch_app" -> "Launch App"
            "play_sound" -> "Play Sound"
            "toggle_settings" -> "Toggle Settings"
            "vibrate" -> "Vibrate"
            "send_broadcast" -> "Send Broadcast"
            else -> action
        }
    }

    private fun setupClickListeners() {
        btnAddMacro.setOnClickListener {
            showTriggerSelectionDialog()
        }

        macrosListView.setOnItemClickListener { _, _, position, _ ->
            showMacroOptionsDialog(position)
        }
    }

    private fun showTriggerSelectionDialog() {
        val triggers = arrayOf(
            "App Launched (WIP/MAYBE WORK)",
            "Phone Unlocked (WORK)",
            "Power Connected (WORK)",
            "Power Disconnected (WORK)",
            "Screen On (WORK)",
            "Headset Connected (DONT TESTED)",
            "Headset Disconnected (DONT TESTED)",
            "Specific Time (DONT TESTED)",
            "Screen Tap (Auto-clicker) (WIP)",
            "Color Detected (WIP)"
        )

        AlertDialog.Builder(this)
            .setTitle("Select Trigger")
            .setItems(triggers) { _, which ->
                when (which) {
                    0 -> showAppSelectionDialog("app_launch")
                    1 -> showActionSelectionDialog("phone_unlock")
                    2 -> showActionSelectionDialog("power_connected")
                    3 -> showActionSelectionDialog("power_disconnected")
                    4 -> showActionSelectionDialog("screen_on")
                    5 -> showActionSelectionDialog("headset_connected")
                    6 -> showActionSelectionDialog("headset_disconnected")
                    7 -> showTimeSelectionDialog()
                    8 -> showAutoClickerDialog()
                    9 -> showColorDetectionDialog()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAppSelectionDialog(triggerType: String) {
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appNames = apps.map {
            "📱 ${packageManager.getApplicationLabel(it)}"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select App for Trigger")
            .setItems(appNames) { _, which ->
                val selectedApp = apps[which]
                showActionSelectionDialog(triggerType, selectedApp.packageName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showActionSelectionDialog(triggerType: String, targetApp: String? = null) {
        val actions = arrayOf(
            "Close App (ROOT)",
            "Call Number (NON-ROOT)",
            "Show Notification (NON-ROOT)",
            "Open Website (NON-ROOT)",
            "Launch App (WIP. im lazy)",
            "Play Sound (NON-ROOT)",
            "Toggle Settings (ROOT)",
            "Vibrate (NON-ROOT)",
            "Send Broadcast (MAYBE ROOT)"
        )

        AlertDialog.Builder(this)
            .setTitle("Select Action")
            .setItems(actions) { _, which ->
                val action = when (which) {
                    0 -> "close_app"
                    1 -> "call_number"
                    2 -> "show_notification"
                    3 -> "open_website"
                    4 -> "launch_app"
                    5 -> "play_sound"
                    6 -> "toggle_settings"
                    7 -> "vibrate"
                    8 -> "send_broadcast"
                    else -> "show_notification"
                }
                showActionConfigurationDialog(triggerType, action, targetApp ?: "")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showActionConfigurationDialog(triggerType: String, action: String, targetApp: String) {
        val triggerName = getTriggerName(triggerType)
        val actionName = getActionName(action)

        when (action) {
            "call_number" -> showNumberInputDialog(triggerType, action, targetApp, triggerName, actionName)
            "open_website" -> showWebsiteInputDialog(triggerType, action, targetApp, triggerName, actionName)
            "show_notification" -> showNotificationInputDialog(triggerType, action, targetApp, triggerName, actionName)
            "toggle_settings" -> showSettingsSelectionDialog(triggerType, action, targetApp, triggerName, actionName)
            "vibrate" -> showVibrateInputDialog(triggerType, action, targetApp, triggerName, actionName)
            "send_broadcast" -> showBroadcastInputDialog(triggerType, action, targetApp, triggerName, actionName)
            else -> createMacro(triggerType, action, targetApp, triggerName, actionName)
        }
    }

    private fun showNumberInputDialog(triggerType: String, action: String, targetApp: String, triggerName: String, actionName: String) {
        val input = EditText(this).apply {
            hint = "Enter phone number"
            inputType = InputType.TYPE_CLASS_PHONE
        }

        AlertDialog.Builder(this)
            .setTitle("Enter Phone Number")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val number = input.text.toString()
                createMacro(triggerType, action, targetApp, triggerName, actionName, number)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showWebsiteInputDialog(triggerType: String, action: String, targetApp: String, triggerName: String, actionName: String) {
        val input = EditText(this).apply {
            hint = "Enter website URL"
            setText("https://")
            inputType = InputType.TYPE_TEXT_VARIATION_URI
        }

        AlertDialog.Builder(this)
            .setTitle("Enter Website URL")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val url = input.text.toString()
                createMacro(triggerType, action, targetApp, triggerName, actionName, url)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNotificationInputDialog(triggerType: String, action: String, targetApp: String, triggerName: String, actionName: String) {
        val input = EditText(this).apply {
            hint = "Enter notification message"
        }

        AlertDialog.Builder(this)
            .setTitle("Enter Notification Message")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val message = input.text.toString()
                createMacro(triggerType, action, targetApp, triggerName, actionName, message)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSettingsSelectionDialog(triggerType: String, action: String, targetApp: String, triggerName: String, actionName: String) {
        val settings = arrayOf("WiFi", "Bluetooth")

        AlertDialog.Builder(this)
            .setTitle("Select Setting to Toggle")
            .setItems(settings) { _, which ->
                val setting = when (which) {
                    0 -> "wifi"
                    1 -> "bluetooth"
                    else -> "wifi"
                }
                createMacro(triggerType, action, targetApp, triggerName, actionName, setting)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showVibrateInputDialog(triggerType: String, action: String, targetApp: String, triggerName: String, actionName: String) {
        val input = EditText(this).apply {
            hint = "Vibration duration (ms)"
            setText("500")
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        AlertDialog.Builder(this)
            .setTitle("Vibration Duration")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val duration = input.text.toString()
                createMacro(triggerType, action, targetApp, triggerName, actionName, duration)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showBroadcastInputDialog(triggerType: String, action: String, targetApp: String, triggerName: String, actionName: String) {
        val input = EditText(this).apply {
            hint = "Broadcast action name"
            setText("com.example.CUSTOM_ACTION")
        }

        AlertDialog.Builder(this)
            .setTitle("Broadcast Action")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val actionName = input.text.toString()
                createMacro(triggerType, action, targetApp, triggerName, actionName, actionName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getTriggerName(triggerType: String): String {
        return when (triggerType) {
            "app_launch" -> "App Launched"
            "phone_unlock" -> "Phone Unlocked"
            "power_connected" -> "Power Connected"
            "power_disconnected" -> "Power Disconnected"
            "screen_on" -> "Screen On"
            "headset_connected" -> "Headset Connected"
            "headset_disconnected" -> "Headset Disconnected"
            "specific_time" -> "Specific Time"
            else -> "Custom Trigger"
        }
    }

    private fun createMacro(triggerType: String, action: String, targetApp: String, triggerName: String, actionName: String, extraData: String? = null) {
        val appName = if (targetApp.isNotEmpty()) {
            try {
                val appInfo = packageManager.getApplicationInfo(targetApp, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                "System"
            }
        } else {
            "System"
        }

        val macroName = "$triggerName → $actionName"

        val macro = Macro(
            id = System.currentTimeMillis(),
            name = macroName,
            trigger = triggerType,
            action = action,
            targetApp = targetApp,
            extraData = extraData,
            isEnabled = true
        )

        macrosList.add(macro)
        saveMacros()
        adapter.clear()
        adapter.addAll(getMacroDisplayList())
        adapter.notifyDataSetChanged()

        Toast.makeText(this, "Macro created!", Toast.LENGTH_SHORT).show()
    }

    private fun showMacroOptionsDialog(position: Int) {
        val macro = macrosList[position]
        val options = arrayOf(
            if (macro.isEnabled) "Disable" else "Enable",
            "Delete",
            "Test Now",
            "Edit"
        )

        AlertDialog.Builder(this)
            .setTitle("Macro Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> toggleMacro(position)
                    1 -> deleteMacro(position)
                    2 -> testMacro(position)
                    3 -> editMacro(position)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleMacro(position: Int) {
        macrosList[position] = macrosList[position].copy(isEnabled = !macrosList[position].isEnabled)
        saveMacros()
        adapter.clear()
        adapter.addAll(getMacroDisplayList())
        adapter.notifyDataSetChanged()
    }

    private fun deleteMacro(position: Int) {
        macrosList.removeAt(position)
        saveMacros()
        adapter.clear()
        adapter.addAll(getMacroDisplayList())
        adapter.notifyDataSetChanged()
    }

    private fun testMacro(position: Int) {
        val macro = macrosList[position]
        executeMacroAction(macro)
        Toast.makeText(this, "Testing macro: ${macro.name}", Toast.LENGTH_SHORT).show()
    }

    private fun editMacro(position: Int) {
        val macro = macrosList[position]
        Toast.makeText(this, "Edit feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun executeMacroAction(macro: Macro) {
        when (macro.action) {
            "close_app" -> closeApp(macro.targetApp)
            "call_number" -> callNumber(macro.extraData ?: "+1234567890")
            "show_notification" -> showNotification("Macro Test", macro.extraData ?: "Macro executed!")
            "open_website" -> openWebsite(macro.extraData ?: "https://google.com")
            "launch_app" -> launchApp(macro.targetApp)
            "toggle_settings" -> toggleSettings(macro.extraData ?: "wifi")
            "vibrate" -> vibrate(macro.extraData?.toLongOrNull() ?: 500)
            "send_broadcast" -> sendCustomBroadcast(macro.extraData ?: "")
        }
    }

    private fun closeApp(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot close system app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun callNumber(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$phoneNumber")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot make call", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNotification(title: String, message: String) {
        Toast.makeText(this, "$title: $message", Toast.LENGTH_LONG).show()
    }

    private fun openWebsite(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open website", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchApp(packageName: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot launch app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleSettings(setting: String) {
        try {
            when (setting) {
                "wifi" -> {
                    val wifiManager = getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
                    val newState = !wifiManager.isWifiEnabled
                    wifiManager.isWifiEnabled = newState
                    Toast.makeText(this, "WiFi ${if (newState) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                }
                "bluetooth" -> {
                    val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                    if (bluetoothAdapter.isEnabled) {
                        bluetoothAdapter.disable()
                        Toast.makeText(this, "Bluetooth disabled", Toast.LENGTH_SHORT).show()
                    } else {
                        bluetoothAdapter.enable()
                        Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot toggle settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun vibrate(duration: Long) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(duration, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(duration)
        }
        Toast.makeText(this, "Vibrating for ${duration}ms", Toast.LENGTH_SHORT).show()
    }

    private fun sendCustomBroadcast(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
        Toast.makeText(this, "Broadcast sent: $action", Toast.LENGTH_SHORT).show()
    }

    private fun showTimeSelectionDialog() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            val timeString = String.format("%02d:%02d", hour, minute)
            createMacro("specific_time", "show_notification", "", "Specific Time", "Show Notification", "Time: $timeString")
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun showAutoClickerDialog() {
        Toast.makeText(this, "Auto-clicker coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showColorDetectionDialog() {
        Toast.makeText(this, "Color detection coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun loadMacros() {
        val prefs = getSharedPreferences("macros", Context.MODE_PRIVATE)
        val macrosJson = prefs.getString("macros_list", "[]") ?: "[]"

        try {
            val type = object : TypeToken<List<Macro>>() {}.type
            val loadedMacros: List<Macro> = Gson().fromJson(macrosJson, type) ?: emptyList()
            macrosList.clear()
            macrosList.addAll(loadedMacros)
        } catch (e: Exception) {
            // Ignore, use empty list
        }
    }

    private fun saveMacros() {
        val prefs = getSharedPreferences("macros", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val macrosJson = Gson().toJson(macrosList)
        editor.putString("macros_list", macrosJson)
        editor.apply()
    }

    private fun toggleMacroService() {
        val serviceIntent = Intent(this, MacroService::class.java)

        if (isServiceRunning()) {
            stopService(serviceIntent)
            Toast.makeText(this, "Macro service stopped", Toast.LENGTH_SHORT).show()
        } else {
            // Для Android 8.0+ используем startForegroundService, для старых версий - startService
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            Toast.makeText(this, "Macro service started", Toast.LENGTH_SHORT).show()
        }
        updateServiceButton()
    }

    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == MacroService::class.java.name }
    }

    private fun updateServiceButton() {
        btnServiceControl.text = if (isServiceRunning()) "Stop Service!" else "Start Service!"
    }
}