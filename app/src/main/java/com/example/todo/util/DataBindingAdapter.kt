package com.example.todo.util

import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("profileUrl")
fun setUpImage(iv: ImageView, profileUrl: String?) {
    //加载图片
    if(profileUrl!=null){
        Glide.with(iv.context).load("$profileUrl").into(iv)
    }else{
        //设置加载失败的图
    }
}