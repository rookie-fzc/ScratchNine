package com.fzc.nine.scratch

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.fzc.nine.scratch.data.ItemInfo

class MainActivity : AppCompatActivity() {

    private lateinit var scratchNineView: ScratchNineView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scratch_new_layout)
        initScratch()
//        testCropPixes()
    }

    // 截取指定位置的图像
    private fun testCropPixes() {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.scratch_reward_1)
        val oriWidth = bitmap.width
        val oriHeight = bitmap.height
        val pixes = IntArray(oriWidth * oriHeight)
        // getPixels 将二维图片的所有信息读取到一个一维数组中
        bitmap.getPixels(
            pixes,
            0,
            oriWidth,
            oriWidth / 4,
            oriHeight / 6,
            oriWidth / 2,
            oriHeight * 3 / 4
        )
        val newBitmap = Bitmap.createBitmap(
            pixes,
            0,
            oriWidth,
            oriWidth / 2,
            oriHeight * 3 / 4,
            Bitmap.Config.ARGB_8888
        )
        findViewById<ImageView>(R.id.top_image).setImageBitmap(newBitmap)
    }

    // 拼接图片
    private fun testMergePixes(n: Int = 2) {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.scratch_reward_1)
        val oriWidth = bitmap.width
        val oriHeight = bitmap.height
        val nWidth = n * oriWidth
        val pixes = IntArray(nWidth * oriHeight)
        for (i in 0 until n) {
            bitmap.getPixels(pixes, i * oriWidth, nWidth, oriWidth, oriHeight, oriWidth, oriHeight)
        }
        val newBitmap =
            Bitmap.createBitmap(pixes, 0, nWidth, nWidth, oriHeight, Bitmap.Config.ARGB_8888)
        findViewById<ImageView>(R.id.top_image).setImageBitmap(newBitmap)
    }

    private fun initScratch() {
        scratchNineView = findViewById(R.id.scratch_view)
        scratchNineView.visibility = View.VISIBLE
        scratchNineView.setOnCompleteListener {

            initAnimateLayout()

        }
        scratchNineView.setMaskLayerDrawable(R.drawable.scratch_mask_img)
        scratchNineView.setBgColor(Color.WHITE)
        scratchNineView.setData(initScratchData())
        scratchNineView.setMaxRange(90F)
        scratchNineView.setDividerColor(Color.YELLOW)
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
        disCard.animate().translationX(1300f).translationY(100F).rotation(-25F).setDuration(700)
            .withEndAction {

                disCard.translationX = 0f
                disCard.translationY = 0f
                disCard.rotation = 0f
                disCard.visibility = View.GONE

                scratchNineView.reset()
            }
    }

    private fun initScratchData(): ItemInfo {
        val imageList = mutableListOf<Int>()
        for (i in 0 until 9) {
            val drawableId = resources.getIdentifier("scratch_reward_$i", "drawable", packageName)
            imageList.add(i, drawableId)
        }
        return ItemInfo(imageList)
    }

    override fun onDestroy() {
        super.onDestroy()
        scratchNineView.clear()
    }
}