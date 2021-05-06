package com.example.todo.user.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.todo.R
import com.example.todo.user.viewmodel.LoginViewModel
import kotlin.math.log

class LoginActivity : AppCompatActivity() {
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var agreeUserAgreement:CheckBox
    private lateinit var rememberPassword:CheckBox
    private lateinit var userAgreement:TextView
    private lateinit var login:Button
    private lateinit var account: EditText
    private lateinit var password: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()
    }

    private fun init() {
        //创建ViewModel
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        loginViewModel.setContext(this)
        //初始化控件
        agreeUserAgreement = findViewById(R.id.cb_user_agreement)
        rememberPassword = findViewById(R.id.cb_remember_password)
        userAgreement = findViewById(R.id.tv_user_agreement)
        login = findViewById(R.id.btn_login)
        account = findViewById(R.id.et_account)
        password = findViewById(R.id.et_password)

        //监听用户协议勾选框
        agreeUserAgreement.setOnCheckedChangeListener { buttonView, isChecked ->
            login.isEnabled = agreeUserAgreement.isChecked
        }
    }

     //点击用户协议
    fun onClickUserAgreement(view:View){
         val dialog =
             AlertDialog.Builder(this@LoginActivity)
         dialog.setTitle("用户协议")
         dialog.setMessage("红岩移动开发部期中考核")
         dialog.setCancelable(false)
         //点击确认
         dialog.setPositiveButton(
             "确认"
         ) { dialog, which ->
             agreeUserAgreement.isChecked = true
             login.isEnabled = true
         }
         //点击取消
         dialog.setNegativeButton(
             "取消"
         ) { dialog, which -> agreeUserAgreement.isChecked = false }
         dialog.show()
     }
     //点击登录
    fun onClickLogin(view: View){
         //调用viewModel中方法分析输入的账号密码是否正确
        if ( loginViewModel.checkLoginMessage(account.editableText.toString(),password.editableText.toString())){
            finish()
        }
     }
     //点击事件 跳转至注册界面
     fun jumpToRegister(view :View){
        val intent= Intent(this,RegisterActivity::class.java)
         startActivity(intent)
    }
}