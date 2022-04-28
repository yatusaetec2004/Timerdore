package com.yts.tymodoro.utils

import android.text.Editable
import android.text.SpannableStringBuilder

//Aquí van las extensiones de datos primitivos y del tipo String

fun String.toEditable():Editable{
    return SpannableStringBuilder(this)
}