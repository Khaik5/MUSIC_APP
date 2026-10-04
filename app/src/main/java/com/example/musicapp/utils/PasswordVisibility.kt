package com.example.musicapp.utils

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageButton
import com.example.musicapp.R

fun ImageButton.bindPasswordVisibility(passwordField: EditText) {
    setOnClickListener {
        val selection = passwordField.selectionStart
        val passwordHidden = passwordField.transformationMethod is PasswordTransformationMethod
        passwordField.transformationMethod = if (passwordHidden) {
            HideReturnsTransformationMethod.getInstance()
        } else {
            PasswordTransformationMethod.getInstance()
        }
        passwordField.setSelection(selection.coerceAtLeast(0))
        contentDescription = context.getString(
            if (passwordHidden) R.string.hide_password else R.string.show_password
        )
    }
}
