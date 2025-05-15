package com.tacoclient.launcher

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log

class Importer : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    @SuppressLint("IntentReset")
    private fun handleDeepLink(originalIntent: Intent) {
        val newIntent = Intent(originalIntent)

        if (isMcRunning) {
            newIntent.setClassName(this, "com.mojang.minecraftpe.Launcher")
        } else {
            prepareLauncher {
               newIntent.apply {
                    putStringArrayListExtra("APKS", it)
                    flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
               }
            }
        }

        startActivity(newIntent)
        finish()
    }

    private val isMcRunning: Boolean
        get() {
            try {
                val clazz = Class.forName(
                    "com.mojang.minecraftpe.Launcher",
                    false,
                    classLoader
                )
                Log.d("Reflection", "Minecraft PE Launcher class exists!")
                return true
            } catch (e: ClassNotFoundException) {
                Log.d("Reflection", "Minecraft PE Launcher class not found.")
                return false
            }
        }
}
