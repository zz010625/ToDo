package com.example.todo.user.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.todo.R
import com.example.todo.user.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var account:EditText
    private lateinit var password:EditText
    private lateinit var verifyPassword:EditText
    private lateinit var register:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        init()
        //设置三个输入框的监听和验证 用于注册格式的效验
        setEditTextListener()
    }
    private fun init() {
        //创建ViewModel
        registerViewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)
        registerViewModel.setContext(this)
        //初始化控件
        account = findViewById(R.id.et_register_account)
         password = findViewById(R.id.et_register_password)
         verifyPassword = findViewById(R.id.et_register_verify_password)
         register = findViewById(R.id.btn_register)
    }
    private fun setEditTextListener(){
        //对账号栏输入长度进行验证
        account.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (account.length() < 2) {
                    register.isEnabled = false
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        //对密码栏输入长度进行验证
        password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                register.isEnabled = password.editableText.toString() == verifyPassword.editableText
                    .toString() && password.length() >= 6
            }

            override fun afterTextChanged(s: Editable) {}
        })
        //对确认密码栏进行核实
        verifyPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                register.isEnabled = password.editableText.toString() == verifyPassword.editableText
                    .toString() && account.length() >= 2 && password.length() >= 6
            }
        })
    }
    //点击返回键
    fun onClickBack(view: View){
        finish()
    }
    //点击注册
    fun onClickRegister(view:View){
        //调用viewModel中方法储存注册信息于数据库中
        registerViewModel.setUserMessage(account.editableText.toString(),password.editableText.toString())
        finish()
    }
}