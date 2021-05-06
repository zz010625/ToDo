package com.example.todo.user.model


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.todo.sql.MySQLiteOPenHelper


class UserData(context: Context) {
    private val context: Context = context
    private val userSqLiteOPenHelper: MySQLiteOPenHelper =
        MySQLiteOPenHelper(context, "User.db", null, 1)
    private val stateSqLiteOPenHelper: MySQLiteOPenHelper =
        MySQLiteOPenHelper(context, "State.db", null, 1)

    //注册时添加用户
    fun addUser(values: ContentValues) {
        userSqLiteOPenHelper.writableDatabase
        val db = userSqLiteOPenHelper.readableDatabase
        db.insert("User", null, values)
        values.clear()
        userSqLiteOPenHelper.close()
    }

    //登录时获取所填账户的密码/返回无该账户
    fun getUserPassword(account: String): String {
        userSqLiteOPenHelper.writableDatabase
        val db = userSqLiteOPenHelper.readableDatabase
        val cursor: Cursor = db.query("User", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val accountInSQL: String = cursor.getString(cursor.getColumnIndex("account"))
                val password: String = cursor.getString(cursor.getColumnIndex("password"))
                if (accountInSQL == account) {
                    return password
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        userSqLiteOPenHelper.close()
        return "ERROR"
    }

    //登录成功后添加已登录状态与用户信息于数据库中 使下次根据登陆状态自动登录
    fun addLoginState(isLogin: Boolean) {
        stateSqLiteOPenHelper.writableDatabase
        val db = stateSqLiteOPenHelper.readableDatabase
        val values = ContentValues()
        if (isLogin) {
            values.put("isLogin", 1)
        } else {
            values.put("isLogin", 0)
        }
        //因为还没做用户信息的设置的功能 所以先以默认内容存入数据库
        values.put("userName","未命名")
        values.put("userProfileUrl","https://profile.csdnimg.cn/1/F/9/1_m0_52051799")

        db.insert("State", null, values)
        values.clear()
        stateSqLiteOPenHelper.close()
    }

    //检测登录状态
    fun checkLoginState(): Boolean {
        stateSqLiteOPenHelper.writableDatabase
        val db = stateSqLiteOPenHelper.readableDatabase
        val cursor: Cursor = db.query("State", null, null, null, null, null, null)
        if (cursor.moveToLast())
        {  val isLogin:Int=cursor.getInt(cursor.getColumnIndex("isLogin"))
            cursor.close()
            stateSqLiteOPenHelper.close()
            return isLogin==1

        }else{
            stateSqLiteOPenHelper.close()
            return false
        }
    }
    //返回登录账号的信息
    fun getLoginUserMessage():ArrayList<String>{
        stateSqLiteOPenHelper.writableDatabase
        val db = stateSqLiteOPenHelper.readableDatabase
        val cursor: Cursor = db.query("State", null, null, null, null, null, null)
        cursor.moveToLast()
        val userName=cursor.getString(cursor.getColumnIndex("userName"))
        val userProfileUrl=cursor.getString(cursor.getColumnIndex("userProfileUrl"))
        val userMessageList= arrayListOf<String>()
        userMessageList.add(userName)
        userMessageList.add(userProfileUrl)
        stateSqLiteOPenHelper.close()
        return userMessageList
    }
}