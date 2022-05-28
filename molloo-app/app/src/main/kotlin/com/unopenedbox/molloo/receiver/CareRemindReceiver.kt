package com.unopenedbox.molloo.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.unopenedbox.molloo.R
import com.unopenedbox.molloo.ui.compose.MainCompose

class CareRemindReceiver : BroadcastReceiver() {
  companion object {
    const val CARE_TYPE = "care_type"
    const val ITEM_ID = "item_id"
    const val DAYS_LEFT = "days_left"
  }
  private lateinit var notificationManager: NotificationManagerCompat

  private val notificationId = 12001
  private val notificationChannelId = "health_check_channel"

  override fun onReceive(context: Context, intent: Intent) {
    val careType = intent.getStringExtra(CARE_TYPE) ?: context.getString(R.string.care_type_default)
    val itemId = intent.getLongExtra(ITEM_ID, -1)
    val daysLeft = intent.getIntExtra(DAYS_LEFT, -1)

    notificationManager = NotificationManagerCompat.from(context)
    createNotificationChannel(context)
    deliverNotification(context, careType, daysLeft)
  }

  private fun createNotificationChannel(context:Context) {
    notificationManager.createNotificationChannel(
      NotificationChannelCompat.Builder(
        notificationChannelId,
        NotificationManager.IMPORTANCE_HIGH,
      ).apply {
        setName(context.getString(R.string.care_reminder_noti_channel_title))
        setDescription(context.getString(R.string.care_reminder_noti_channel_description))
        setVibrationEnabled(true)
      }.build()
    )
  }

  private fun deliverNotification(context: Context, careType:String, daysLeft:Int) {
    val contentIntent = Intent(context, MainCompose::class.java)
    val contentPendingIntent = PendingIntent.getActivity(context, notificationId, contentIntent, PendingIntent.FLAG_IMMUTABLE)

    val builder = NotificationCompat.Builder(context, notificationChannelId).apply {
      setSmallIcon(IconCompat.createWithResource(context, R.drawable.ic_simple_hospital))
      setContentTitle(context.getString(if (daysLeft == 0) R.string.care_reminder_noti_title else R.string.care_reminder_noti_close_title).format(careType))
      setContentText(if (daysLeft == 0) {
        context.getString(R.string.care_reminder_noti_description)
      } else {
        context.getString(R.string.care_reminder_noti_close_description).format(daysLeft)
      })
      setContentIntent(contentPendingIntent)
      priority = NotificationCompat.PRIORITY_HIGH
      setAutoCancel(false)
      setDefaults(NotificationCompat.DEFAULT_ALL)
    }

    notificationManager.notify(notificationId + careType.hashCode() % 10000, builder.build())
  }
}