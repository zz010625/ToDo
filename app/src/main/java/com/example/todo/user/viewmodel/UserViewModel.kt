package com.example.todo.user.viewmodel

import androidx.lifecycle.MutableLiveData
import com.example.todo.base.BaseViewModel
import com.example.todo.user.model.UserData

class UserViewModel: BaseViewModel() {
     var userProfileUrl=MutableLiveData<String>()
     var userName=MutableLiveData<String>()
    private val userData by lazy {
        UserData(mContext)
    }
  fun checkIsLogin():Boolean{
     return userData.checkLoginState()
 }

    fun initData() {
        if (checkIsLogin()){
           val userMessageList=userData.getLoginUserMessage()
            userName.postValue(userMessageList[0])
            userProfileUrl.postValue(userMessageList[1])

        }else{
            userName.postValue("点击头像立刻登录")
            userProfileUrl.postValue("https://profile.csdnimg.cn/1/F/9/1_m0_52051799")
        }
    }
}