package com.nullptr.monever

class LogParser {
    fun parseLog(logText: String): Set<String> {
        return logText.split(" ").filter { it.startsWith("#") }.map { it.substring(1, it.length) }.toSet()
    }
}