package com.holayzz.github.shelpik

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class MacroAdapter(
    private var macros: List<Macro>,
    private val onItemClick: (Int) -> Unit
) : BaseAdapter() {

    fun updateMacros(newMacros: List<Macro>) {
        this.macros = newMacros
        notifyDataSetChanged()
    }

    override fun getCount(): Int = macros.size

    override fun getItem(position: Int): Macro = macros[position]

    override fun getItemId(position: Int): Long = macros[position].id

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)

        val macro = getItem(position)
        val textView = view.findViewById<TextView>(android.R.id.text1)

        val status = if (macro.isEnabled) "✅" else "❌"
        val displayText = "$status ${macro.getTriggerEmoji()} ${macro.name}\nAction: ${macro.getActionDisplayName()}"

        textView.text = displayText
        textView.setTextColor(Color.WHITE)
        textView.setBackgroundColor(Color.parseColor("#2E2E2E"))

        // Make the entire item clickable
        view.setOnClickListener {
            if (position >= 0 && position < macros.size) {
                onItemClick(position)
            }
        }

        return view
    }
}