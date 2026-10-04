package com.example.musicapp.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun View.applySystemBarsInsets(
    top: Boolean = true,
    bottom: Boolean = true,
    left: Boolean = true,
    right: Boolean = true
) {
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(
            initialLeft + if (left) bars.left else 0,
            initialTop + if (top) bars.top else 0,
            initialRight + if (right) bars.right else 0,
            initialBottom + if (bottom) bars.bottom else 0
        )
        insets
    }
    ViewCompat.requestApplyInsets(this)
}
