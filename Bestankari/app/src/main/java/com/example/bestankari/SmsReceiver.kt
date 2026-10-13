package com.example.bestankari

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.Notification
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val body = parts.joinToString("") { it.messageBody ?: "" }
        if (!looksLikeBankTx(body)) return

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "tx"
        nm.createNotificationChannel(
            NotificationChannel(channelId, "تراکنش‌های بانکی", NotificationManager.IMPORTANCE_HIGH)
        )

        val open = Intent(context, MainActivity::class.java).apply {
            putExtra("sms", body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val pi = PendingIntent.getActivity(
            context, id, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val n = Notification.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("تراکنش جدید؛ برای ثبت توضیحات بزنید")
            .setContentText(body.lines().firstOrNull { it.contains("ریال") } ?: body)
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        nm.notify(id, n)
    }

    // فقط پیامک‌هایی که شبیه تراکنش بانکی‌اند
    private fun looksLikeBankTx(t: String): Boolean =
        t.contains("ریال") &&
            (t.contains("پرید") || t.contains("نشست") || t.contains("برداشت") || t.contains("واریز"))
}
