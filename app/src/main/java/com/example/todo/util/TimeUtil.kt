package com.example.todo.util

import java.text.SimpleDateFormat
import java.util.*

object  TimeUtil {
    fun getTime(date: Date): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm")
        return format.format(date)
    }

    fun getMaxTime(time1: String, time2: String): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val date1 = format.parse(time1)
        val data2 = format.parse(time2)
        return if (date1.time >data2.time) {
            time1
        } else {
            time2
        }
    }
    fun getTimeDifference(time1: String, time2: String):Long{
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val date1 = format.parse(time1)
        val data2 = format.parse(time2)
        return Math.abs(date1.time-data2.time)
    }
    fun getSystemTime():String{
        val systemTime: Calendar = Calendar.getInstance()
        systemTime.set(
            systemTime.get(Calendar.YEAR),
            systemTime.get(Calendar.MONTH),
            systemTime.get(Calendar.DAY_OF_MONTH),
            systemTime.get(Calendar.HOUR_OF_DAY),
            systemTime.get(Calendar.MINUTE)
        )
        return getTime(systemTime.time)
    }
}