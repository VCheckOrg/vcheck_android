package com.vcheck.sdk.core.util.custom_views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.LinearLayout
import com.vcheck.sdk.core.R


class RectOverlayView : LinearLayout {

    private var bitmap: Bitmap? = null

    private var holeWidth: Int = 2
    private var holeHeight: Int = 2

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (bitmap == null) {
            createWindowFrame()
        }
        canvas.drawBitmap(bitmap!!, 0f, 0f, null)
    }

    private fun createWindowFrame() {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val osCanvas = Canvas(bitmap!!)
        val outerRectangle = RectF(0F, 0F, width.toFloat(), height.toFloat())

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = resources.getColor(R.color.vcheck_stream_ui_mask)
        paint.alpha = 99

        osCanvas.drawRect(outerRectangle, paint)
    }

    private fun drawTransparentRect() {

        val osCanvas = Canvas(bitmap!!)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = Color.TRANSPARENT
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)

        val centerX = (width / 2).toFloat()
        val centerY = (height / 2).toFloat()

        val left: Int = (centerX - holeWidth / 2).toInt()
        val top: Int = (centerY - holeHeight / 2).toInt()
        val right: Int = (centerX + holeWidth / 2).toInt()
        val bottom: Int = (centerY + holeHeight / 2).toInt()

        osCanvas.drawRect(Rect(left, top, right, bottom), paint)
    }

    override fun isInEditMode(): Boolean {
        return true
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        bitmap = null
    }

    fun setRectHoleSize(width: Int, height: Int) {
        this.holeWidth = width
        this.holeHeight = height
        //createWindowFrame()
        drawTransparentRect()
    }
}