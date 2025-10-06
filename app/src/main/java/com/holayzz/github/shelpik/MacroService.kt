package com.holayzz.github.shelpik

import android.app.*
import android.content.*
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MacroService : Service() {

    private val CHANNEL_ID = "MacroServiceChannel"
    private val NOTIFICATION_ID = 1

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("MacroService", "Received action: ${intent.action}")

            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> {
                    executeMacrosForTrigger(TriggerTypes.SCREEN_ON)
                }
                Intent.ACTION_USER_PRESENT -> {
                    executeMacrosForTrigger(TriggerTypes.PHONE_UNLOCK)
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    executeMacrosForTrigger(TriggerTypes.POWER_CONNECTED)
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    executeMacrosForTrigger(TriggerTypes.POWER_DISCONNECTED)
                }
                Intent.ACTION_HEADSET_PLUG -> {
                    val state = intent.getIntExtra("state", -1)
                    if (state == 1) {
                        executeMacrosForTrigger(TriggerTypes.HEADSET_CONNECTED)
                    } else if (state == 0) {
                        executeMacrosForTrigger(TriggerTypes.HEADSET_DISCONNECTED)
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

        val macros = loadMacros().filter { macro ->
            macro.trigger == trigger && macro.isEnabled &&
                    (packageName == null || macro.targetApp == packageName)
        }

        macros.forEach { macro ->
            Log.d("MacroService", "Executing macro: ${macro.name}")
            executeMacroAction(macro)
        }
    }

    private fun executeMacroAction(macro: Macro) {
        try {
            when (macro.action) {
                ActionTypes.CLOSE_APP -> closeApp(macro.targetApp)
                ActionTypes.CALL_NUMBER -> callNumber(macro.extraData ?: "+1234567890")
                ActionTypes.SHOW_NOTIFICATION -> showNotification(
                    macro.extraData ?: "Macro",
                    "Macro executed: ${macro.name}"
                )
                ActionTypes.OPEN_WEBSITE -> openWebsite(macro.extraData ?: "https://google.com")
                ActionTypes.LAUNCH_APP -> launchApp(macro.targetApp)
                ActionTypes.TOGGLE_SETTINGS -> toggleSettings(macro.extraData ?: "wifi")
                ActionTypes.VIBRATE -> vibrate(macro.extraData?.toLongOrNull() ?: 500)
                ActionTypes.SEND_BROADCAST -> sendCustomBroadcast(macro.extraData ?: "")
            }
        } catch (e: Exception) {
            Log.e("MacroService", "Error executing macro: ${e.message}")
        }
    }

    private fun closeApp(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MacroService", "Cannot close app: ${e.message}")
        }
    }

    private fun callNumber(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$phoneNumber")
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
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
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

    private fun toggleSettings(setting: String) {
        try {
            when (setting) {
                "wifi" -> {
                    val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
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
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
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
            val type = object : TypeToken<List<Macro>>() {}.type
            Gson().fromJson<List<Macro>>(macrosJson, type) ?: emptyList()
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
                    executeMacrosForTrigger(TriggerTypes.APP_LAUNCH, packageName)
                }
            }
        }
    }
}