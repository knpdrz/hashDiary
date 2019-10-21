package com.nullptr.monever

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LogParserTest{
    @Test
    fun parsingTest(){
        val logParser = LogParser()
        val parsedLog = logParser.parseLog("ala #ma chyba #kota")
        assertThat(parsedLog).isEqualTo(listOf("ma", "kota"))
    }
}