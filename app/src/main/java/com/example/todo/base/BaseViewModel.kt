package com.example.todo.base

import android.content.Context
import androidx.lifecycle.ViewModel

open class BaseViewModel:ViewModel() {
    lateinit var mContext: Context
    //获得view中context
    fun setContext(context: Context){
        this.mContext=context
    }
}