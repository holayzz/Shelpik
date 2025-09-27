package com.holayzz.github.shelpik

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView

class ApkEditorActivity : AppCompatActivity() {

    private lateinit var appsListView: ListView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apk_editor)

        appsListView = findViewById(R.id.appsListView)
        tvEmpty = findViewById(R.id.tvEmpty)

        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        val packageManager = packageManager
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        val appList = mutableListOf<String>()

        for (app in apps) {
            val appName = packageManager.getApplicationLabel(app).toString()
            val packageName = app.packageName
            val appSize = getAppSize(app)

            appList.add("$appName\nPackage: $packageName\nSize: $appSize")
        }

        if (appList.isEmpty()) {
            tvEmpty.text = "Приложения не найдены"
            appsListView.visibility = ListView.GONE
        } else {
            tvEmpty.visibility = TextView.GONE
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, appList)
            appsListView.adapter = adapter
        }

        appsListView.setOnItemClickListener { _, _, position, _ ->
            val selectedApp = apps[position]
            showAppOptions(selectedApp.packageName, packageManager.getApplicationLabel(selectedApp).toString())
        }
    }

    private fun getAppSize(app: android.content.pm.ApplicationInfo): String {
        return try {
            val apkFile = java.io.File(app.sourceDir)
            val sizeInMB = apkFile.length() / (1024 * 1024)
            "%.2f MB".format(sizeInMB)
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun showAppOptions(packageName: String, appName: String) {
        val options = arrayOf("Удалить приложение", "Показать информацию", "Отмена")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Действие с $appName")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> tryUninstallApp(packageName)
                    1 -> showAppInfo(packageName, appName)
                }
            }
            .show()
    }

    private fun tryUninstallApp(packageName: String) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_DELETE)
            intent.data = android.net.Uri.parse("package:$packageName")
            startActivity(intent)
        } catch (e: Exception) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Ошибка")
                .setMessage("Не удалось запустить удаление приложения")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun showAppInfo(packageName: String, appName: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Информация о приложении")
            .setMessage("Название: $appName\nПакет: $packageName")
            .setPositiveButton("OK", null)
            .show()
    }
}