package io.github.defolters.homeapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.paperdb.Paper

class HomeFirebaseMessagingService : FirebaseMessagingService() {

    private val channelId: String = "HOMEAPP"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage?.data?.isNotEmpty()?.let { _ ->

            val pendingIntent = PendingIntent.getActivity(
                this, 0, Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_ONE_SHOT
            )

            remoteMessage.notification?.let { it ->
                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_noun_humidity)
                    .setColor(getColor(R.color.colorPrimary))
                    .setContentTitle(it.title)
                    .setContentText(it.body)
                    .setPriority(PRIORITY_MAX)
                    .setChannelId(channelId)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)

                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        channelId,
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    notificationManager.createNotificationChannel(channel)
                }

                val isNotificationOn = Paper.book().read<Boolean>("NOTIFICATION")
                if (isNotificationOn) {
                    notificationManager.notify(0, notificationBuilder.build())
                    sendBroadcast(Intent("BROADCAST"))
                }

            }

        }
    }
}
