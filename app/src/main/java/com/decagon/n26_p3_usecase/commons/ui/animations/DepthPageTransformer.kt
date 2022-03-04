package com.decagon.edconnect.utils.animations

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs


object DepthPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        val pageWidth: Int = view.width
        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.alpha = 0F
        } else if (position <= 0) { // [-1,0]
            // Use the default slide transition when moving to the left page
            view.alpha = 1F
            view.translationX = 0F
            view.scaleX = 1F
            view.scaleY = 1F
        } else if (position <= 1) { // (0,1]
            // Fade the page out.
            view.alpha = 1 - position

            // Counteract the default slide transition
            view.translationX = pageWidth * -position

            // Scale the page down (between MIN_SCALE and 1)
            val scaleFactor: Float = (MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position)))
            view.scaleX = scaleFactor
            view.scaleY = scaleFactor
        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.alpha = 0F
        }
    }

        private const val MIN_SCALE = 0.75f
}