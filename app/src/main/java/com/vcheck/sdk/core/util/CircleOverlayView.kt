package com.vcheck.sdk.core.util

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.LinearLayout
import com.vcheck.sdk.core.R

class CircleOverlayView : LinearLayout {

    private var bitmap: Bitmap? = null

    private var parentWidth: Int = 4
    private var parentHeight: Int = 4
    private var radius: Int = 2

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

        paint.color = resources.getColor(R.color.vcheck_background_secondary)

        //paint.alpha = 99 // no need alpha setting here

        osCanvas.drawRect(outerRectangle, paint)

        //osCanvas.drawRoundRect(outerRectangle, 10F, 10F, paint) - rounded corners option
    }

    private fun drawTransparentCircle() {

        val osCanvas = Canvas(bitmap!!)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = Color.TRANSPARENT
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)

        val centerX = (parentWidth / 2).toFloat()
        val centerY = (parentHeight / 2).toFloat()

        val radius = radius.toFloat() //?

        osCanvas.drawCircle(centerX, centerY, radius, paint)
    }

    override fun isInEditMode(): Boolean {
        return true
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        bitmap = null
    }

    fun setCircleHoleSize(parentWidth: Int, parentHeight: Int, radius: Int) {
        this.parentWidth = parentWidth
        this.parentHeight = parentHeight
        this.radius = radius
        //createWindowFrame()
        drawTransparentCircle()
    }
}

//        val left: Int = (centerX - holeWidth / 2).toInt()
//        val top: Int = (centerY - holeHeight / 2).toInt()
//        val right: Int = (centerX + holeWidth / 2).toInt()
//        val bottom: Int = (centerY + holeHeight / 2).toInt()