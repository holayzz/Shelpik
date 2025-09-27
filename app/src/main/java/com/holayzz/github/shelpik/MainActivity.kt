package com.holayzz.github.shelpik

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Находим кнопки по ID
        val btnMacros = findViewById<Button>(R.id.btnMacros)
        val btnTweaker = findViewById<Button>(R.id.btnTweaker)
        val btnApkEditor = findViewById<Button>(R.id.btnApkEditor)

        btnMacros.setOnClickListener {
            val intent = Intent(this, MacrosActivity::class.java)
            startActivity(intent)
        }

        btnTweaker.setOnClickListener {
            val intent = Intent(this, TweakerActivity::class.java)
            startActivity(intent)
        }

        btnApkEditor.setOnClickListener {
            val intent = Intent(this, ApkEditorActivity::class.java)
            startActivity(intent)
        }
    }
}