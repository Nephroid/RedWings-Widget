package com.redwings.widget.widget

import android.content.Context
import android.os.Parcel
import android.util.Log
import android.widget.RemoteViews
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.redwings.widget.R

object WidgetCrashGuard {

    const val MAX_SAFE_PARCEL_BYTES = 800 * 1024 // 800 KB safe ceiling

    /**
     * Validates whether a RemoteViews instance is safe to pass across the Android Binder IPC.
     * Prevents TransactionTooLargeException.
     * Returns true if safe; false if oversized (and logs diagnostics to Crashlytics).
     */
    fun validateRemoteViews(views: RemoteViews, widgetId: Int): Boolean {
        val parcel = Parcel.obtain()
        return try {
            views.writeToParcel(parcel, 0)
            val sizeBytes = parcel.dataSize()

            try {
                FirebaseCrashlytics.getInstance().setCustomKey("widget_last_parcel_bytes", sizeBytes)
            } catch (_: Exception) {}

            if (sizeBytes > MAX_SAFE_PARCEL_BYTES) {
                Log.e("WidgetCrashGuard", "RemoteViews parcel size $sizeBytes exceeds safe ceiling $MAX_SAFE_PARCEL_BYTES bytes for widget $widgetId")
                try {
                    val crashlytics = FirebaseCrashlytics.getInstance()
                    crashlytics.setCustomKey("widget_oversized_id", widgetId)
                    crashlytics.setCustomKey("widget_oversized_bytes", sizeBytes)
                    crashlytics.recordException(
                        IllegalStateException("RemoteViews parcel size exceeded: $sizeBytes bytes for widget $widgetId")
                    )
                } catch (_: Exception) {}
                false
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e("WidgetCrashGuard", "Parcel serialization check failed: ${e.message}", e)
            true
        } finally {
            parcel.recycle()
        }
    }

    /**
     * Fallback emergency RemoteViews if regular views exceed IPC bounds or crash.
     */
    fun buildEmergencyViews(context: Context): RemoteViews {
        return RemoteViews(context.packageName, R.layout.red_wings_widget_layout).apply {
            setTextViewText(R.id.widget_title, "DETROIT RED WINGS")
            setTextViewText(R.id.widget_opponent, "SCHEDULE STANDBY")
            setTextViewText(R.id.widget_countdown, "-- : --")
        }
    }
}
