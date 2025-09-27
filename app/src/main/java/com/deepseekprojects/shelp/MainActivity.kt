@file:Suppress("DEPRECATION")

package com.deepseekprojects.shelp

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var deviceInfoText: TextView
    private lateinit var refreshButton: Button

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Находим наши View элементы
        deviceInfoText = findViewById(R.id.deviceInfoText)
        refreshButton = findViewById(R.id.refreshButton)

        // Обновляем информацию при запуске
        updateSystemInfo()

        // Обработчик кнопки обновления
        refreshButton.setOnClickListener {
            updateSystemInfo()
        }
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun updateSystemInfo() {
        val info = StringBuilder()

        // 1. Информация о производителе и модели
        info.append("📱 Устройство:\n")
        info.append("• Производитель: ${Build.MANUFACTURER}\n")
        info.append("• Модель: ${Build.MODEL}\n")
        info.append("• Бренд: ${Build.BRAND}\n")
        info.append("• Продукт: ${Build.PRODUCT}\n")
        info.append("• Устройство: ${Build.DEVICE}\n\n")

        // 2. Информация об Android
        info.append("🤖 Android:\n")
        info.append("• Версия: ${Build.VERSION.RELEASE}\n")
        info.append("• API Level: ${Build.VERSION.SDK_INT}\n")
        info.append("• Кодовое имя: ${Build.VERSION.CODENAME}\n")
        info.append("• Базовый уровень безопасности: ${Build.VERSION.SECURITY_PATCH}\n\n")

        // 3. Информация о железе
        info.append("⚙️ Железо:\n")
        info.append("• Процессор: ${Build.CPU_ABI}\n")
        info.append("• Аппаратная платформа: ${Build.HARDWARE}\n")
        info.append("• Платформа: ${Build.BOARD}\n")
        info.append("• Загрузчик: ${Build.BOOTLOADER}\n\n")

        // 4. Информация о сборке
        info.append("🏗️ Сборка:\n")
        info.append("• ID сборки: ${Build.ID}\n")
        info.append("• Тип сборки: ${Build.TYPE}\n")
        info.append("• Теги: ${Build.TAGS}\n")
        info.append("• Время сборки: ${SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(Date(Build.TIME))}\n\n")

        // 5. Информация о экране (примерная)
        info.append("📱 Экран:\n")
        val displayMetrics = resources.displayMetrics
        info.append("• Разрешение: ${displayMetrics.widthPixels} x ${displayMetrics.heightPixels} px\n")
        info.append("• Плотность: ${displayMetrics.density}dpi\n")
        info.append("• Масштаб: ${displayMetrics.scaledDensity}\n\n")

        // 6. Информация о памяти (базовая)
        info.append("💾 Память:\n")
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory() / (1024 * 1024)
        val freeMemory = runtime.freeMemory() / (1024 * 1024)
        val maxMemory = runtime.maxMemory() / (1024 * 1024)

        info.append("• Всего памяти JVM: $totalMemory MB\n")
        info.append("• Свободно памяти JVM: $freeMemory MB\n")
        info.append("• Макс. память JVM: $maxMemory MB\n")

        // Устанавливаем текст
        deviceInfoText.text = info.toString()
    }
}