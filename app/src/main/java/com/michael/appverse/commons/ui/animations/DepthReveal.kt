package com.michael.edconnect.utils.animations

import android.view.View
import androidx.viewpager2.widget.ViewPager2


object DepthReveal : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        if (position < -1) {    // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.alpha = 0F
        } else if (position <= 0) {    // [-1,0]
            page.alpha = 1F
            page.translationX = 0F
            page.scaleX = 1F
            page.scaleY = 1F
        } else if (position <= 1) {    // (0,1]
            page.translationX = -position * page.width
            page.alpha = 1 - Math.abs(position)
            page.scaleX = 1 - Math.abs(position)
            page.scaleY = 1 - Math.abs(position)
        } else {    // (1,+Infinity]
            // This page is way off-screen to the right.
            page.alpha = 0F
        }
    }
}