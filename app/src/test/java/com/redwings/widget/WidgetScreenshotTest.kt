package com.redwings.widget

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

        context.getSharedPreferences(WidgetBinder.PREFS, Context.MODE_PRIVATE).edit()
            .putString("team_record_DET", "22-7-5")
            .putString("team_record_OPP", "20-8-6")
            .putString("next_game_opp", "TOR")
            .commit()

        // 1. Outer Screen 4x2 Render (red_wings_widget_layout)
        renderWidget(
            context = context,
            provider = provider,
            layoutResId = R.layout.red_wings_widget_layout,
            widthDp = 340,
            heightDp = 150,
            outputFiles = listOf(
                File("/home/andre/Downloads/actual_widget_render.png"),
                File("/home/andre/Downloads/actual_widget_render_outer_4x2.png")
            )
        )

        // 2. Inner Unfolded Screen 4x2 Render (red_wings_widget_wide)
        renderWidget(
            context = context,
            provider = provider,
            layoutResId = R.layout.red_wings_widget_wide,
            widthDp = 540,
            heightDp = 170,
            outputFiles = listOf(
                File("/home/andre/Downloads/actual_widget_render_inner_4x2.png")
            )
        )
    }

    private fun renderWidget(
        context: Context,
        provider: RedWingsWidgetProvider,
        layoutResId: Int,
        widthDp: Int,
        heightDp: Int,
        outputFiles: List<File>
    ) {
        val views = RemoteViews(context.packageName, layoutResId)
        provider.applyWidgetTheme(context, views, WidgetTheme.HOME)
        WidgetBinder.bindEmptyState(context, views, WidgetTheme.HOME)
        provider.applyResponsiveLayout(context, views, widthDp, heightDp)

        val root = FrameLayout(context)
        val inflated = views.apply(context, root)
        root.addView(inflated)

        val scale = 2
        val widthPx = widthDp * scale
        val heightPx = heightDp * scale

        root.measure(
            View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY)
        )
        root.layout(0, 0, widthPx, heightPx)

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        root.draw(canvas)

        outputFiles.forEach { file ->
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
    }
}
