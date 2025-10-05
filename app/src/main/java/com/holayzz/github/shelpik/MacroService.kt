package com.holayzz.github.shelpik

import android.app.*
import android.content.*
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MacroService : Service() {

    private val CHANNEL_ID = "MacroServiceChannel"
    private val NOTIFICATION_ID = 1

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("MacroService", "Received action: ${intent.action}")

            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> {
                    executeMacrosForTrigger("screen_on")
                }
                Intent.ACTION_USER_PRESENT -> {
                    executeMacrosForTrigger("phone_unlock")
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    executeMacrosForTrigger("power_connected")
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    executeMacrosForTrigger("power_disconnected")
                }
                Intent.ACTION_HEADSET_PLUG -> {
                    val state = intent.getIntExtra("state", -1)
                    if (state == 1) {
                        executeMacrosForTrigger("headset_connected")
                    } else if (state == 0) {
                        executeMacrosForTrigger("headset_disconnected")
                    }
                }
            }
        }
    }

    private var appLaunchReceiver: AppLaunchReceiver? = null
    private var localBroadcastManager: LocalBroadcastManager? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("MacroService", "Service creating...")

        createNotificationChannel()

        // Запускаем сервис в foreground
        startForeground(NOTIFICATION_ID, createNotification())

        // Регистрируем ресивер для системных событий
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_HEADSET_PLUG)
        }
        registerReceiver(receiver, filter)

        // Регистрируем кастомный ресивер для запуска приложений
        appLaunchReceiver = AppLaunchReceiver()
        val appFilter = IntentFilter(Intent.ACTION_PACKAGE_ADDED)
        appFilter.addDataScheme("package")
        registerReceiver(appLaunchReceiver, appFilter)

        // Локальный бродкаст для коммуникации с активностями
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager?.registerReceiver(localReceiver, IntentFilter("APP_LAUNCHED"))

        Toast.makeText(this, "Macro service started", Toast.LENGTH_SHORT).show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Macro Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for Macro Service"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Macro Service")
            .setContentText("Monitoring system events for macros")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MacroService", "Service starting...")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(receiver)
            appLaunchReceiver?.let { unregisterReceiver(it) }
            localBroadcastManager?.unregisterReceiver(localReceiver)
        } catch (e: Exception) {
            Log.e("MacroService", "Error unregistering receivers: ${e.message}")
        }
        Log.d("MacroService", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun executeMacrosForTrigger(trigger: String, packageName: String? = null) {
        Log.d("MacroService", "Executing macros for trigger: $trigger, package: $packageName")

        val macros = loadMacros().filter {
            it.trigger == trigger && it.isEnabled &&
                    (packageName == null || it.targetApp == packageName)
        }

        macros.forEach { macro ->
            Log.d("MacroService", "Executing macro: ${macro.name}")
            executeMacroAction(macro)
        }
    }

    // Остальные методы executeMacroAction, closeApp, callNumber и т.д. остаются без изменений
    private fun executeMacroAction(macro: Macro) {
        try {
            when (macro.action) {
                "close_app" -> closeApp(macro.targetApp)
                "call_number" -> callNumber(macro.extraData ?: "+1234567890")
                "show_notification" -> showNotification(
                    macro.extraData ?: "Macro",
                    "Macro executed: ${macro.name}"
                )
                "open_website" -> openWebsite(macro.extraData ?: "https://google.com")
                "launch_app" -> launchApp(macro.targetApp)
                "play_sound" -> playSound(macro.extraData?.toIntOrNull() ?: 1)
                "toggle_settings" -> toggleSettings(macro.extraData ?: "wifi")
                "vibrate" -> vibrate(macro.extraData?.toLongOrNull() ?: 500)
                "send_broadcast" -> sendCustomBroadcast(macro.extraData ?: "")
            }
        } catch (e: Exception) {
            Log.e("MacroService", "Error executing macro: ${e.message}")
        }
    }

    private fun closeApp(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = android.net.Uri.parse("package:$packageName")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MacroService", "Cannot close app: ${e.message}")
        }
    }

    private fun callNumber(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = android.net.Uri.parse("tel:$phoneNumber")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MacroService", "Cannot make call: ${e.message}")
        }
    }

    private fun showNotification(title: String, message: String) {
        Toast.makeText(this, "$title: $message", Toast.LENGTH_LONG).show()
    }

    private fun openWebsite(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MacroService", "Cannot open website: ${e.message}")
        }
    }

    private fun launchApp(packageName: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MacroService", "Cannot launch app: ${e.message}")
        }
    }

    private fun playSound(soundType: Int) {
        Toast.makeText(this, "Playing sound type: $soundType", Toast.LENGTH_SHORT).show()
    }

    private fun toggleSettings(setting: String) {
        try {
            when (setting) {
                "wifi" -> {
                    val wifiManager = getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
                    wifiManager.isWifiEnabled = !wifiManager.isWifiEnabled
                }
                "bluetooth" -> {
                    val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                    if (bluetoothAdapter.isEnabled) {
                        bluetoothAdapter.disable()
                    } else {
                        bluetoothAdapter.enable()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MacroService", "Cannot toggle settings: ${e.message}")
        }
    }

    private fun vibrate(duration: Long) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(duration, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private fun sendCustomBroadcast(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    private fun loadMacros(): List<Macro> {
        val prefs = getSharedPreferences("macros", Context.MODE_PRIVATE)
        val macrosJson = prefs.getString("macros_list", "[]") ?: "[]"

        return try {
            val type = object : com.google.gson.reflect.TypeToken<List<Macro>>() {}.type
            com.google.gson.Gson().fromJson(macrosJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Ресивер для отслеживания установки приложений
    class AppLaunchReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val packageName = intent.data?.schemeSpecificPart
            packageName?.let {
                val macroIntent = Intent("APP_LAUNCHED").apply {
                    putExtra("package_name", it)
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(macroIntent)
            }
        }
    }

    private val localReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "APP_LAUNCHED" -> {
                    val packageName = intent.getStringExtra("package_name")
                    executeMacrosForTrigger("app_launch", packageName)
                }
            }
        }
    }
}