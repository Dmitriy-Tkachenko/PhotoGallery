package com.tkachenko.photogallery

import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

private const val TAG = "PollWorker"

class PollWorker(private val context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {

    override fun doWork(): Result {
        val query = QueryPreferences.getStoredQuery(context = context)
        val lastResultId = QueryPreferences.getLastResultId(context = context)
        val items: List<GalleryItem> = if (query.isEmpty()) {
            FlickrFetch().fetchPhotosRequest()
                .execute()
                .body()
                ?.galleryItems
        } else {
            FlickrFetch().searchPhotosRequest(query)
                .execute()
                .body()
                ?.galleryItems
        }?: emptyList()

        if (items.isEmpty()) {
            return Result.success()
        }

        val resultId = items.first().id
        if (resultId == lastResultId) {
            Log.i(TAG, "Got an old result: $resultId")
        } else {
            Log.i(TAG, "Got a new result: $resultId")
            QueryPreferences.setLastResultId(context = context, resultId)

            val intent = PhotoGalleryActivity.newIntent(context = context)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            val resources = context.resources
            val notification = NotificationCompat
                .Builder(context, NOTIFICATION_CHANNEL_ID)
                .setTicker(resources.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_pictures_title))
                .setContentText(resources.getString(R.string.new_pictures_text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(0, notification)
        }
        return Result.success()
    }
}