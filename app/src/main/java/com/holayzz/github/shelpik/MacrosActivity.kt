package com.holayzz.github.shelpik

import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView

class MacrosActivity : AppCompatActivity() {

    private lateinit var macrosListView: ListView
    private val macrosList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_macros)

        val btnAddMacro = findViewById<Button>(R.id.btnAddMacro)
        macrosListView = findViewById<ListView>(R.id.macrosListView)

        setupListView()

        btnAddMacro.setOnClickListener {
            showAddMacroDialog()
        }

        macrosListView.setOnItemClickListener { _, _, position, _ ->
            showMacroOptionsDialog(position)
        }
    }

    private fun setupListView() {
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, macrosList)
        macrosListView.adapter = adapter
    }

    private fun showAddMacroDialog() {
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appNames = apps.map { packageManager.getApplicationLabel(it).toString() }

        AlertDialog.Builder(this)
            .setTitle("Выберите приложение")
            .setItems(appNames.toTypedArray()) { _, which ->
                val selectedApp = apps[which]
                createMacro(selectedApp.packageName, packageManager.getApplicationLabel(selectedApp).toString())
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun createMacro(packageName: String, appName: String) {
        val macro = "Закрыть $appName при запуске"
        macrosList.add(macro)
        adapter.notifyDataSetChanged()
    }

    private fun showMacroOptionsDialog(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Действие с макросом")
            .setItems(arrayOf("Удалить", "Отключить")) { _, which ->
                when (which) {
                    0 -> {
                        macrosList.removeAt(position)
                        adapter.notifyDataSetChanged()
                    }
                    1 -> {
                        // Логика отключения макроса
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}