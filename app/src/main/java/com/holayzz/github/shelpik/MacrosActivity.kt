package com.holayzz.github.shelpik

import android.app.ActivityManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MacrosActivity : AppCompatActivity() {

    private lateinit var macrosListView: ListView
    private lateinit var btnAddMacro: Button
    private lateinit var btnServiceControl: Button
    private lateinit var repository: MacroRepository
    private lateinit var executor: MacroExecutor
    private val macros = mutableListOf<Macro>()
    private lateinit var adapter: MacroAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_macros)

        repository = MacroRepository(this)
        executor = MacroExecutor(this)

        setupUI()
        loadMacros()
    }

    private fun setupUI() {
        macrosListView = findViewById(R.id.macrosListView)
        btnAddMacro = findViewById(R.id.btnAddMacro)
        btnServiceControl = findViewById(R.id.btnServiceControl) // Теперь находим кнопку из XML

        btnServiceControl.setOnClickListener { toggleMacroService() }

        adapter = MacroAdapter(macros) { position ->
            showMacroOptionsDialog(position)
        }
        macrosListView.adapter = adapter

        btnAddMacro.setOnClickListener { showTriggerSelectionDialog() }
        updateServiceButton()
    }

    private fun loadMacros() {
        macros.clear()
        macros.addAll(repository.loadMacros())
        adapter.notifyDataSetChanged()

        if (macros.isEmpty()) {
            Toast.makeText(this, "No macros yet. Tap 'Add New Macro' to create one!", Toast.LENGTH_LONG).show()
        }
    }

    private fun showMacroOptionsDialog(position: Int) {
        if (position < 0 || position >= macros.size) {
            Toast.makeText(this, "Invalid macro selection", Toast.LENGTH_SHORT).show()
            return
        }

        val macro = macros[position]
        val options = arrayOf(
            if (macro.isEnabled) "Disable" else "Enable",
            "Delete",
            "Test Now"
        )

        AlertDialog.Builder(this)
            .setTitle("Macro Options: ${macro.name}")
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
        if (position < 0 || position >= macros.size) return

        // Toggle the macro in the current list
        macros[position] = macros[position].copy(isEnabled = !macros[position].isEnabled)

        // Save the updated list
        repository.saveMacros(macros)

        // Update the adapter
        adapter.notifyDataSetChanged()

        val state = if (macros[position].isEnabled) "enabled" else "disabled"
        Toast.makeText(this, "Macro $state", Toast.LENGTH_SHORT).show()
    }

    private fun deleteMacro(position: Int) {
        if (position < 0 || position >= macros.size) return

        val macroName = macros[position].name

        AlertDialog.Builder(this)
            .setTitle("Delete Macro")
            .setMessage("Are you sure you want to delete '$macroName'?")
            .setPositiveButton("Delete") { _, _ ->
                // Remove from current list
                macros.removeAt(position)

                // Save the updated list
                repository.saveMacros(macros)

                // Update the adapter
                adapter.notifyDataSetChanged()

                Toast.makeText(this, "Macro deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun testMacro(position: Int) {
        if (position < 0 || position >= macros.size) return

        val macro = macros[position]
        executor.executeMacro(macro)
        Toast.makeText(this, "Testing: ${macro.name}", Toast.LENGTH_SHORT).show()
    }

    private fun createMacro(triggerType: String, action: String, targetApp: String, extraData: String? = null) {
        val macro = Macro(
            name = "Custom Macro",
            trigger = triggerType,
            action = action,
            targetApp = targetApp,
            extraData = extraData
        )

        // Add to current list
        macros.add(macro)

        // Save the updated list
        repository.saveMacros(macros)

        // Update the adapter
        adapter.notifyDataSetChanged()

        Toast.makeText(this, "Macro created!", Toast.LENGTH_SHORT).show()
    }

    // ... rest of your methods (showTriggerSelectionDialog, showAppSelectionDialog, etc.)
    private fun showTriggerSelectionDialog() {
        val triggers = arrayOf(
            "App Launched (WIP)",
            "Phone Unlocked (WORK)",
            "Power Connected (WORK)",
            "Power Disconnected (WORK)",
            "Screen On (WORK)",
            "Headset Connected (UNTESTED)",
            "Headset Disconnected (UNTESTED)",
            "Specific Time (UNTESTED)"
        )

        AlertDialog.Builder(this)
            .setTitle("Select Trigger")
            .setItems(triggers) { _, which ->
                when (which) {
                    0 -> showAppSelectionDialog(TriggerTypes.APP_LAUNCH)
                    1 -> showActionSelectionDialog(TriggerTypes.PHONE_UNLOCK)
                    2 -> showActionSelectionDialog(TriggerTypes.POWER_CONNECTED)
                    3 -> showActionSelectionDialog(TriggerTypes.POWER_DISCONNECTED)
                    4 -> showActionSelectionDialog(TriggerTypes.SCREEN_ON)
                    5 -> showActionSelectionDialog(TriggerTypes.HEADSET_CONNECTED)
                    6 -> showActionSelectionDialog(TriggerTypes.HEADSET_DISCONNECTED)
                    7 -> showTimeSelectionDialog()
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

    private fun showActionSelectionDialog(triggerType: String, targetApp: String = "") {
        val actions = arrayOf(
            "Close App (ROOT)",
            "Call Number (NON-ROOT)",
            "Show Notification (NON-ROOT)",
            "Open Website (NON-ROOT)",
            "Launch App (WIP)",
            "Toggle Settings (ROOT)",
            "Vibrate (NON-ROOT)",
            "Send Broadcast (ROOT?)"
        )

        AlertDialog.Builder(this)
            .setTitle("Select Action")
            .setItems(actions) { _, which ->
                val action = when (which) {
                    0 -> ActionTypes.CLOSE_APP
                    1 -> ActionTypes.CALL_NUMBER
                    2 -> ActionTypes.SHOW_NOTIFICATION
                    3 -> ActionTypes.OPEN_WEBSITE
                    4 -> ActionTypes.LAUNCH_APP
                    5 -> ActionTypes.TOGGLE_SETTINGS
                    6 -> ActionTypes.VIBRATE
                    7 -> ActionTypes.SEND_BROADCAST
                    else -> ActionTypes.SHOW_NOTIFICATION
                }
                showActionConfigurationDialog(triggerType, action, targetApp)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showActionConfigurationDialog(triggerType: String, action: String, targetApp: String) {
        val configurator = ActionConfigurator(this, triggerType, action, targetApp, ::createMacro)
        configurator.configure()
    }

    private fun showTimeSelectionDialog() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            val timeString = String.format("%02d:%02d", hour, minute)
            createMacro(TriggerTypes.SPECIFIC_TIME, ActionTypes.SHOW_NOTIFICATION, "", "Time: $timeString")
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun toggleMacroService() {
        val serviceIntent = Intent(this, MacroService::class.java)
        if (isServiceRunning()) {
            stopService(serviceIntent)
            Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show()
        }
        updateServiceButton()
    }

    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == MacroService::class.java.name }
    }

    private fun updateServiceButton() {
        btnServiceControl.text = if (isServiceRunning()) "Stop Service" else "Start Service"
    }
}