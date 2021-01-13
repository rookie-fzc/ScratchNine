package com.fzc.nine.scratch

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class ScratchGuideView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var startPoint: PointF = PointF()
    private var endPointF = PointF()
    private var controlPoint1 = PointF()
    private var controlPoint2 = PointF()

    private var path = Path()
    private var paint = Paint()
    private var pathRecord: FloatArray = FloatArray(2)
    private var circlePaint = Paint()
    private lateinit var pathMeasure: PathMeasure
    private lateinit var fingerBitmap: Bitmap

    init {
        initPaint()
    }

    private fun initPath() {
        path.reset()
        path.moveTo(startPoint.x, startPoint.y)

        path.cubicTo(
            controlPoint1.x,
            controlPoint1.y,
            controlPoint2.x,
            controlPoint2.y,
            endPointF.x,
            endPointF.y
        )
        pathMeasure = PathMeasure(path, false)
    }

    private fun initPaint() {
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.color = Color.WHITE
        paint.strokeWidth = (80).toFloat()
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND

        circlePaint.isAntiAlias = true
        circlePaint.color = Color.YELLOW
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = (40).toFloat()
        circlePaint.strokeJoin = Paint.Join.ROUND
        circlePaint.strokeCap = Paint.Cap.ROUND

        fingerBitmap = BitmapFactory.decodeResource(resources, R.drawable.task_center_finger_icon)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        startPoint.x = 0.167F * w
        startPoint.y = h * 0.333F

        controlPoint1.x = 0.253F * w
        controlPoint1.y = h * 0.75F

        controlPoint2.x = (w * 0.555F)
        controlPoint2.y = (h * 0.167F)

        endPointF.x = (w * 0.833F)
        endPointF.y = (h * 0.666F)

        initPath()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(path, paint)
        canvas?.drawBitmap(fingerBitmap, pathRecord[0], pathRecord[1], null)
    }

    private var pathAnim: ValueAnimator? = null

    fun startGuideAnm() {
        visibility = VISIBLE
        pathAnim = ValueAnimator.ofFloat(1F, 0F)
        pathAnim?.repeatMode = ValueAnimator.RESTART
        pathAnim?.repeatCount = -1
        pathAnim?.duration = 1000
        pathAnim?.addUpdateListener {
            val animatedValue = it.animatedValue as Float
            val position = pathMeasure.length * animatedValue
            pathMeasure.getPosTan(position, pathRecord, null)
            path.reset()
            pathMeasure.getSegment(
                pathMeasure.length * animatedValue,
                pathMeasure.length,
                path,
                true
            )
            postInvalidate()
        }
        pathAnim?.start()
    }

    fun hide() {
        if (visibility == VISIBLE) {
            pathAnim?.cancel()
            pathAnim = null
            visibility = GONE
        }
    }
}
