package com.holayzz.github.shelpik

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val tvVersion = findViewById<TextView>(R.id.tvVersion)
        val btnGitHub = findViewById<Button>(R.id.btnGitHub)

        // Устанавливаем версию приложения
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            tvVersion.text = "Ver: ${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: Exception) {
            tvVersion.text = "Ver: ???"
        }

        btnGitHub.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/holayzz/Shelpik"))
            startActivity(intent)
        }
    }
}