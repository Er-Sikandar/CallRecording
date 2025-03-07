package com.cr.callrecording

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cr.callrecording.R

class BackgroundRecordingService : Service() {
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var callStateListener: CallStateListener

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "RecordingChannel"
        var isRunning = false
    }
    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }
    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        callStateListener = CallStateListener(this)

        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        Log.e("TAG", "onStartCommand: ")
        return START_STICKY
    }

    override fun onDestroy() {
        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_NONE)
        isRunning=false
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        createNotificationChannel()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Call Recording Service")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = CHANNEL_ID
            val channelName = "Recording Channel"
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(notificationChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
