package com.wagerr.legacywallet

import org.junit.Test
import org.wagerrj.core.Coin

import org.junit.Assert.assertEquals

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    @Test
    @Throws(Exception::class)
    fun addition_isCorrect() {
        assertEquals(4, (2 + 2).toLong())
        val s = Coin.valueOf(1).toString()

    }
}