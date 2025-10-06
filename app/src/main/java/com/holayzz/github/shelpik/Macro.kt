package com.holayzz.github.shelpik

import java.io.Serializable

data class Macro(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val trigger: String,
    val action: String,
    val targetApp: String = "",
    val extraData: String? = null,
    val isEnabled: Boolean = true
) : Serializable {

    fun getDisplayName(): String {
        val triggerName = getTriggerDisplayName()
        val actionName = getActionDisplayName()
        return "$triggerName → $actionName"
    }

    fun getTriggerDisplayName(): String = when (trigger) {
        TriggerTypes.APP_LAUNCH -> "App Launched"
        TriggerTypes.PHONE_UNLOCK -> "Phone Unlocked"
        TriggerTypes.POWER_CONNECTED -> "Power Connected"
        TriggerTypes.POWER_DISCONNECTED -> "Power Disconnected"
        TriggerTypes.SCREEN_ON -> "Screen On"
        TriggerTypes.HEADSET_CONNECTED -> "Headset Connected"
        TriggerTypes.HEADSET_DISCONNECTED -> "Headset Disconnected"
        TriggerTypes.SPECIFIC_TIME -> "Specific Time"
        else -> "Custom Trigger"
    }

    fun getActionDisplayName(): String = when (action) {
        ActionTypes.CLOSE_APP -> "Close App"
        ActionTypes.CALL_NUMBER -> "Call Number"
        ActionTypes.SHOW_NOTIFICATION -> "Show Notification"
        ActionTypes.OPEN_WEBSITE -> "Open Website"
        ActionTypes.LAUNCH_APP -> "Launch App"
        ActionTypes.PLAY_SOUND -> "Play Sound"
        ActionTypes.TOGGLE_SETTINGS -> "Toggle Settings"
        ActionTypes.VIBRATE -> "Vibrate"
        ActionTypes.SEND_BROADCAST -> "Send Broadcast"
        else -> action
    }

    fun getTriggerEmoji(): String = when (trigger) {
        TriggerTypes.APP_LAUNCH -> "📱"
        TriggerTypes.PHONE_UNLOCK -> "🔓"
        TriggerTypes.POWER_CONNECTED -> "🔌"
        TriggerTypes.POWER_DISCONNECTED -> "🔋"
        TriggerTypes.SCREEN_ON -> "📲"
        TriggerTypes.HEADSET_CONNECTED -> "🎧"
        TriggerTypes.HEADSET_DISCONNECTED -> "🎧"
        TriggerTypes.SPECIFIC_TIME -> "⏰"
        else -> "⚡"
    }
}