package com.fzc.nine.scratch

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import com.fzc.nine.scratch.data.ItemInfo

class ScratchNineFragment : Fragment() {
    private lateinit var scratchNineView: ScratchNineView
    private var guideView: ScratchGuideView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scratch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initScratch()
    }

    private fun initScratch() {
        scratchNineView = findViewById(R.id.scratch_view)
        scratchNineView.visibility = View.VISIBLE
        scratchNineView.setOnStartListener {
            guideView?.hide()
        }
        scratchNineView.setOnCompleteListener {
            showGuide()
            initAnimateLayout()
        }
        scratchNineView.setMaskLayerDrawable(R.drawable.scratch_reward_coffee_cover)
        scratchNineView.setBgColor(Color.WHITE)
        scratchNineView.setData(initScratchData())
        scratchNineView.setMaxRange(90F)
        scratchNineView.setDividerColor(Color.YELLOW)
    }

    private fun showGuide() {
        guideView = findViewById(R.id.guide_view)
        guideView?.startGuideAnm()
    }

    private fun initAnimateLayout() {
        val scratchCard = findViewById<CardView>(R.id.scratch_scratch)
        val scratchBitmap =
            Bitmap.createBitmap(scratchCard.width, scratchCard.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(scratchBitmap)
        findViewById<View>(R.id.scratch_discard_anim_img).background =
            BitmapDrawable(resources, scratchBitmap)
        canvas.drawColor(0)
        scratchCard.draw(canvas)
        val disCard = findViewById<CardView>(R.id.scratch_discard_anim)
        disCard.visibility = View.VISIBLE
        disCard.animate().cancel()
        disCard.animate().translationX(-1330F).translationY(100F).rotation(25F).setDuration(700)
            .withEndAction {

                disCard.translationX = 0f
                disCard.translationY = 0f
                disCard.rotation = 0f
                disCard.visibility = View.GONE
                scratchNineView.setMaskLayerDrawable(R.drawable.cober_1)
                scratchNineView.reset()
            }
    }

    private fun initScratchData(): ItemInfo {
        val imageList = mutableListOf<Int>()
        for (i in 0 until 9) {
            val drawableId = resources.getIdentifier("scratch_reward_$i", "drawable", context?.packageName)
            imageList.add(i, drawableId)
        }
        return ItemInfo(imageList)
    }

    override fun onDestroy() {
        super.onDestroy()
        scratchNineView.clear()
    }

}

fun <T : View> Fragment.findViewById(viewId: Int): T {
    val rootView = view!!
    return rootView.findViewById(viewId)
}