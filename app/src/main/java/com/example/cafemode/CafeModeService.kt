
package com.example.cafemode

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class CafeModeService : Service() {
    private var audioProcessor: CafeModeAudioProcessor? = null
    private lateinit var notificationManager: NotificationManager

    companion object {
        const val ACTION_UPDATE_INTENSITY = "UPDATE_INTENSITY"
        const val ACTION_UPDATE_SPATIAL = "UPDATE_SPATIAL"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "CafeModeChannel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializeAudioProcessor()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_UPDATE_INTENSITY -> {
                val intensity = intent.getFloatExtra("intensity", 0.5f)
                audioProcessor?.updateIntensity(intensity)
            }
            ACTION_UPDATE_SPATIAL -> {
                val spatial = intent.getFloatExtra("spatial", 0.5f)
                audioProcessor?.updateSpatialWidth(spatial)
            }
            else -> {
                startForeground(NOTIFICATION_ID, createNotification())
                audioProcessor?.enable()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        audioProcessor?.disable()
        audioProcessor?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun initializeAudioProcessor() {
        try {
            audioProcessor = CafeModeAudioProcessor(AudioEffect.EFFECT_TYPE_NULL)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Café Mode",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Audio processing for café ambience"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Café Mode Active")
            .setContentText("Processing audio for ambient café experience")
            .setSmallIcon(R.drawable.ic_cafe)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}