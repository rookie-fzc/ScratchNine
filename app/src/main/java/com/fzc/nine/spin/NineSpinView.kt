package com.fzc.nine.spin

import com.fzc.nine.utils.ImageUtils
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.fzc.nine.scratch.R
import java.lang.IllegalArgumentException


data class ItemInfo(val reward: Int)

class NineSpinView : View {

    private val mTextPaint: Paint = Paint()
    private val mBgPaint: Paint = Paint()
    private val mHitPaint: Paint = Paint()

    private var mBackgroundColor = 0
    private var mTextSize = 0F
    private var mTextColor = 0
    private var mRowAndColumn = 0
    private var mRadius = 0F
    private var mCenterIndex = 0
    private var mHitColor = 0

    private var mItemImg: Drawable? = null
    private var mCenterImg: Drawable? = null

    private var mItemSize = 0f
    private var mBitmapCache: HashMap<Int, Bitmap?>? = null
    private var centerRectF: RectF? = null

    private var mCompleteListener: (() -> Unit)? = null
    private var mCenterListener: (() -> Unit)? = null

    private val orderArray = intArrayOf(0, 1, 2, 5, 8, 7, 6, 3)

    private var hitIndex = -1
    private var repeatCount = 0
    private var targetIndex = -1
    private var isSpinning = false
    private var isFinish = false

    private var dataList:ArrayList<ItemInfo>? = null

    companion object {
        const val TAG = "NineSpinView"
        const val MAX_REPEAT_COUNT = 3
        const val DEFAULT_TEXT_SIZE = 31F
        const val DEFAULT_RADIUS = 0F
        const val DEFAULT_ROW_COLUMN = 3
        const val DEFAULT_TEXT_COLOR = Color.BLACK
        const val DEFAULT_HIT_COLOR = Color.YELLOW
    }

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attributes: AttributeSet?) : super(context, attributes) {
        val typeArray = context?.resources?.obtainAttributes(attributes, R.styleable.NineSpinView)
        if (typeArray != null) {
            mBackgroundColor =
                typeArray.getColor(R.styleable.NineSpinView_spin_backgroundColor, 0)
            mTextSize =
                typeArray.getDimension(
                    R.styleable.NineSpinView_spin_textSize,
                    DEFAULT_TEXT_SIZE
                )
            mTextColor =
                typeArray.getColor(
                    R.styleable.NineSpinView_spin_textColor,
                    DEFAULT_TEXT_COLOR
                )
            mHitColor =
                typeArray.getColor(
                    R.styleable.NineSpinView_spin_hit_color,
                    DEFAULT_HIT_COLOR
                )
            mRowAndColumn =
                typeArray.getInt(
                    R.styleable.NineSpinView_spin_rowAndColumn,
                    DEFAULT_ROW_COLUMN
                )
            mRadius = typeArray.getDimension(
                R.styleable.NineSpinView_spin_radius,
                DEFAULT_RADIUS
            )
            mItemImg = typeArray.getDrawable(
                R.styleable.NineSpinView_spin_item_img
            )
            mCenterImg = typeArray.getDrawable(
                R.styleable.NineSpinView_spin_center_img
            )
            typeArray.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mItemSize = (width / mRowAndColumn).toFloat()
        mCenterIndex = mRowAndColumn * mRowAndColumn / 2
        initBitmapCache()
        initPaint()
    }

    private fun initBitmapCache() {
        mBitmapCache = HashMap()
        val centerImg = BitmapFactory.decodeResource(resources, R.drawable.spin_nine_center)
        val itemImg = ImageUtils.drawable2Bitmap(mItemImg!!)
        val width = itemImg.width
        val height = itemImg.height
        val matrix = Matrix()
        matrix.setScale(
            mItemSize * 0.98f / width.toFloat(),
            mItemSize * 0.98f / height.toFloat()
        )
        val scaleItemImg = Bitmap.createBitmap(itemImg, 0, 0, width, height, matrix, true)
        val scaleCenterImg =
            Bitmap.createBitmap(centerImg, 0, 0, centerImg.width, centerImg.height, matrix, true)
        (0 until mRowAndColumn * mRowAndColumn).forEach { index ->
            val newBitmap = if (index == mCenterIndex) {
                scaleCenterImg
            } else {
                scaleItemImg
            }
            mBitmapCache?.set(index, newBitmap)
        }
    }

    private fun initPaint() {
        mTextPaint.isAntiAlias = true
        mTextPaint.style = Paint.Style.FILL
        mTextPaint.color = mTextColor
        mTextPaint.textSize = mTextSize
        mTextPaint.textAlign = Paint.Align.CENTER
        mTextPaint.isDither = true

        mBgPaint.isAntiAlias = true
        mBgPaint.color = mBackgroundColor
        mBgPaint.style = Paint.Style.FILL

        mHitPaint.isAntiAlias = true
        mHitPaint.color = mHitColor
        mHitPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // 背景层
        drawBackground(canvas)
        // 内容层
        drawContent(canvas)
    }

    private fun drawBackground(canvas: Canvas?) {
        canvas?.drawRoundRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            mRadius,
            mRadius,
            mBgPaint
        )
    }

    private fun drawContent(canvas: Canvas?) {
        val size = mItemSize
        var row = 0
        (0 until mRowAndColumn * mRowAndColumn).forEach { i ->
            val column = i % mRowAndColumn
            if (column == 0 && i > 0) {
                row++
            }
            val left = column * size + 3
            val top = row * size + 3
            drawItem(left, top, canvas, i, i)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            val touchX = event.x
            val touchY = event.y
            centerRectF?.let { centerRectF ->
                if (touchX > centerRectF.left
                    && touchX < centerRectF.right
                    && touchY > centerRectF.top &&
                    touchY < centerRectF.bottom
                ) {
                    if (isSpinning) {
                        return@let
                    }
                    isSpinning = true
                    reset()
                    this.mCenterListener?.invoke()
                }
            }
        }
        return true
    }

    private fun drawItem(left: Float, top: Float, canvas: Canvas?, imgId: Int, index: Int) {
        val newBitmap: Bitmap? = mBitmapCache?.get(imgId)
        if (index == mCenterIndex) {
            val bitmap: Bitmap = newBitmap!!
            if (centerRectF == null) {
                centerRectF = RectF(left, top, left + mItemSize, top + mItemSize)
            }
            val imgLeft = left + mItemSize / 2 - (newBitmap.width.div(2))
            val imgTop = top + mItemSize / 2 - (newBitmap.height.div(2))
            canvas?.drawBitmap(bitmap, imgLeft, imgTop, null)
        } else {
            val bitmap: Bitmap = newBitmap!!
            canvas?.drawBitmap(bitmap, left, top, null)
            if (index == hitIndex) {
                canvas?.drawRoundRect(
                    left,
                    top,
                    left + mItemSize,
                    top + mItemSize,
                    mRadius,
                    mRadius,
                    mHitPaint
                )
            }
            val nLeft = left + mItemSize / 2
            val nTop = top + mItemSize * 1.2f / 2
            if (dataList != null && dataList!!.size <= mRowAndColumn*mRowAndColumn) {
                val reward = dataList!![index].reward
                canvas?.drawText("x $reward", nLeft, nTop, mTextPaint)
            } else {
                canvas?.drawText("x $index", nLeft, nTop, mTextPaint)
            }
        }
    }

    fun startSpinWithTargetReward(reward: Int) {
        dataList?.forEachIndexed{index, itemInfo ->
            if (reward == itemInfo.reward) {
                this.targetIndex = index
                return@forEachIndexed
            }
        }
        spinNineAnim()
    }

    private fun spinNineAnim() {
        orderArray.forEachIndexed { index, i ->
            handler.postDelayed({
                if (isFinish) {
                    return@postDelayed
                }
                hitIndex = i
                if (targetIndex == i && repeatCount == MAX_REPEAT_COUNT) {
                    isSpinning = false
                    isFinish = true
                    this.mCompleteListener?.invoke()
                    return@postDelayed
                }
                invalidate()
                if (i == orderArray.last() && repeatCount++ < MAX_REPEAT_COUNT) {
                    spinNineAnim()
                }
            }, ((index + 1) * 100).toLong())
        }
    }

    fun setData(data:ArrayList<ItemInfo>) {
        if (data.size <= 0 && data.size > mRowAndColumn * mRowAndColumn) throw IllegalArgumentException(
            "data not use"
        )
        this.dataList = data
    }

    fun setOnCenterClickListener(clickListener: () -> Unit) {
        this.mCenterListener = clickListener
    }

    fun setCompleteListener(complete: () -> Unit) {
        this.mCompleteListener = complete
    }

    private fun reset() {
        hitIndex = -1
        targetIndex = -1
        repeatCount = 0
        isFinish = false
        invalidate()
    }
}