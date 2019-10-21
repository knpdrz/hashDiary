package com.nullptr.monever

import java.io.Serializable
import java.util.*

data class Log(val text: String, val happyRating: Int, var creationDate: Date? = Calendar.getInstance().time) : Serializable