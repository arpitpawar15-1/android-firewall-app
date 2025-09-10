
package com.example.firewall.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.example.firewall.MainActivity
import com.example.firewall.R

class FirewallVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null

    companion object {
        const val ACTION_START = "com.example.firewall.service.START"
        const val ACTION_STOP = "com.example.firewall.service.STOP"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForegroundService()
                setupVpn()
                // TODO: Start a thread to handle network traffic
            }
            ACTION_STOP -> {
                stopVpn()
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val channelId = "firewall_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Firewall Status", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Firewall Active")
            .setContentText("Your device is protected.")
            .setSmallIcon(R.mipmap.ic_launcher) // You need to add an icon here
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    private fun setupVpn() {
        if (vpnInterface == null) {
            val builder = Builder()
            builder.addAddress("10.0.0.2", 32)
            builder.addRoute("0.0.0.0", 0)
            // TODO: Add DNS server
            // builder.addDnsServer("8.8.8.8")
            // TODO: Add allowed/disallowed applications

            vpnInterface = builder.establish()
        }
    }

    private fun stopVpn() {
        vpnInterface?.close()
        vpnInterface = null
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }
}
