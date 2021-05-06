package com.example.todo.user.viewmodel


import android.content.Context
import android.text.Editable
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.todo.user.activity.LoginActivity
import com.example.todo.user.model.UserData
import com.example.todo.util.ToastUtil


class LoginViewModel : ViewModel() {
    private lateinit var context: Context
    private lateinit var userData: UserData

    //获得view中context
    fun setContext(context: Context) {
        this.context = context
        userData = UserData(context)
    }

    fun checkLoginMessage(account: String, password: String):Boolean {
        //刷新数据库中登录状态
        userData.addLoginState(password == userData.getUserPassword(account))
        return if (password == userData.getUserPassword(account)) {
            ToastUtil.showMsg(context, "登录成功")
            true
        } else {
            ToastUtil.showMsg(context, "账号或密码错误")
            false
        }
    }

}