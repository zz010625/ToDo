package com.example.todo.user.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.todo.R
import com.example.todo.databinding.FragmentUserBinding
import com.example.todo.user.activity.LoginActivity
import com.example.todo.user.viewmodel.UserViewModel

class UserFragment : Fragment() {
    private lateinit var userViewModel: UserViewModel
    private lateinit var userProfile: ImageView
    private lateinit var binding:FragmentUserBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //创建binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        addObserve()
    }


    private fun initData() {
        //根据登录状态加载不同数据
        userViewModel.initData()
    }

    override fun onStart() {
        super.onStart()
        initData()
    }
    private fun init(view: View) {
        //创建viewModel
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        userViewModel.setContext(activity as Context)
        binding.viewModel = userViewModel
        userProfile = view.findViewById(R.id.iv_user_profile)
        userProfile.setOnClickListener {
            if (!userViewModel.checkIsLogin()){
                val intent = Intent(activity, LoginActivity::class.java)
                startActivity(intent)
            }

        }
    }
    private fun addObserve(){
        userViewModel.userName.observe(viewLifecycleOwner, Observer {
            binding.viewModel = userViewModel
        })
        userViewModel.userProfileUrl.observe(viewLifecycleOwner, Observer {
            binding.viewModel = userViewModel
        })
    }
}