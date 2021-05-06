package com.example.todo.user.viewmodel


import android.content.ContentValues
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.todo.user.model.UserData
import com.example.todo.util.ToastUtil

class RegisterViewModel: ViewModel() {
    private lateinit var context: Context
    private lateinit var userData: UserData
    //获得view中context
    fun setContext(context: Context){
        this.context=context
        userData= UserData(context)
    }
    //设置用户信息 并调用model曾将数据添加到数据库
    fun setUserMessage(account:String, password:String) {
        val values = ContentValues()
        values.put("account", account)
        values.put("password", password)
        values.put("userName", "未命名")
        values.put("userProfileUrl", "https://profile.csdnimg.cn/1/F/9/1_m0_52051799")
        userData.addUser(values)
        ToastUtil.showMsg(context,"注册成功")
    }

}