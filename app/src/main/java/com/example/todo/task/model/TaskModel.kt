package com.example.todo.task.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.todo.bean.Task
import com.example.todo.bean.TaskGroup
import com.example.todo.sql.MySQLiteOPenHelper
import java.util.*


class TaskModel(context: Context) {
    private val context = context
    private val taskGroupSqLiteOPenHelper: MySQLiteOPenHelper =
        MySQLiteOPenHelper(context, "TaskGroup.db", null, 1)
    private val tasksSqLiteOPenHelper: MySQLiteOPenHelper =
        MySQLiteOPenHelper(context, "Tasks.db", null, 1)

    fun initData(taskGroupList: ArrayList<TaskGroup>) {
        taskGroupSqLiteOPenHelper.writableDatabase
        val db = taskGroupSqLiteOPenHelper.readableDatabase
        val taskGroupCursor: Cursor = db.query("TaskGroup", null, null, null, null, null, null)
        //如果为0 则说明数据库内无数据 则进行添加默认存在的 任务组
        if (taskGroupCursor.count == 0) {
            val values = ContentValues()
            for (i in 0 until taskGroupList.size) {
                values.put("taskGroupName", taskGroupList[i].taskGroupName)
                db.insert("TaskGroup", null, values)
                values.clear()
            }
        } else {
            //不为空 则并不是第一次打开APP 所以直接从数据库中将数据还原到taskGroupList中
            //先清空原来的list
            taskGroupList.clear()
            if (taskGroupCursor.moveToFirst()) {
                do {
                    val taskGroupName: String =
                        taskGroupCursor.getString(taskGroupCursor.getColumnIndex("taskGroupName"))
                    val taskList = ArrayList<Task>()
                    //在tasks数据库中搜索出任务组名为此时任务组名的任务 放入list中
                    tasksSqLiteOPenHelper.writableDatabase
                    val db = tasksSqLiteOPenHelper.readableDatabase
                    val tasksCursor: Cursor = db.query("Tasks", null, null, null, null, null, null)
                    //如果数据库中任务不为0 则搜索添加
                    if (tasksCursor.count != 0) {
                        if (tasksCursor.moveToLast()) {
                            do {
                                val belongingToTaskGroupName: String =
                                    tasksCursor.getString(tasksCursor.getColumnIndex("belongingToTaskGroupName"))
                                val isFinish =
                                    tasksCursor.getInt(tasksCursor.getColumnIndex("isFinish"))
                                val taskName: String =
                                    tasksCursor.getString(tasksCursor.getColumnIndex("taskName"))
                                val startTime: String =
                                    tasksCursor.getString(tasksCursor.getColumnIndex("startTime"))
                                val endTime: String =
                                    tasksCursor.getString(tasksCursor.getColumnIndex("endTime"))
                                //如果任务组名匹配 则将对于任务添加到对于任务组中

                                if (belongingToTaskGroupName == taskGroupName) {
                                    taskList.add(
                                        Task(
                                            taskName,
                                            belongingToTaskGroupName,
                                            startTime,
                                            endTime,
                                            isFinish == 1
                                        )
                                    )
                                }
                                //如果是总任务组 则全部添加进去
                                if (taskGroupName == "总任务") {
                                    taskList.add(
                                        Task(
                                            taskName,
                                            belongingToTaskGroupName,
                                            startTime,
                                            endTime,
                                            isFinish == 1
                                        )
                                    )
                                }
                            } while (tasksCursor.moveToPrevious())
                        }
                    }
                    tasksCursor.close()
                    tasksSqLiteOPenHelper.close()
                    taskGroupList.add(TaskGroup(taskGroupName, taskList))
                } while (taskGroupCursor.moveToNext())
            }
            //将总任务放至list最后
            val taskGroup = taskGroupList[1]
            taskGroupList.removeAt(1)
            taskGroupList.add(taskGroup)
            taskGroupCursor.close()
        }
        taskGroupSqLiteOPenHelper.close()
    }

    //添加task时 将taskGroupList中数据添加进task数据库
    fun addTask(task: Task) {
        tasksSqLiteOPenHelper.writableDatabase
        val db = tasksSqLiteOPenHelper.readableDatabase
        val values = ContentValues()
        values.put("taskName", task.taskName)
        values.put("belongingToTaskGroupName", task.belongingToTaskGroupName)
        values.put("startTime", task.startTime)
        values.put("endTime", task.endTime)
        var isFinish = 0
        if (task.isFinish) {
            isFinish = 1
        }
        values.put("isFinish", isFinish)
        db.insert("Tasks", null, values)
        values.clear()
        taskGroupSqLiteOPenHelper.close()
    }

    //添加任务组
    fun addTaskGroup(taskGroupName: String) {
        taskGroupSqLiteOPenHelper.writableDatabase
        val db = taskGroupSqLiteOPenHelper.readableDatabase
        val values = ContentValues()
        values.put("taskGroupName", taskGroupName)
        db.insert("TaskGroup", null, values)
        values.clear()
        taskGroupSqLiteOPenHelper.close()
    }

    //删除任务
    fun deleteTask(taskGroupName: String, childPosition: Int, taskGroupList: ArrayList<TaskGroup>) {
        val deleteTaskId = getChangeTaskId(taskGroupName, childPosition)
        val positionInTaskList = getChangeTaskPositionInList(deleteTaskId)
        tasksSqLiteOPenHelper.writableDatabase
        val tasksDb = tasksSqLiteOPenHelper.readableDatabase
        tasksDb.delete("Tasks", "id=?", arrayOf(deleteTaskId.toString()))
        //删除集合中总任务中的任务
        taskGroupList[taskGroupList.size - 1].taskList.removeAt(positionInTaskList)
        tasksSqLiteOPenHelper.close()
    }

    //改变数据库中 对应任务的完成状态
    fun changeFinishState(
        groupPosition: Int,
        childPosition: Int,
        taskGroupList: ArrayList<TaskGroup>,
        finish: Boolean
    ): Int {
        val taskGroupName = taskGroupList[groupPosition].taskGroupName
        tasksSqLiteOPenHelper.writableDatabase
        val db = tasksSqLiteOPenHelper.readableDatabase
        val values = ContentValues()
        var isFinish = 0
        if (finish) {
            isFinish = 1
        }
        values.put("isFinish", isFinish)
        //更新数据库
        val changeTaskId = getChangeTaskId(taskGroupName, childPosition)
        db.update("Tasks", values, "id = ?", arrayOf(changeTaskId.toString()))
        //更新集合中 总任务中的任务状态
        val changeTaskPositionInList = getChangeTaskPositionInList(changeTaskId)
        taskGroupList[taskGroupList.size - 1].taskList[changeTaskPositionInList].isFinish = finish
        tasksSqLiteOPenHelper.close()
        return changeTaskPositionInList
    }

    //修改任务组名
    fun changeTaskGroupName(olderName: String, newName: String) {
        taskGroupSqLiteOPenHelper.writableDatabase
        tasksSqLiteOPenHelper.writableDatabase
        val taskGroupDb = taskGroupSqLiteOPenHelper.readableDatabase
        val tasksDb = tasksSqLiteOPenHelper.readableDatabase
        val values = ContentValues()
        values.put("taskGroupName", newName)
        taskGroupDb.update("TaskGroup", values, "taskGroupName = ?", arrayOf(olderName))
        values.clear()
        values.put("belongingToTaskGroupName", newName)
        tasksDb.update("Tasks", values, "belongingToTaskGroupName = ?", arrayOf(olderName))
        tasksSqLiteOPenHelper.close()
        taskGroupSqLiteOPenHelper.close()
    }
    //删除任务组
    fun deleteTaskGroup(taskGroupName: String) {
        taskGroupSqLiteOPenHelper.writableDatabase
        tasksSqLiteOPenHelper.writableDatabase
        val taskGroupDb = taskGroupSqLiteOPenHelper.readableDatabase
        val tasksDb = tasksSqLiteOPenHelper.readableDatabase
        taskGroupDb.delete("TaskGroup","taskGroupName = ?", arrayOf(taskGroupName))
        tasksDb.delete("Tasks","belongingToTaskGroupName = ?", arrayOf(taskGroupName))
        tasksSqLiteOPenHelper.close()
        taskGroupSqLiteOPenHelper.close()
    }
    //改变任务数据
    fun changeTaskData(
        belongingToTaskGroupName: String,
        childPosition: Int,
        values: ContentValues,taskGroupList: ArrayList<TaskGroup>
    ) {
        val deleteTaskId = getChangeTaskId(belongingToTaskGroupName, childPosition)
        val positionInTaskList = getChangeTaskPositionInList(deleteTaskId)
        tasksSqLiteOPenHelper.writableDatabase
        val tasksDb = tasksSqLiteOPenHelper.readableDatabase
        tasksDb.update("Tasks",values,"id = ?", arrayOf(deleteTaskId.toString()))
        //更新集合中总任务中的任务
        val task= taskGroupList[taskGroupList.size - 1].taskList[positionInTaskList]
        task.taskName= values.get("taskName") as String
        task.startTime=values.get("startTime") as String
        task.endTime=values.get("endTime") as String
        tasksSqLiteOPenHelper.close()
    }
    //得到改变了的任务ID
    private fun getChangeTaskId(taskGroupName: String, childPosition: Int): Int {
        var changeTaskId = 0
        taskGroupSqLiteOPenHelper.writableDatabase
        val taskGroupDb = taskGroupSqLiteOPenHelper.readableDatabase
        val taskGroupCursor: Cursor =
            taskGroupDb.query("TaskGroup", null, null, null, null, null, null)
        tasksSqLiteOPenHelper.writableDatabase
        val tasksDb = tasksSqLiteOPenHelper.readableDatabase
        val tasksCursor: Cursor = tasksDb.query("Tasks", null, null, null, null, null, null)
        if (taskGroupCursor.moveToFirst()) {
            do {
                val taskGroupName1: String =
                    taskGroupCursor.getString(taskGroupCursor.getColumnIndex("taskGroupName"))
                if (taskGroupName1 == taskGroupName) {
                    //从需修改任务对应的任务组中搜索该任务
                    var position = -1//用于找到修改的任务所在位置
                    if (tasksCursor.moveToLast()) {
                        do {
                            val belongingToTaskGroupName: String =
                                tasksCursor.getString(tasksCursor.getColumnIndex("belongingToTaskGroupName"))
                            if (belongingToTaskGroupName == taskGroupName) {
                                position += 1//每当搜索到一个任务组中的任务就+1 直到=childPosition 则该任务为要修改的任务
                                if (position == childPosition) {
                                    changeTaskId =
                                        tasksCursor.getInt(tasksCursor.getColumnIndex("id"))
                                }
                            }

                        } while (tasksCursor.moveToPrevious())
                    }
                }
            } while (taskGroupCursor.moveToNext())
        }
        taskGroupSqLiteOPenHelper.close()
        return changeTaskId
    }

    //得到改变了的任务在List中的位置
    private fun getChangeTaskPositionInList(changeTaskId: Int): Int {
        tasksSqLiteOPenHelper.writableDatabase
        val tasksDb = tasksSqLiteOPenHelper.readableDatabase
        val tasksCursor: Cursor = tasksDb.query("Tasks", null, null, null, null, null, null)
        var positionInTaskList = -1
        if (tasksCursor.moveToLast()) {
            do {
                positionInTaskList += 1
                if (changeTaskId == tasksCursor.getInt(tasksCursor.getColumnIndex("id"))) {
                    break
                }


            } while (tasksCursor.moveToPrevious())
        }
        return positionInTaskList
    }




}