package com.karafon.app.core.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Manages runtime permissions for the app
 */
class PermissionManager(private val context: Context) {

    companion object {
        val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            )
        }

        const val PERMISSION_REQUEST_CODE = 1001
    }

    /**
     * Check if all required permissions are granted
     */
    fun hasAllPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Check if microphone permission is granted
     */
    fun hasMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if bluetooth permission is granted
     */
    fun hasBluetoothPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true // Not required on older versions
    }

    /**
     * Get list of missing permissions
     */
    fun getMissingPermissions(): List<String> {
        return REQUIRED_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Get user-friendly permission names
     */
    fun getPermissionDisplayName(permission: String): String {
        return when (permission) {
            Manifest.permission.RECORD_AUDIO -> "Микрофон"
            Manifest.permission.MODIFY_AUDIO_SETTINGS -> "Настройки звука"
            Manifest.permission.BLUETOOTH_CONNECT -> "Bluetooth"
            else -> "Неизвестное разрешение"
        }
    }

    /**
     * Get permission rationale message
     */
    fun getPermissionRationale(permission: String): String {
        return when (permission) {
            Manifest.permission.RECORD_AUDIO ->
                "Для работы караоке необходим доступ к микрофону для передачи вашего голоса."
            Manifest.permission.BLUETOOTH_CONNECT ->
                "Для подключения к Bluetooth-колонкам и наушникам необходимо разрешение."
            Manifest.permission.MODIFY_AUDIO_SETTINGS ->
                "Для управления звуком необходимо разрешение на изменение настроек аудио."
            else ->
                "Это разрешение необходимо для работы приложения."
        }
    }
}
