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

        WidgetBinder.logoBitmapLoader = { _, url ->
            val bmp = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            val isDet = url.contains("det", ignoreCase = true)
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isDet) android.graphics.Color.parseColor("#CE1126") else android.graphics.Color.parseColor("#00205B")
                style = android.graphics.Paint.Style.FILL
            }
            canvas.drawCircle(60f, 60f, 56f, paint)
            val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = 34f
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            canvas.drawText(if (isDet) "DET" else "TOR", 60f, 72f, textPaint)
            bmp
        }

        context.getSharedPreferences(WidgetBinder.PREFS, Context.MODE_PRIVATE).edit()
            .putString(
                "atlantic_standings",
                "1. FLA • 2. BOS • 3. TOR • 4. TBL • 5. DET • 6. BUF • 7. OTT • 8. MTL"
            )
            .commit()

        // 1. Home Theme Outer Screen 4x2
        renderWidget(
            context = context,
            provider = provider,
            layoutResId = R.layout.red_wings_widget_layout,
            theme = WidgetTheme.HOME,
            widthDp = 340,
            heightDp = 150,
            outputFiles = listOf(
                File("/home/andre/Downloads/actual_widget_render_outer_4x2.png")
            )
        )

        // 2. Away Theme Outer Screen 4x2
        renderWidget(
            context = context,
            provider = provider,
            layoutResId = R.layout.red_wings_widget_layout,
            theme = WidgetTheme.AWAY,
            widthDp = 340,
            heightDp = 150,
            outputFiles = listOf(
                File("/home/andre/Downloads/actual_widget_render_away_4x2.png")
            )
        )

        // 3. Heritage Theme Outer Screen 4x2
        renderWidget(
            context = context,
            provider = provider,
            layoutResId = R.layout.red_wings_widget_layout,
            theme = WidgetTheme.HERITAGE,
            widthDp = 340,
            heightDp = 150,
            outputFiles = listOf(
                File("/home/andre/Downloads/actual_widget_render_heritage_4x2.png")
            )
        )

        // 4. Retro Theme Outer Screen 4x2
        renderWidget(
            context = context,
            provider = provider,
            layoutResId = R.layout.red_wings_widget_layout,
            theme = WidgetTheme.REVERSE_RETRO,
            widthDp = 340,
            heightDp = 150,
            outputFiles = listOf(
                File("/home/andre/Downloads/actual_widget_render_retro_4x2.png")
            )
        )

        // 5. Stadium Series Theme Outer Screen 4x2
        renderWidget(
            context = context,
            provider = provider,
            layoutResId = R.layout.red_wings_widget_layout,
            theme = WidgetTheme.STADIUM_SERIES,
            widthDp = 340,
            heightDp = 150,
            outputFiles = listOf(
                File("/home/andre/Downloads/actual_widget_render_stadium_4x2.png")
            )
        )

        // 6. Inner Unfolded Screen 4x2 Render (red_wings_widget_wide)
        renderWidget(
            context = context,
            provider = provider,
            layoutResId = R.layout.red_wings_widget_wide,
            theme = WidgetTheme.HOME,
            widthDp = 540,
            heightDp = 170,
            outputFiles = listOf(
                File("/home/andre/Downloads/actual_widget_render_inner_4x2.png")
            )
        )

        WidgetBinder.logoBitmapLoader = null
    }

    private fun renderWidget(
        context: Context,
        provider: RedWingsWidgetProvider,
        layoutResId: Int,
        theme: WidgetTheme,
        widthDp: Int,
        heightDp: Int,
        outputFiles: List<File>
    ) {
        val views = RemoteViews(context.packageName, layoutResId)
        provider.applyWidgetTheme(context, views, theme)
        val game = WidgetBinder.WidgetGame(
            opponentName = "Toronto Maple Leafs",
            opponentAbbrev = "TOR",
            gameTimeMillis = System.currentTimeMillis() + 86400000L * 2 + 14 * 3600000L + 22 * 1000L,
            isHomeGame = true,
            venue = "Little Caesars Arena",
            standingLine = "4th in Atlantic • 94 pts",
            h2hLine = "DET leads 2-1",
            awayRecord = "20-8-6",
            homeRecord = "22-7-5"
        )
        val prefs = context.getSharedPreferences(WidgetBinder.PREFS, Context.MODE_PRIVATE)
        kotlinx.coroutines.runBlocking {
            WidgetBinder.bindGameData(context, views, game, theme)
        }
        provider.bindStandings(context, views, prefs, theme)
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
