package com.decagon.n26_p3_usecase.features.qrCode

import com.google.gson.Gson

object ClassConverter {

    val gson = Gson()

    fun <T> toJson(value: T): String = gson.toJson(value)

    inline fun <reified T> toClass(value: String, dataClass: T) = gson.fromJson(value, T::class.java)
}