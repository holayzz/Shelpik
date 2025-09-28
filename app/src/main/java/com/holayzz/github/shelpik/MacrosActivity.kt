package com.holayzz.github.shelpik

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.service.quicksettings.TileService
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast

// Data class для хранения макросов
data class Macro(
    val id: Long,
    val name: String,
    val trigger: String,
    val action: String,
    val targetApp: String,
    val isEnabled: Boolean = true
)

class MacrosActivity : AppCompatActivity() {

    private lateinit var macrosListView: ListView
    private lateinit var btnAddMacro: Button
    private lateinit var btnBack: Button
    private val macrosList = mutableListOf<Macro>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_macros)

        macrosListView = findViewById(R.id.macrosListView)
        btnAddMacro = findViewById(R.id.btnAddMacro)
        btnBack = findViewById(R.id.btnBack)

        setupListView()
        setupClickListeners()
        loadMacros()
    }

    private fun setupListView() {
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, getMacroDisplayList())
        macrosListView.adapter = adapter
    }

    private fun getMacroDisplayList(): List<String> {
        return macrosList.map { macro ->
            val status = if (macro.isEnabled) "✅" else "❌"
            "$status ${macro.name}\nTrigger: ${macro.trigger} → Action: ${macro.action}"
        }
    }

    private fun setupClickListeners() {
        btnAddMacro.setOnClickListener {
            showTriggerSelectionDialog()
        }

        btnBack.setOnClickListener {
            finish()
        }

        macrosListView.setOnItemClickListener { _, _, position, _ ->
            showMacroOptionsDialog(position)
        }
    }

    private fun showTriggerSelectionDialog() {
        val triggers = arrayOf(
            "📱 App Launched",
            "🔌 Phone Unlocked",
            "🔋 Power Connected",
            "🌐 WiFi Connected",
            "⏰ Specific Time",
            "🎯 Screen Tap (Auto-clicker)",
            "🎨 Color Detected"
        )

        AlertDialog.Builder(this)
            .setTitle("Select Trigger")
            .setItems(triggers) { _, which ->
                when (which) {
                    0 -> showAppSelectionDialog("app_launch")
                    1 -> showActionSelectionDialog("phone_unlock")
                    2 -> showActionSelectionDialog("power_connected")
                    3 -> showActionSelectionDialog("wifi_connected")
                    4 -> showTimeSelectionDialog()
                    5 -> showAutoClickerDialog()
                    6 -> showColorDetectionDialog()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAppSelectionDialog(triggerType: String) {
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appNames = apps.map { packageManager.getApplicationLabel(it).toString() }

        AlertDialog.Builder(this)
            .setTitle("Select App for Trigger")
            .setItems(appNames.toTypedArray()) { _, which ->
                val selectedApp = apps[which]
                showActionSelectionDialog(triggerType, selectedApp.packageName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showActionSelectionDialog(triggerType: String, targetApp: String? = null) {
        val actions = arrayOf(
            "❌ Close App",
            "📞 Call Number",
            "🔔 Show Notification",
            "🌐 Open Website",
            "📱 Launch App",
            "🔊 Play Sound",
            "⚡ Toggle Settings"
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
                    else -> "close_app"
                }

                createMacro(triggerType, action, targetApp ?: "")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createMacro(triggerType: String, action: String, targetApp: String) {
        val triggerName = when (triggerType) {
            "app_launch" -> "App Launched"
            "phone_unlock" -> "Phone Unlocked"
            "power_connected" -> "Power Connected"
            "wifi_connected" -> "WiFi Connected"
            else -> "Custom Trigger"
        }

        val actionName = when (action) {
            "close_app" -> "Close App"
            "call_number" -> "Call Number"
            "show_notification" -> "Show Notification"
            "open_website" -> "Open Website"
            "launch_app" -> "Launch App"
            "play_sound" -> "Play Sound"
            "toggle_settings" -> "Toggle Settings"
            else -> "Custom Action"
        }

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
            targetApp = targetApp
        )

        macrosList.add(macro)
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
            "Test Now"
        )

        AlertDialog.Builder(this)
            .setTitle("Macro Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> toggleMacro(position)
                    1 -> deleteMacro(position)
                    2 -> testMacro(position)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleMacro(position: Int) {
        macrosList[position] = macrosList[position].copy(isEnabled = !macrosList[position].isEnabled)
        adapter.clear()
        adapter.addAll(getMacroDisplayList())
        adapter.notifyDataSetChanged()
    }

    private fun deleteMacro(position: Int) {
        macrosList.removeAt(position)
        adapter.clear()
        adapter.addAll(getMacroDisplayList())
        adapter.notifyDataSetChanged()
    }

    private fun testMacro(position: Int) {
        val macro = macrosList[position]
        executeMacroAction(macro)
        Toast.makeText(this, "Testing macro: ${macro.name}", Toast.LENGTH_SHORT).show()
    }

    private fun executeMacroAction(macro: Macro) {
        when (macro.action) {
            "close_app" -> closeApp(macro.targetApp)
            "call_number" -> callNumber("+1234567890") // Default number
            "show_notification" -> showNotification("Macro Test", "Macro executed successfully!")
            "open_website" -> openWebsite("https://google.com")
            // TODO: Implement other actions
        }
    }

    private fun closeApp(packageName: String) {
        try {
            // Try to force stop the app
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = android.net.Uri.parse("package:$packageName")
            startActivity(intent)

            // Alternative method using activity manager (requires system permission)
            // val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            // activityManager.killBackgroundProcesses(packageName)

            Toast.makeText(this, "Attempting to close app", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot close system app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun callNumber(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = android.net.Uri.parse("tel:$phoneNumber")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot make call", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNotification(title: String, message: String) {
        // Simple toast notification for now
        Toast.makeText(this, "$title: $message", Toast.LENGTH_LONG).show()
    }

    private fun openWebsite(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open website", Toast.LENGTH_SHORT).show()
        }
    }

    // TODO: Implement these dialog methods
    private fun showTimeSelectionDialog() {
        Toast.makeText(this, "Time selection coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showAutoClickerDialog() {
        Toast.makeText(this, "Auto-clicker coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showColorDetectionDialog() {
        Toast.makeText(this, "Color detection coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun loadMacros() {
        // TODO: Load macros from SharedPreferences or database
    }

    private fun saveMacros() {
        // TODO: Save macros to SharedPreferences or database
    }
}