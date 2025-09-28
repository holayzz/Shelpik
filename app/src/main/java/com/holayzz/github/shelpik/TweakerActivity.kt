package com.holayzz.github.shelpik

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.provider.Settings
import android.content.Intent

class TweakerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweaker)

        val btnEnableDevOptions = findViewById<Button>(R.id.btnEnableDevOptions)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        btnEnableDevOptions.setOnClickListener {
            enableDeveloperOptions()
            tvStatus.text = "Dev settings activated!\nGo to settings myboy."
        }
        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun enableDeveloperOptions() {
        try {
            // Попытка включить настройки разработчика
            Settings.Global.putInt(contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1)
            Settings.Global.putInt(contentResolver, Settings.Global.ADB_ENABLED, 1)

            // Открываем настройки разработчика
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            // Если не получается, просто открываем настройки
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }
    }
}