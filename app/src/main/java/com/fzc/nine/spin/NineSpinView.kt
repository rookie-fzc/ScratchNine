package com.fzc.nine.spin

import com.fzc.nine.utils.ImageUtils
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.fzc.nine.scratch.R


data class ItemInfo(val reward: Int)

class NineSpinView : View {

    private val mTextPaint: Paint = Paint()
    private val mBgPaint: Paint = Paint()

    private var mBackgroundColor = 0
    private var mTextSize = 0F
    private var mTextColor = 0
    private var mRowAndColumn = 0
    private var mRadius = 0F
    private var mCenterIndex = 0

    private var mItemImg: Drawable? = null
    private var mCenterImg: Drawable? = null

    private var mItemSize = 0f
    private var mBitmapCache: HashMap<Int, Bitmap?>? = null

    companion object {
        const val TAG = "NineSpinView"
        const val DEFAULT_TEXT_SIZE = 31F
        const val DEFAULT_RADIUS = 0F
        const val DEFAULT_ROW_COLUMN = 3
        const val DEFAULT_TEXT_COLOR = Color.BLACK
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

    private fun drawItem(left: Float, top: Float, canvas: Canvas?, imgId: Int, index: Int) {
        val newBitmap: Bitmap? = mBitmapCache?.get(imgId)
        if (index == mCenterIndex) {
            val bitmap: Bitmap = newBitmap!!
            val imgLeft = left + mItemSize / 2 - (newBitmap.width.div(2))
            val imgTop = top + mItemSize / 2 - (newBitmap.height.div(2))
            canvas?.drawBitmap(bitmap, imgLeft, imgTop, null)
        } else {
            val bitmap: Bitmap = newBitmap!!
            canvas?.drawBitmap(bitmap, left, top, null)
            val nLeft = left + mItemSize / 2
            val nTop = top + mItemSize * 1.2f / 2
            canvas?.drawText("x $index", nLeft, nTop, mTextPaint)
        }
    }
}