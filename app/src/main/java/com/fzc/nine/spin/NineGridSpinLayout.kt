package com.fzc.nine.spin

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fzc.nine.GridItemInfo
import com.fzc.nine.scratch.R
import java.lang.IllegalArgumentException

class NineGridSpinLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        const val MAX_REPEAT_COUNT = 3
        const val MIDDLE = 4
    }

    private var mGridSpinView: View? = null
    private lateinit var gridAdapter: GridAdapter
    private var gridDataList: MutableList<GridItemInfo> = mutableListOf()
    private val orderArray = intArrayOf(0, 1, 2, 5, 8, 7, 6, 3)
    private var mCompleteListener: (() -> Unit)? = null
    private var centerListener: (() -> Unit)? = null

    private var repeatCount = 0
    private var targetIndex = 0
    private var finishSpin = false
    private var isSpinning = false

    override fun onFinishInflate() {
        super.onFinishInflate()
        setSpinGridView()
    }

    private fun setSpinGridView() {
        mGridSpinView = LayoutInflater.from(context).inflate(R.layout.grid_spin_layout, null)
        addView(mGridSpinView)
        initSpinRv()
    }

    private fun initSpinRv() {
        val spinRv = findViewById<RecyclerView>(R.id.spin_rv)
        val layoutManager = GridLayoutManager(spinRv.context, 3)
        spinRv.layoutManager = layoutManager
        gridAdapter = GridAdapter()
        spinRv.adapter = gridAdapter
        gridAdapter.setOnItemClickListener {
            if (isSpinning) {
                return@setOnItemClickListener
            }
            resetSpin()
            gridAdapter.notifyDataSetChanged()
            centerListener?.invoke()
        }
    }

    inner class GridAdapter :
        RecyclerView.Adapter<GridAdapter.GridVM>() {

        private lateinit var dataList: MutableList<GridItemInfo>

        fun setData(data: MutableList<GridItemInfo>) {
            this.dataList = data
        }

        private var itemClickListener: (() -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridVM {
            val rootView =
                LayoutInflater.from(parent.context).inflate(R.layout.grid_item_layout, null)
            return GridVM(rootView)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: GridVM, position: Int) {
            holder.ivMask.visibility = View.GONE
            holder.tvContent.text = "x ${dataList[position].reward}"
            if (position == MIDDLE) {
                holder.tvContent.visibility = View.GONE
                holder.itemIvBg.visibility = View.GONE
                holder.centerBg.visibility = View.VISIBLE
                holder.centerBg.setImageResource(dataList[position].imgResId)
            } else {
                holder.tvContent.visibility = View.VISIBLE
                holder.centerBg.visibility = View.GONE
                holder.itemIvBg.visibility = View.VISIBLE
                holder.itemIvBg.setImageResource(dataList[position].imgResId)
            }
            holder.centerBg.setOnClickListener {
                itemClickListener?.invoke()
            }
            if (dataList[position].showYellow) {
                holder.ivMask.visibility = View.VISIBLE
            }
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        fun setOnItemClickListener(listener: () -> Unit) {
            this.itemClickListener = listener
        }

        inner class GridVM(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvContent: TextView = itemView.findViewById(R.id.tv_item_num)
            val itemIvBg: ImageView = itemView.findViewById(R.id.iv_item_bg)
            val centerBg: ImageView = itemView.findViewById(R.id.iv_center_bg)
            val ivMask: ImageView = itemView.findViewById(R.id.iv_mask)
        }
    }

    private fun resetSpin() {
        isSpinning = false
        finishSpin = false
        repeatCount = 0
        gridDataList.forEach { it.showYellow = false }
    }

    private fun spinNineAnim() {
        orderArray.forEachIndexed { index, i ->
            handler.postDelayed({
                if (finishSpin) {
                    return@postDelayed
                }
                if (targetIndex == i && repeatCount == MAX_REPEAT_COUNT) {
                    gridDataList[targetIndex].showYellow = true
                    gridAdapter.notifyItemChanged(targetIndex)
                    finishSpin = true
                    isSpinning = false
                    mCompleteListener?.invoke()
                    return@postDelayed
                }
                isSpinning = true
                gridDataList[i].showYellow = true
                gridAdapter.notifyItemChanged(i)
                gridDataList[i].showYellow = false
                gridAdapter.notifyItemChanged(i)
                if (i == orderArray.last() && repeatCount++ < MAX_REPEAT_COUNT) {
                    spinNineAnim()
                }
            }, ((index + 1) * 100).toLong())
        }
    }

    fun setData(dataList: ArrayList<Int>) {
        gridDataList.clear()
        dataList.forEachIndexed { index, value ->
            val imgId =
                if (index == 4) R.drawable.spin_nine_center else R.drawable.spin_nine_item_bg
            val info = GridItemInfo(imgId, value)
            gridDataList.add(info)
        }
        gridAdapter.setData(gridDataList)
        gridAdapter.notifyDataSetChanged()
    }

    fun setOnCenterClickListener(clickListener: () -> Unit) {
        this.centerListener = clickListener
    }

    fun setCompleteListener(complete: () -> Unit) {
        this.mCompleteListener = complete
    }

    fun startSpinWithTarget(realWard: Int) {
        if (gridDataList.size <= 0) {
            throw IllegalArgumentException("spin data must be not empty")
        }

        gridDataList.forEachIndexed { index, gridItemInfo ->
            if (gridItemInfo.reward == realWard) {
                targetIndex = index
                return@forEachIndexed
            }
        }
        spinNineAnim()
    }
}