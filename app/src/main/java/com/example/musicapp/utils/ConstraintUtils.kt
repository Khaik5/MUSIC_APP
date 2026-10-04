package com.example.musicapp.utils

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams

fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

fun View.setConstraintBottomMargin(dp: Int) {
    updateLayoutParams<ConstraintLayout.LayoutParams> {
        bottomMargin = context.dp(dp)
    }
}
