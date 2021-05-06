package com.example.todo.task.fragment

import android.content.ContentValues

interface DataAndStateChange {
    fun deleteTask(groupPosition:Int, childPosition:Int)
    fun changeFinishState(groupPosition:Int, childPosition:Int, isFinish:Boolean):Int
    fun changeTaskGroupName(olderName:String,newName:String)
    fun changeGroupExpendState(position:Int)
    fun deleteTaskGroup(taskGroupName:String)
    fun changeTaskData(belongingToTaskGroupName: String, childPosition: Int,values: ContentValues) {

    }
}