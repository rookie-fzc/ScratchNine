package com.fzc.nine.scratch

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.fzc.nine.scratch.data.ItemInfo
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

class ScratchNineView : View {

    private val mTextPaint: Paint = Paint()
    private val mPathPaint: Paint = Paint()
    private val mMaskColorPaint: Paint = Paint()
    private val mMaskBitmapPaint: Paint = Paint()
    private val mBgPaint: Paint = Paint()
    private val mDividerPaint: Paint = Paint()
    private val mBorderPaint: Paint = Paint()
    private val mPath = Path()
    private lateinit var mBitmap: Bitmap
    private lateinit var mCanvas: Canvas
    private var data: ItemInfo? = null
    private var mBitmapCache: HashMap<Int, Bitmap?>? = null
    private var maxRange = 80F

    private lateinit var onComplete: () -> Unit
    private var mCompleted: AtomicBoolean = AtomicBoolean(false)

    private var lastX = 0F
    private var lastY = 0F
    private var mBackgroundColor = 0
    private var mTextSize = 0F
    private var mTextColor = 0
    private var maskColor = 0
    private var maskResource = 0
    private var mRowAndColumn = 0
    private var mItemSize = 0f
    private var mDividerWidth = 0
    private var mDividerColor = 0
    private var radius = 0F
    private var mBorderColor = 0
    private var mBorderWidth = 0

    companion object {
        const val TAG = "ScratchNineView"
        const val DEFAULT_TEXT_SIZE = 48F
        const val DEFAULT_ROW_COLUMN = 3
        const val DEFAULT_TEXT_COLOR = Color.BLACK
        const val DEFAULT_MASK_COLOR = Color.GRAY
        const val DEFAULT_DIVIDER_WIDTH = 2
        const val DEFAULT_DIVIDER_COLOR = Color.YELLOW
        const val DEFAULT_BORDER_WIDTH = 6
        const val DEFAULT_BORDER_COLOR = Color.YELLOW
        const val DEFAULT_RADIUS = 0F

        const val ADJUST_RANGE = 10
    }

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attributes: AttributeSet?) : super(context, attributes) {
        val typeArray = context?.resources?.obtainAttributes(attributes, R.styleable.ScratchView)
        if (typeArray != null) {
            mBackgroundColor =
                typeArray.getColor(R.styleable.ScratchView_scratch_backgroundColor, 0)
            mTextSize =
                typeArray.getDimension(
                    R.styleable.ScratchView_scratch_textSize,
                    DEFAULT_TEXT_SIZE
                )
            mTextColor =
                typeArray.getColor(R.styleable.ScratchView_scratch_textColor, DEFAULT_TEXT_COLOR)
            maskColor =
                typeArray.getColor(R.styleable.ScratchView_scratch_maskColor, DEFAULT_MASK_COLOR)
            mRowAndColumn =
                typeArray.getInt(R.styleable.ScratchView_scratch_rowAndColumn, DEFAULT_ROW_COLUMN)
            mDividerWidth =
                typeArray.getDimensionPixelSize(
                    R.styleable.ScratchView_scratch_dividerWidth,
                    DEFAULT_DIVIDER_WIDTH
                )
            mDividerColor =
                typeArray.getColor(
                    R.styleable.ScratchView_scratch_dividerColor,
                    DEFAULT_DIVIDER_COLOR
                )
            mBorderWidth =
                typeArray.getDimensionPixelSize(
                    R.styleable.ScratchView_scratch_borderWidth,
                    DEFAULT_BORDER_WIDTH
                )
            mBorderColor =
                typeArray.getColor(
                    R.styleable.ScratchView_scratch_borderColor,
                    DEFAULT_BORDER_COLOR
                )
            radius = typeArray.getDimension(R.styleable.ScratchView_scratch_radius, DEFAULT_RADIUS)

            typeArray.recycle()
        }
    }

    private fun initBitmapCache() {
        mBitmapCache = HashMap()
        data?.images?.filter { it != -1 }?.map {
            cacheBitmap(it)
        }
    }

    private fun cacheBitmap(drawableId: Int) {
        val newBitmap = BitmapFactory.decodeResource(context.resources, drawableId)
        mBitmapCache?.set(drawableId, newBitmap)
        invalidate()
    }

    private fun initPaint() {
        mTextPaint.isAntiAlias = true
        mTextPaint.style = Paint.Style.FILL
        mTextPaint.color = mTextColor
        mTextPaint.textSize = mTextSize
        mTextPaint.textAlign = Paint.Align.CENTER
        mTextPaint.isDither = true

        mMaskColorPaint.isAntiAlias = true
        mMaskColorPaint.style = Paint.Style.FILL
        mMaskColorPaint.color = maskColor
        mMaskColorPaint.isDither = true

        mPathPaint.isAntiAlias = true
        mPathPaint.style = Paint.Style.STROKE
        mPathPaint.strokeWidth = (width shr 2).toFloat()
        mPathPaint.strokeJoin = Paint.Join.ROUND
        mPathPaint.strokeCap = Paint.Cap.ROUND

        mBgPaint.isAntiAlias = true
        mBgPaint.color = mBackgroundColor
        mBgPaint.style = Paint.Style.FILL

        mMaskBitmapPaint.isAntiAlias = true
        mMaskBitmapPaint.isDither = true
        mMaskBitmapPaint.flags = 3

        mDividerPaint.isAntiAlias = true
        mDividerPaint.color = mDividerColor
        mDividerPaint.style = Paint.Style.STROKE
        mDividerPaint.strokeWidth = mDividerWidth.toFloat()

        mBorderPaint.isAntiAlias = true
        mBorderPaint.color = mBorderColor
        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.strokeWidth = mBorderWidth.toFloat()
        mMaskBitmapPaint.isDither = true
        mMaskBitmapPaint.flags = 3
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mItemSize = (width / mRowAndColumn).toFloat()
        initBitmapCache()
        initPaint()
        createMaskBitmap()
        mPathPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)

    }

    private fun createMaskBitmap(): Bitmap {
        val createBitmap =
            Bitmap.createBitmap(
                width - paddingStart - paddingEnd,
                height - paddingTop - paddingBottom,
                Bitmap.Config.ARGB_8888
            )
        mBitmap = createBitmap
        mCanvas = Canvas(createBitmap)
        if (maskResource <= 0) {
            drawMaskColorLayer()
        } else {
            val copyBitmap = BitmapFactory.decodeResource(context.resources, maskResource)
                .copy(Bitmap.Config.ARGB_8888, true)
            val width = copyBitmap.width
            val height = copyBitmap.height
            val matrix = Matrix()
            matrix.setScale(
                createBitmap.width.toFloat() / width.toFloat(),
                createBitmap.height.toFloat() / height.toFloat()
            )
            mCanvas.drawBitmap(copyBitmap, matrix, mMaskBitmapPaint)
            copyBitmap.recycle()
        }
        return createBitmap
    }

    private fun drawMaskColorLayer() {
        mCanvas.drawRoundRect(
            0F,
            0F,
            width.toFloat(),
            height.toFloat(),
            radius,
            radius,
            mMaskColorPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.apply {
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = x
                    lastY = y
                    mPath.moveTo(lastX, lastY)
                }
                MotionEvent.ACTION_MOVE -> {
                    var dx = abs(x - lastX)
                    var dy = abs(y - lastY)
                    takeIf {
                        dx > ADJUST_RANGE || dy > ADJUST_RANGE
                    }.apply {
                        mPath.lineTo(x, y)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (!mCompleted.get()) {
                        computeWipeArea()
                    }
                }
            }
            invalidate()
        }
        return true
    }

    fun setMaxRange(maxRange: Float) {
        this.maxRange = maxRange
    }

    private fun computeWipeArea() {
        val w = width
        val h = height
        val totalArea = w * h
        var wipeArea = 0F
        val bitmap = mBitmap
        val pixels = IntArray(totalArea)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        for (i in 0..w) {
            for (j in 0..h) {
                val index = i + j * w
                if (index < totalArea && pixels[index] == 0) {
                    wipeArea++
                }
            }
        }
        if (wipeArea > 0 && totalArea > 0) {
            val percent = wipeArea * 100 / totalArea
            if (percent > maxRange) {
                mCompleted.compareAndSet(false, true)
                onComplete.invoke()
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        Log.e(TAG, "onDraw: ---------------")
        // 背景层
        drawBackground(canvas)
        // 内容层
        drawContent(canvas)
        // 遮罩层
        canvas?.drawBitmap(mBitmap, 0f, 0f, mMaskColorPaint)
        // 涂鸦
        mCanvas.drawPath(mPath, mPathPaint)
        // 画分割线
        drawDivider(canvas)
        // 画边框
        drawBorderRect(canvas)
    }

    private fun drawBorderRect(canvas: Canvas?) {
        val halfWidth = mBorderWidth / 2f
        canvas?.drawRoundRect(
            halfWidth,
            halfWidth,
            width.toFloat() - halfWidth,
            height.toFloat() - halfWidth,
            radius,
            radius,
            mBorderPaint
        )
    }

    private fun drawBackground(canvas: Canvas?) {
        canvas?.drawRoundRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            radius,
            radius,
            mBgPaint
        )
    }

    private fun drawContent(canvas: Canvas?) {
        val size = mItemSize
        var row = 0
        val realDataList = data?.images
        if (realDataList != null) {
            for (i in realDataList.indices) {
                val column = i % mRowAndColumn
                if (column == 0 && i > 0) {
                    row++
                }
                val left = column * size
                val top = row * size
                drawItem(left, top, canvas, realDataList[i])
            }
        }
    }

    private fun drawDivider(canvas: Canvas?) {
        if (data == null) {
            return
        }
        val size = mItemSize
        var row = 0
        val dataSize = data?.images?.size ?: 0 + 1
        for (i in 0 until dataSize) {
            val column = i % mRowAndColumn
            if (column == 0 && i > 0) {
                row++
            }
            val left = column * size
            val right = left + size
            val top = row * size
            val bottom = top + size
            // 画线
            if (column != 0) {
                canvas?.drawLine(left, top, left, bottom, mDividerPaint)
            }
            if (row != 0) {
                canvas?.drawLine(left, top, right, top, mDividerPaint)
            }
        }
    }

    private fun drawItem(left: Float, top: Float, canvas: Canvas?, imgId: Int) {
        val newBitmap: Bitmap? = mBitmapCache?.get(imgId)
        val nLeft = left + mItemSize / 2 - (newBitmap?.width?.div(2) ?: 0)
        val nTop = top + mItemSize / 2 - (newBitmap?.height?.div(2) ?: 0)
        if (newBitmap != null) {
            val bitmap: Bitmap = newBitmap
            canvas?.drawBitmap(bitmap, nLeft, nTop, null)
        }
    }

    fun setMaskLayerDrawable(resId: Int) {
        this.maskResource = resId
        if (width > 0 && height > 0) {
            createMaskBitmap()
        }
    }

    fun setData(data: ItemInfo) {
        this.data = data
    }

    fun setOnCompleteListener(onComplete: () -> Unit) {
        this.onComplete = onComplete
    }

    fun setBgColor(color: Int) {
        this.mBackgroundColor = color
    }

    fun setTextColor(color: Int) {
        this.mTextColor = color
    }

    fun setTextSize(size: Float) {
        this.mTextSize = size
    }

    fun setMaskColor(color: Int) {
        this.maskColor = color
    }

    fun setSpan(span: Int) {
        this.mRowAndColumn = span
    }

    fun setRoundRadius(radius: Float) {
        this.radius = radius
    }

    fun setDividerWidth(width: Int) {
        this.mDividerWidth = width
    }

    fun setDividerColor(color: Int) {
        this.mDividerColor = color
    }

    fun reset() {
        createMaskBitmap()
        mPath.reset()
        mCompleted.set(false)
        invalidate()
    }

    fun clear() {
        mBitmapCache?.values?.map {
            it?.recycle()
        }
    }
}