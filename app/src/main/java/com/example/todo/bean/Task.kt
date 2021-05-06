package com.example.todo.bean

data class Task(var taskName:String,var belongingToTaskGroupName:String,var startTime:String,var endTime:String,var isFinish:Boolean)