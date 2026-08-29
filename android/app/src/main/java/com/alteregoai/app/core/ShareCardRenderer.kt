package com.alteregoai.app.core

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import com.alteregoai.app.data.AlterEgoProfileEntity
import java.io.File
import java.io.FileOutputStream

object ShareCardRenderer {
    fun shareText(context: Context, text: String) {
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, "Share with"))
    }

    fun renderAndShare(context: Context, text: String, alterEgo: AlterEgoProfileEntity?) {
        val bitmap = createBitmap(1080, 1350, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val background = Paint().apply { shader = LinearGradient(0f, 0f, 1080f, 1350f, Color.rgb(7, 8, 18), Color.rgb(38, 18, 88), Shader.TileMode.CLAMP) }
        canvas.drawRect(0f, 0f, 1080f, 1350f, background)
        val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(140, 255, 255); typeface = android.graphics.Typeface.DEFAULT_BOLD }
        canvas.drawText(AppConstants.appName, 72f, 110f, accent.apply { textSize = 34f })
        canvas.drawText(AppConstants.viralHook, 72f, 155f, accent.apply { textSize = 22f; alpha = 175 })
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 64f; typeface = android.graphics.Typeface.DEFAULT_BOLD }
        drawWrapped(canvas, text.ifBlank { "Day 7 of becoming my alter ego" }, bodyPaint, 72, 270, 936)
        alterEgo?.let {
            val circle = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(140, 255, 255) }
            canvas.drawCircle(125f, 1065f, 52f, circle)
            canvas.drawText(it.alterEgoName.take(1).uppercase(), 107f, 1088f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(5, 6, 13); textSize = 42f; typeface = android.graphics.Typeface.DEFAULT_BOLD })
            canvas.drawText(it.alterEgoName, 205f, 1055f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 32f; typeface = android.graphics.Typeface.DEFAULT_BOLD })
            canvas.drawText("Level ${it.level} ${it.stage().title}", 205f, 1100f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.LTGRAY; textSize = 24f })
        }
        val folder = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(folder, "alter-ego-card.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "image/png"; putExtra(Intent.EXTRA_STREAM, uri); putExtra(Intent.EXTRA_TEXT, text); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, "Share image card with"))
    }

    private fun drawWrapped(canvas: Canvas, text: String, paint: TextPaint, x: Int, y: Int, width: Int) {
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width).setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(0f, 1.12f).build().draw(canvas.apply { save(); translate(x.toFloat(), y.toFloat()) })
        canvas.restore()
    }
}
