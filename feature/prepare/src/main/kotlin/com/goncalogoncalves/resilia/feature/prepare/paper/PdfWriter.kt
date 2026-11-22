package com.goncalogoncalves.resilia.feature.prepare.paper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withTranslation
import com.goncalogoncalves.resilia.core.designsystem.R as DsR
import java.io.File

/**
 * Minimal flowing-text PDF writer on top of android.graphics.pdf.PdfDocument (decision §6.3:
 * no external library). A4 portrait, Atkinson Hyperlegible (bundled, OFL), black on white.
 * Units are PostScript points (1/72 inch).
 */
internal class PdfWriter(context: Context, private val footer: String) {

    private val doc = PdfDocument()
    private var page: PdfDocument.Page? = null
    private var pageNumber = 0
    var y = MARGIN
        private set

    val regular: Typeface = ResourcesCompat.getFont(context, DsR.font.atkinson_hyperlegible_regular) ?: Typeface.DEFAULT
    val bold: Typeface = ResourcesCompat.getFont(context, DsR.font.atkinson_hyperlegible_bold) ?: Typeface.DEFAULT_BOLD

    val canvas: Canvas get() = (page ?: newPage()).canvas

    fun paint(size: Float, isBold: Boolean = false) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = if (isBold) bold else regular
        textSize = size
        color = Color.BLACK
    }

    fun layout(text: CharSequence, paint: TextPaint, width: Int, maxLines: Int = Int.MAX_VALUE): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.15f)
            .setMaxLines(maxLines)
            .setEllipsize(if (maxLines == Int.MAX_VALUE) null else TextUtils.TruncateAt.END)
            .build()

    /** Draws flowing text at the cursor, starting a new page when it does not fit. */
    fun text(value: String, size: Float = 10.5f, isBold: Boolean = false, indent: Float = 0f, after: Float = 4f) {
        if (value.isBlank()) return
        val l = layout(value, paint(size, isBold), (CONTENT_WIDTH - indent).toInt())
        ensure(l.height.toFloat())
        canvas.withTranslation(MARGIN + indent, y) { l.draw(this) }
        y += l.height + after
    }

    fun heading(value: String, size: Float = 15f) {
        ensure(size * 3) // keep a heading with at least a line of what follows
        y += 6f
        text(value, size, isBold = true, after = 6f)
    }

    /** A labelled answer; an empty answer becomes a line to fill in by hand. */
    fun field(label: String, value: String) {
        if (value.isNotBlank()) {
            text("$label: $value")
        } else {
            ensure(22f)
            text("$label:", after = 0f)
            canvas.drawLine(MARGIN, y + 14f, MARGIN + CONTENT_WIDTH, y + 14f, linePaint)
            y += 20f
        }
    }

    fun boxed(value: String, size: Float = 14f) {
        val l = layout(value, paint(size, isBold = true), (CONTENT_WIDTH - 20).toInt())
        val h = l.height + 16f
        ensure(h + 6)
        canvas.drawRect(RectF(MARGIN, y, MARGIN + CONTENT_WIDTH, y + h), boxPaint)
        canvas.withTranslation(MARGIN + 10, y + 8) { l.draw(this) }
        y += h + 10
    }

    fun space(points: Float) { y += points }

    fun ensure(height: Float) {
        if (page == null || y + height > PAGE_HEIGHT - MARGIN - FOOTER_SPACE) newPage()
    }

    fun newPage(): PdfDocument.Page {
        page?.let { finish(it) }
        pageNumber++
        val p = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        page = p
        y = MARGIN
        return p
    }

    private fun finish(p: PdfDocument.Page) {
        val l = layout("$footer $pageNumber", paint(8f), CONTENT_WIDTH.toInt(), maxLines = 2)
        p.canvas.withTranslation(MARGIN, PAGE_HEIGHT - MARGIN - l.height + 10) { l.draw(this) }
        doc.finishPage(p)
    }

    fun writeTo(file: File) {
        page?.let { finish(it) }
        page = null
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
    }

    companion object {
        const val PAGE_WIDTH = 595 // A4
        const val PAGE_HEIGHT = 842
        const val MARGIN = 40f
        const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN
        private const val FOOTER_SPACE = 24f

        /** ISO/IEC 7810 ID-1 (bank card): 85.6 × 53.98 mm. */
        const val CARD_WIDTH = 85.6f / 25.4f * 72f
        const val CARD_HEIGHT = 53.98f / 25.4f * 72f

        val linePaint = Paint().apply { color = Color.GRAY; strokeWidth = 0.6f }
        val boxPaint = Paint().apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 1.5f }
        val cutPaint = Paint().apply {
            color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 0.6f
            pathEffect = DashPathEffect(floatArrayOf(4f, 3f), 0f)
        }
        val foldPaint = Paint().apply {
            color = Color.GRAY; strokeWidth = 0.6f
            pathEffect = DashPathEffect(floatArrayOf(1f, 2f), 0f)
        }
    }
}
