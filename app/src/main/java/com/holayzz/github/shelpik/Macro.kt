package com.holayzz.github.shelpik

import java.io.Serializable

data class Macro(
    val id: Long,
    val name: String,
    val trigger: String,
    val action: String,
    val targetApp: String,
    val extraData: String? = null,
    val isEnabled: Boolean = true
) : Serializable