package com.holayzz.github.shelpik

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnMacros = findViewById<Button>(R.id.btnMacros)
        val btnTweaker = findViewById<Button>(R.id.btnTweaker)
        val btnApkEditor = findViewById<Button>(R.id.btnApkEditor)
        val btnInfo = findViewById<Button>(R.id.btnInfo)

        btnMacros.setOnClickListener {
            startActivity(Intent(this, MacrosActivity::class.java))
        }

        btnTweaker.setOnClickListener {
            startActivity(Intent(this, TweakerActivity::class.java))
        }

        btnApkEditor.setOnClickListener {
            startActivity(Intent(this, ApkEditorActivity::class.java))
        }

        btnInfo.setOnClickListener {
            startActivity(Intent(this, InfoActivity::class.java))
        }
    }
}