package com.nullptr.monever.log

import java.io.Serializable
import java.util.*

data class Log(val text: String, val happyRating: Int, var creationDate: Date? = Calendar.getInstance().time,
               val tags: Set<String> = mutableSetOf()) : Serializable