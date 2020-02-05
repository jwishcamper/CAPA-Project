package com.example.capaproject

import android.appwidget.AppWidgetProviderInfo
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetHost
import android.content.Context


class WidgetHost(context: Context, hostId: Int) : AppWidgetHost(context, hostId) {
    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo
    ): AppWidgetHostView {
        // pass back our custom AppWidgetHostView
        return WidgetView(context)
    }
}