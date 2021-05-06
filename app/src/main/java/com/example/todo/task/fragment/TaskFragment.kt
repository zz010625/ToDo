package com.example.todo.task.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.example.todo.R
import com.example.todo.bean.TaskGroup
import com.example.todo.task.adapter.ExtendableListViewAdapter
import com.example.todo.task.viewmodel.TaskViewModel
import com.example.todo.util.TimeUtil
import com.example.todo.util.ToastUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import kotlin.collections.ArrayList


class TaskFragment : Fragment(), DataAndStateChange {
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var extendableListViewAdapter: ExtendableListViewAdapter
    private lateinit var expandableListView: ExpandableListView
    private lateinit var add: FloatingActionButton
    private lateinit var addTask: FloatingActionButton
    private lateinit var addTaskGroup: FloatingActionButton
    private var isClickAdd = false
    private var expendGroup = 0//最后一次展开的group位置
    private var previousExpendGroup = 0//上一次展开的group位置
    private var expendGroupNumber = 1//展开group数量 用于判断是否有展开的group 若无则无法添加任务 默认为1 因为默认打开位置为0的group

    private var taskGroupList = ArrayList<TaskGroup>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initData()
    }

    private fun initData() {
        taskViewModel.initData(taskGroupList)
        extendableListViewAdapter = ExtendableListViewAdapter(this, activity, taskGroupList)
        expandableListView.setAdapter(extendableListViewAdapter)
        expandableListView.setGroupIndicator(null)
        expandableListView.expandGroup(0)
        //关掉expandableListView的默认点击效果 即把expandableListView的默认点击效果设为透明 不然圆角item点击后任出现矩形边框
        expandableListView.selector = ColorDrawable(Color.TRANSPARENT)
        //获取展开的 group 并由此来决定任务是添加到哪个任务组中
        expandableListView.setOnGroupExpandListener {
            previousExpendGroup = expendGroup
            expendGroup = it
            expendGroupNumber += 1
        }
        expandableListView.setOnGroupCollapseListener {
            expendGroup = previousExpendGroup
            expendGroupNumber -= 1
        }
    }

    private fun initView(view: View) {
        //创建viewModel
        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)
        taskViewModel.setContext(activity as Context)
        taskViewModel.registerBroadcast()
        //初始化控件
        expandableListView = view.findViewById(R.id.elv_task_list)
        add = view.findViewById(R.id.fab_add)
        addTask = view.findViewById(R.id.fab_add_task)
        addTaskGroup = view.findViewById(R.id.fab_add_task_group)
        //设置浮动按钮点击事件监听
        setFloatingActionButtonOnClickListener()
    }

    //三个浮动按钮的点击监听
    private fun setFloatingActionButtonOnClickListener() {
        add.setOnClickListener {
            val animatorSet = AnimatorSet()
            if (!isClickAdd) {
                isClickAdd = true
                val moveX = ObjectAnimator.ofFloat(addTaskGroup, "translationX", -200f)
                val moveY = ObjectAnimator.ofFloat(addTask, "translationY", -200f)
                animatorSet.play(moveX).with(moveY)
                animatorSet.start()
            } else {
                isClickAdd = false
                val moveX = ObjectAnimator.ofFloat(addTaskGroup, "translationX", 0f)
                val moveY = ObjectAnimator.ofFloat(addTask, "translationY", 0f)
                animatorSet.play(moveX).with(moveY)
                animatorSet.start()
            }
        }
        addTask.setOnClickListener {
            if (expendGroupNumber > 0) {
                if (extendableListViewAdapter.getTaskGroupName(expendGroup) != "总任务") {
                    val view: View =
                        LayoutInflater.from(context).inflate(R.layout.item_add_task_dialog, null)
                    val taskNameEditText = view.findViewById<EditText>(R.id.et_task_group_name)
                    val taskStartTimeEditText = view.findViewById<EditText>(R.id.et_start_time)
                    val taskEndTimeEditText = view.findViewById<EditText>(R.id.et_end_time)
                    val determine = view.findViewById<TextView>(R.id.tv_determine)
                    val cancel = view.findViewById<TextView>(R.id.tv_cancel)

                    //弹出Dialog输入任务信息
                    val dialog=buildTaskDialog(view, taskStartTimeEditText, taskEndTimeEditText)
                    //点击确定
                    determine.setOnClickListener {
                        //若三个输入框都不为空 才可添加任务
                        if (taskNameEditText.text.isNotEmpty() && taskStartTimeEditText.text.isNotEmpty() && taskEndTimeEditText.text.isNotEmpty()) {
                            //判断时间格式是否设置正确
                            if (TimeUtil.getMaxTime(
                                    taskStartTimeEditText.text.toString(),
                                    taskEndTimeEditText.text.toString()
                                ) == taskEndTimeEditText.text.toString()
                            ) {
                                taskViewModel.addTask(
                                    taskGroupList,
                                    extendableListViewAdapter.getTaskGroupName(expendGroup),
                                    taskNameEditText.text.toString(),
                                    taskStartTimeEditText.text.toString(),
                                    taskEndTimeEditText.text.toString()
                                )
                                extendableListViewAdapter.notifyDataSetChanged()
                                dialog.dismiss()
                            } else {
                                ToastUtil.showMsg(activity as Context, "时间设置不正确!")

                            }

                        } else {
                            ToastUtil.showMsg(activity as Context, "请完整填写信息!")
                        }
                    }
                    //点击取消
                    cancel.setOnClickListener {
                        dialog.dismiss()
                    }
                } else {
                    ToastUtil.showMsg(activity as Context, "不能在总任务中添加任务 请收起总任务!")
                }
            } else {
                ToastUtil.showMsg(activity as Context, "请展开一个任务组以添加任务!")
            }


        }
        addTaskGroup.setOnClickListener {
            //弹出Dialog输入任务组信息
            var builder: AlertDialog.Builder
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.item_add_task_group_dialog, null)
            val taskGroupNameEditText = view.findViewById<EditText>(R.id.et_task_group_name)
            val determine = view.findViewById<TextView>(R.id.tv_determine)
            val cancel = view.findViewById<TextView>(R.id.tv_cancel)

            builder = AlertDialog.Builder(activity as Context).setView(view)
            val dialog = builder.create()
            dialog.show()

            //点击确定
            determine.setOnClickListener {
                //若输入框都不为空 才可添加任务组
                if (taskGroupNameEditText.text.isNotEmpty()) {
                    taskViewModel.addTaskGroup(taskGroupList, taskGroupNameEditText.text.toString())
                    extendableListViewAdapter.notifyDataSetChanged()
                    dialog.dismiss()
                } else {
                    ToastUtil.showMsg(activity as Context, "请完整填写信息!")
                }
            }
            //点击取消
            cancel.setOnClickListener {
                dialog.dismiss()
            }
        }

    }
    fun buildTaskDialog(view: View,taskStartTimeEditText:EditText,taskEndTimeEditText:EditText):Dialog{
        //弹出Dialog输入任务信息
        var builder = AlertDialog.Builder(activity as Context).setView(view)
        val dialog = builder.create()
        dialog.show()

        //时间的配置
        val startDate: Calendar = Calendar.getInstance()
        val endDate: Calendar = Calendar.getInstance()
        startDate.set(
            startDate.get(Calendar.YEAR),
            startDate.get(Calendar.MONTH),
            startDate.get(Calendar.DAY_OF_MONTH),
            startDate.get(Calendar.HOUR_OF_DAY),
            startDate.get(Calendar.MINUTE)
        )
        endDate.set(2022, 12, 31, 0, 0)


        //设置开始时间
        taskStartTimeEditText.setOnClickListener {
            val pvTime =
                TimePickerBuilder(activity,
                    OnTimeSelectListener { date, v ->
                        taskStartTimeEditText.setText(TimeUtil.getTime(date))
                    })
                    .setType(booleanArrayOf(true, true, true, true, true, false))
                    .setCancelText("取消")//取消按钮文字
                    .setSubmitText("确定")//确认按钮文字
                    .setTitleSize(20)//标题文字大小
                    .setTitleText("请选择任务开始时间")//标题文字
                    .setOutSideCancelable(false)//点击屏幕，点在控件外部范围时，是否取消显示
                    .isCyclic(true)//是否循环滚动
                    .setTitleColor(Color.BLACK)//标题文字颜色
                    .setSubmitColor(Color.BLUE)//确定按钮文字颜色
                    .setCancelColor(Color.BLUE)//取消按钮文字颜色
                    .setRangDate(startDate, endDate)//起始终止年月日设定
                    .setLabel("年", "月", "日", "时", "分", "秒")//默认设置为年月日时分秒
                    .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                    .isDialog(true)//是否显示为对话框样式
                    .build()
            pvTime.show()
        }
        //设置结束时间
        taskEndTimeEditText.setOnClickListener {
            val pvTime =
                TimePickerBuilder(activity,
                    OnTimeSelectListener { date, v ->
                        taskEndTimeEditText.setText(TimeUtil.getTime(date))
                    })
                    .setType(booleanArrayOf(true, true, true, true, true, false))
                    .setCancelText("取消")//取消按钮文字
                    .setSubmitText("确定")//确认按钮文字
                    .setTitleSize(20)//标题文字大小
                    .setTitleText("请选择任务结束时间")//标题文字
                    .setOutSideCancelable(false)//点击屏幕，点在控件外部范围时，是否取消显示
                    .isCyclic(true)//是否循环滚动
                    .setTitleColor(Color.BLACK)//标题文字颜色
                    .setSubmitColor(Color.BLUE)//确定按钮文字颜色
                    .setCancelColor(Color.BLUE)//取消按钮文字颜色
                    .setRangDate(startDate, endDate)//起始终止年月日设定
                    .setLabel("年", "月", "日", "时", "分", "秒")//默认设置为年月日时分秒
                    .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                    .isDialog(true)//是否显示为对话框样式
                    .build()
            pvTime.show()
        }
        return dialog
    }


    /**
     * 接口回调方法
     * 调用viewModel中方法处理task的各项事件
     */
    override fun deleteTask(groupPosition: Int, childPosition: Int) {
        taskViewModel.deleteTask(groupPosition, childPosition, taskGroupList)
        extendableListViewAdapter.notifyDataSetChanged()

    }

    override fun changeFinishState(groupPosition: Int, childPosition: Int, isFinish: Boolean): Int {
        return taskViewModel.changeFinishState(
            groupPosition,
            childPosition,
            taskGroupList,
            isFinish
        )

    }

    override fun changeTaskGroupName(olderName: String, newName: String) {
        taskViewModel.changeTaskGroupName(olderName, newName)
    }

    override fun changeGroupExpendState(position: Int) {
        if (expandableListView.isGroupExpanded(position)) {
            expandableListView.collapseGroup(position)
        } else {
            expandableListView.expandGroup(position)
        }
    }

    override fun deleteTaskGroup(taskGroupName: String) {
        taskViewModel.deleteTaskGroup(taskGroupName)
    }
    override fun changeTaskData(belongingToTaskGroupName: String, childPosition: Int,values: ContentValues) {
        taskViewModel.changeTaskData(belongingToTaskGroupName,childPosition,values,taskGroupList)
    }
    override fun onResume() {
        extendableListViewAdapter.notifyDataSetChanged()
        super.onResume()
    }


}