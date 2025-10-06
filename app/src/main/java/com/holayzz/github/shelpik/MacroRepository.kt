package com.holayzz.github.shelpik

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MacroRepository(private val context: Context) {

    private val gson = Gson()
    private val prefs: SharedPreferences = context.getSharedPreferences("macros", Context.MODE_PRIVATE)

    fun saveMacros(macros: List<Macro>) {
        val json = gson.toJson(macros)
        prefs.edit().putString("macros_list", json).apply()
    }

    fun loadMacros(): MutableList<Macro> {
        val json = prefs.getString("macros_list", "[]") ?: "[]"
        return try {
            val type = object : TypeToken<List<Macro>>() {}.type
            val loaded = gson.fromJson<List<Macro>>(json, type) ?: emptyList()
            loaded.toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
    }
}