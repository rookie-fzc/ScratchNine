package com.fzc.nine.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix

import android.graphics.PixelFormat

import android.graphics.drawable.BitmapDrawable

import android.graphics.drawable.Drawable


/**
 * Auth        :C
 * Time        :2020/7/15
 * Description :
 */
class ImageUtils {
    companion object {

        fun drawable2Bitmap(drawable: Drawable): Bitmap {
            if (drawable is BitmapDrawable) {
                if (drawable.bitmap != null) {
                    return drawable.bitmap
                }
            }
            val bitmap: Bitmap =
                if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                    Bitmap.createBitmap(
                        1, 1,
                        if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
                    )
                } else {
                    Bitmap.createBitmap(
                        drawable.intrinsicWidth,
                        drawable.intrinsicHeight,
                        if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
                    )
                }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            return bitmap
        }
    }
}