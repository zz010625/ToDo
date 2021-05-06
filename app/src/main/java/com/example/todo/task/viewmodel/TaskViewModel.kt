package com.example.todo.task.viewmodel

import android.content.ContentValues
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.todo.base.BaseViewModel
import com.example.todo.bean.Task
import com.example.todo.bean.TaskGroup
import com.example.todo.notice.broadcast.NotificationReceiver
import com.example.todo.task.model.TaskModel
import com.example.todo.util.SendNotificationUtil
import com.example.todo.util.TimeUtil
import kotlin.collections.ArrayList


class TaskViewModel : BaseViewModel() {
    private var sendNotificationTime=0
    private val taskModel by lazy {
        TaskModel(mContext)
    }


    fun initData(taskGroupList: ArrayList<TaskGroup>) {
        taskGroupList.add(TaskGroup("今天", ArrayList()))
        taskGroupList.add(TaskGroup("总任务", ArrayList()))
        taskModel.initData(taskGroupList)

        //根据现有未完成任务 对开始时间小于系统时间的任务进行推送通知
        if(sendNotificationTime<1){
            //一次运行APP只推送一轮
            sendNotification(taskGroupList)
            sendNotificationTime++
        }
    }

    fun addTask(
        taskGroupList: ArrayList<TaskGroup>,
        belongingToTaskGroupName: String,
        taskName: String,
        taskStartTime: String,
        taskEndTime: String
    ) {
        val task = Task(taskName, belongingToTaskGroupName, taskStartTime, taskEndTime, false)
        for (i in 0 until taskGroupList.size) {
            //向指定的任务组/总任务中添加任务
            if (taskGroupList[i].taskGroupName == belongingToTaskGroupName || taskGroupList[i].taskGroupName == "总任务") {
                taskGroupList[i].taskList.add(0, task)
            }
        }
        taskModel.addTask(task)

        //添加新任务时 对新任务进行定时的通知推送
        //任务未完成时
        if (!task.isFinish) {
            if (TimeUtil.getMaxTime(
                    task.startTime,
                    TimeUtil.getSystemTime()
                ) == TimeUtil.getSystemTime()
            ) {
                //任务未完成且任务开始时间小于系统时间 则立刻推送通知
                SendNotificationUtil.setAlarmToService(0, mContext,taskName,taskEndTime)
            } else {
                //任务未完成且任务开始时间大于系统时间 则推迟两者时间差进行通知
                SendNotificationUtil.setAlarmToService(
                    TimeUtil.getTimeDifference(
                        task.startTime,
                        TimeUtil.getSystemTime()
                    ), mContext,taskName,taskEndTime
                )
            }
        }
    }

    fun addTaskGroup(taskGroupList: ArrayList<TaskGroup>, taskGroupName: String) {
        taskGroupList.add(TaskGroup(taskGroupName, ArrayList()))
        //将总任务放至list最后
        val taskGroup = taskGroupList[taskGroupList.size - 2]
        taskGroupList.removeAt(taskGroupList.size - 2)
        taskGroupList.add(taskGroup)
        taskModel.addTaskGroup(taskGroupName)
    }


    fun deleteTask(groupPosition: Int, childPosition: Int, taskGroupList: ArrayList<TaskGroup>) {
        //删除任务组中的任务
        taskGroupList[groupPosition].taskList.removeAt(childPosition)

        val taskGroupName = taskGroupList[groupPosition].taskGroupName
        taskModel.deleteTask(taskGroupName, childPosition, taskGroupList)
    }

    fun changeFinishState(
        groupPosition: Int,
        childPosition: Int,
        taskGroupList: ArrayList<TaskGroup>,
        isFinish: Boolean
    ): Int {
        //改变任务的完成状态
        taskGroupList[groupPosition].taskList[childPosition].isFinish = isFinish
        return taskModel.changeFinishState(groupPosition, childPosition, taskGroupList, isFinish)

    }

    fun changeTaskGroupName(olderName: String, newName: String) {
        taskModel.changeTaskGroupName(olderName, newName)
    }

    fun deleteTaskGroup(taskGroupName: String) {
        taskModel.deleteTaskGroup(taskGroupName)
    }

    fun changeTaskData(
        belongingToTaskGroupName: String,
        childPosition: Int,
        values: ContentValues,
        taskGroupList: ArrayList<TaskGroup>
    ) {
        taskModel.changeTaskData(belongingToTaskGroupName, childPosition, values, taskGroupList)
    }
    //动态注册推送通知的广播
    fun registerBroadcast() {
        val filter = IntentFilter()
        filter.addAction("SEND_NOTIFICATION")

        val broadcastReceiver = NotificationReceiver(mContext)
        LocalBroadcastManager.getInstance(mContext).registerReceiver(broadcastReceiver, filter)
    }
    //发送通知
    private fun sendNotification(taskGroupList: ArrayList<TaskGroup>) {

            val taskList = taskGroupList[taskGroupList.size-1].taskList //获取总任务组
            for (j in 0 until taskList.size) {
                val task = taskList[j]
                //任务未完成时
                if (!task.isFinish) {
                    if (TimeUtil.getMaxTime(
                            task.startTime,
                            TimeUtil.getSystemTime()
                        ) == TimeUtil.getSystemTime()
                    ) {
                        //任务未完成且任务开始时间小于系统时间 则立刻推送通知
                        SendNotificationUtil.setAlarmToService(0, mContext,task.taskName,task.endTime)
                    } else {
                        //任务未完成且任务开始时间大于系统时间 则推迟两者时间差进行通知
                        SendNotificationUtil.setAlarmToService(
                            TimeUtil.getTimeDifference(
                                task.startTime,
                                TimeUtil.getSystemTime()
                            ), mContext
                        ,task.taskName,task.endTime)
                    }
                }
            }

    }
}