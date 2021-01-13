package com.fzc.nine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.fzc.nine.scratch.R
import com.fzc.nine.spin.NineGridSpinLayout


data class GridItemInfo(val imgResId: Int, val reward: Int, var showYellow: Boolean = false)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

//    // 截取指定位置的图像
//    private fun testCropPixes() {
//        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.scratch_reward_1)
//        val oriWidth = bitmap.width
//        val oriHeight = bitmap.height
//        val pixes = IntArray(oriWidth * oriHeight)
//        // getPixels 将二维图片的所有信息读取到一个一维数组中
//        bitmap.getPixels(
//            pixes,
//            0,
//            oriWidth,
//            oriWidth / 4,
//            oriHeight / 6,
//            oriWidth / 2,
//            oriHeight * 3 / 4
//        )
//        val newBitmap = Bitmap.createBitmap(
//            pixes,
//            0,
//            oriWidth,
//            oriWidth / 2,
//            oriHeight * 3 / 4,
//            Bitmap.Config.ARGB_8888
//        )
//        findViewById<ImageView>(R.id.top_image).setImageBitmap(newBitmap)
//    }
//
//    // 拼接图片
//    private fun testMergePixes(n: Int = 2) {
//        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.scratch_reward_1)
//        val oriWidth = bitmap.width
//        val oriHeight = bitmap.height
//        val nWidth = n * oriWidth
//        val pixes = IntArray(nWidth * oriHeight)
//        for (i in 0 until n) {
//            bitmap.getPixels(pixes, i * oriWidth, nWidth, oriWidth, oriHeight, oriWidth, oriHeight)
//        }
//        val newBitmap =
//            Bitmap.createBitmap(pixes, 0, nWidth, nWidth, oriHeight, Bitmap.Config.ARGB_8888)
//        findViewById<ImageView>(R.id.top_image).setImageBitmap(newBitmap)
//    }
}