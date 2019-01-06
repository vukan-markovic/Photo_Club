package vukan.com.photoclub

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import androidx.navigation.NavDeepLinkBuilder

class DeepLinkAppWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val remoteViews = RemoteViews(
            context.packageName,
            R.layout.deep_link_appwidget
        )

        val pendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.app_navigation)
            .setDestination(R.id.main_fragment)
            .createPendingIntent()

        remoteViews.setOnClickPendingIntent(R.id.widget_image, pendingIntent)
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews)
    }
}