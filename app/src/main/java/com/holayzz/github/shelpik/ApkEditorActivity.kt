package com.holayzz.github.shelpik

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Button
import androidx.core.net.toUri

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

    @SuppressLint("SetTextI18n")
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
            tvEmpty.text = "Apps dont found :("
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
        } catch (_: Exception) {
            "Unknown"
        }
    }

    private fun showAppOptions(packageName: String, appName: String) {
        val options = arrayOf("Delete", "Show info", "Back")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Work with $appName")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> tryUninstallApp(packageName)
                    1 -> showAppInfo(packageName, appName)
                }
            }
            .show()
    }

    @SuppressLint("UseKtx")
    private fun tryUninstallApp(packageName: String) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_DELETE)
            intent.data = "package:$packageName".toUri()
            startActivity(intent)
        } catch (_: Exception) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Error! :0")
                .setMessage("Failed to start app uninstallation")
                .setPositiveButton("OK :(", null)
                .show()
        }
    }

    private fun showAppInfo(packageName: String, appName: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Info about app")
            .setMessage("Name: $appName\nPKG: $packageName")
            .setPositiveButton("OK", null)
            .show()
    }
}