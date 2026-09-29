package com.redwings.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.test.core.app.ApplicationProvider
import com.redwings.widget.widget.RedWingsWidgetProvider
import com.redwings.widget.widget.WidgetBinder
import com.redwings.widget.widget.WidgetTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class WidgetScreenshotTest {

    @Test
    fun renderActualWidgetToImage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val provider = RedWingsWidgetProvider()
        val views = RemoteViews(context.packageName, R.layout.red_wings_widget_layout)

        // Bind with Home (Frosted Ice) theme
        provider.applyWidgetTheme(context, views, WidgetTheme.HOME)
        WidgetBinder.bindEmptyState(context, views, WidgetTheme.HOME)
        provider.applyResponsiveLayout(context, views, 360, 200)

        // Inflate in a container
        val root = FrameLayout(context)
        val inflated = views.apply(context, root)
        root.addView(inflated)

        // Measure and layout at 720x400 (scale 2x for standard 360x200 dp)
        val width = 720
        val height = 400
        root.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        root.layout(0, 0, width, height)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        root.draw(canvas)

        val outputFile = File("/home/andre/Downloads/actual_widget_render.png")
        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }
}
