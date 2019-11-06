package com.wagerr.legacywallet.utils

import android.util.Log
import java.io.IOException
import java.net.Socket

const val TIME_OUT = -1L

fun getPing(host: String, port: Int): Long {
    try {
        var duration = TIME_OUT
        val startTime = System.nanoTime()
        try {
            val socket = Socket(host, port)
            val endTime = System.nanoTime()
            duration = (endTime - startTime) / 1000000
            socket.close()
            Log.v("PING", "ping $host time $duration")
        } catch (e: IOException) {
            Log.v("PING", "ping " + host + e.toString())
        }
        return duration
    } catch (e: Exception) {
        Log.v("PING", "ping " + host + e.toString())
    }
    return TIME_OUT
}