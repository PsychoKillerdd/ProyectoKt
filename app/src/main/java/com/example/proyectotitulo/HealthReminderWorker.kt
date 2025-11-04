package com.example.proyectotitulo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlin.random.Random

class HealthReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        const val CHANNEL_ID = "health_reminders"
        const val NOTIFICATION_ID = 1001
    }

    private val motivationalMessages = listOf(
        "💪 ¡Es hora de medir tu salud! Toma tu oxígeno en sangre y frecuencia cardíaca.",
        "❤️ Registra tus datos de salud ahora. Tu bienestar es importante.",
        "🩺 ¡Momento de chequeo! Mide tu SpO2 y ritmo cardíaco, guárdalos en la app.",
        "⏰ Recordatorio de salud: Abre la app y registra tus signos vitales.",
        "🌟 ¡Cuida tu salud! Toma tus mediciones de oxígeno y frecuencia cardíaca.",
        "💙 Tu salud importa. Registra tu saturación de oxígeno y ritmo cardíaco ahora.",
        "📊 ¡Construye tu historial! Mide y guarda tus datos de salud.",
        "🎯 ¡Sigue adelante! Registra tus signos vitales para mantener el control.",
        "✨ Mantén tu racha. Mide tu SpO2 y frecuencia cardíaca ahora.",
        "🏃 ¡Actúa ahora! Toma tus mediciones y guarda los datos en tu perfil.",
        "🔔 Es momento de cuidarte. Registra tu oxígeno en sangre y RC.",
        "💚 Tu compromiso con la salud empieza ahora. ¡Registra tus datos!",
        "🌈 Cada medición cuenta. Toma tu SpO2 y frecuencia cardíaca.",
        "⚡ ¡No lo olvides! Mide tus signos vitales y guárdalos.",
        "🎉 ¡Hora del chequeo! Oxígeno en sangre + RC = Datos guardados."
    )

    override fun doWork(): Result {
        createNotificationChannel()
        showNotification()
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de Salud"
            val descriptionText = "Notificaciones para recordarte registrar tus datos de salud"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification() {
        val intent = Intent(applicationContext, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Seleccionar mensaje aleatorio
        val message = motivationalMessages[Random.nextInt(motivationalMessages.size)]

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("HealthTrack - Recordatorio")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
