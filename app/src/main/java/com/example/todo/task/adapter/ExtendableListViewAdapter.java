package com.example.todo.task.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.todo.R;
import com.example.todo.bean.Task;
import com.example.todo.bean.TaskGroup;
import com.example.todo.task.fragment.TaskFragment;
import com.example.todo.util.TimeUtil;
import com.example.todo.util.ToastUtil;


import java.util.ArrayList;

public class ExtendableListViewAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList taskGroupList;
    private TaskFragment taskFragment;
    private int mChangeTaskPositionInList;
    private View changeTaskInAllTasks;
    private ArrayList taskInAllTasks = new ArrayList();
    private float x1=0f;
    private float x2=0f;
    public ExtendableListViewAdapter(TaskFragment taskFragment, Context context, ArrayList<TaskGroup> taskGroupList) {
        this.context = context;
        this.taskGroupList = taskGroupList;
        this.taskFragment = taskFragment;
    }

    @Override
    public int getGroupCount() {
        return taskGroupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        TaskGroup taskGroup = (TaskGroup) taskGroupList.get(groupPosition);
        return taskGroup.getTaskList().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return taskGroupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        TaskGroup taskGroup = (TaskGroup) taskGroupList.get(groupPosition);
        return taskGroup.getTaskList().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder holder;

        holder = new GroupHolder();
        convertView = LayoutInflater.from(context).inflate(
                R.layout.item_task_group, null);
        holder.taskGroupName = (TextView) convertView
                .findViewById(R.id.tv_group_name);
        holder.arrow = (ImageView) convertView.findViewById(R.id.iv_arrow);

        TaskGroup taskGroup = (TaskGroup) taskGroupList.get(groupPosition);
        holder.taskGroupName.setText(taskGroup.getTaskGroupName());
        if (isExpanded) {
            holder.arrow.setImageResource(R.mipmap.ic_list_open);
        } else {
            holder.arrow.setImageResource(R.mipmap.ic_list_close);
        }

        //设置长按事件
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //震动
                Vibrator vibrator = (Vibrator) context.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
                vibrator.vibrate(300);
                //禁止对总任务进行操作
                if (groupPosition != getGroupCount() - 1) {
                    //弹出Dialog 对任务组进行修改/删除
                    View view = LayoutInflater.from(context).inflate(R.layout.item_revise_task_group_dialog, null);
                    EditText taskGroupNameEditText = view.findViewById(R.id.et_task_group_name);
                    TextView determine = view.findViewById(R.id.tv_determine);
                    TextView delete = view.findViewById(R.id.tv_delete);
                    taskGroupNameEditText.setText(taskGroup.getTaskGroupName());//填充原名
                    AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(view);
                    Dialog dialog = builder.create();
                    dialog.show();
                    //监听点击事件
                    determine.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!TextUtils.isEmpty(taskGroupNameEditText.getText())) {
                                //回调到数据库修改任务组名
                                taskFragment.changeTaskGroupName(taskGroup.getTaskGroupName(), taskGroupNameEditText.getText().toString());
                                //修改集合中任务组名
                                taskGroup.setTaskGroupName(taskGroupNameEditText.getText().toString());
                                dialog.dismiss();
                                //刷新数据
                                notifyDataSetChanged();
                            } else {
                                ToastUtil.INSTANCE.showMsg(context, "请完整填写信息!");
                            }
                        }
                    });
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //回调到数据库删除任务组及该任务组下的所以任务
                            taskFragment.deleteTaskGroup(taskGroup.getTaskGroupName());
                            //删除集合中该任务组及总任务组中与该任务组对应的任务
                            taskGroupList.remove(groupPosition);
                            TaskGroup allTasksGroup = (TaskGroup) taskGroupList.get(taskGroupList.size() - 1);
                            for (int i = 0; i < allTasksGroup.getTaskList().size(); i++) {
                                if (allTasksGroup.getTaskList().get(i).getBelongingToTaskGroupName().equals(taskGroup.getTaskGroupName())) {
                                    allTasksGroup.getTaskList().remove(i);
                                }
                            }
                            dialog.dismiss();
                            //刷新数据
                            notifyDataSetChanged();
                        }
                    });
                } else {
                    ToastUtil.INSTANCE.showMsg(context, "无法对总任务进行修改!");
                }
                return false;
            }
        });
        //重写view点击事件 因为设置了长按事件后 原来的点击展开事件失效了
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskFragment.changeGroupExpendState(groupPosition);
            }
        });
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder holder = new ChildHolder();
        convertView = LayoutInflater.from(context).inflate(
                R.layout.item_task, null);
        holder.taskName = convertView.findViewById(R.id.tv_taskName);
        holder.taskCheckBox = convertView.findViewById(R.id.cb_task);
        holder.taskTime = convertView.findViewById(R.id.tv_time);
        holder.taskLayout = convertView.findViewById(R.id.layout_task);
        holder.delete=convertView.findViewById(R.id.iv_delete);
        TaskGroup taskGroup = (TaskGroup) taskGroupList.get(groupPosition);
        Task task =taskGroup.getTaskList().get(childPosition);
        holder.taskName.setText(task.getTaskName());
        holder.taskTime.setText("截止至" + task.getEndTime());


        //获取总任务中所有任务对象
        if (groupPosition == getGroupCount() - 1) {
            taskInAllTasks.add(convertView);

        } else {
            //clear的原因是 每次刷新 所有item对象都会重新被创建 所以之前保存的任务对象 在一次刷新后就没意义了
            taskInAllTasks.clear();
        }
        //添加勾选框监听
        View finalConvertView = convertView;
        holder.taskCheckBox.setOnClickListener(new View.OnClickListener() {
            int changeTaskPositionInList;

            @Override
            public void onClick(View v) {

                if (groupPosition != getGroupCount() - 1) {//非总任务组
                    //当勾选状态由 选中-->未选中时 检查任务截至时间是否大于当前系统时间 若小于则不允许取消完成状态
                    if (!holder.taskCheckBox.isChecked()) {
                        if (TimeUtil.INSTANCE.getMaxTime(task.getEndTime(), TimeUtil.INSTANCE.getSystemTime()).equals(TimeUtil.INSTANCE.getSystemTime())) {
                            //不允许取消
                            holder.taskCheckBox.setChecked(true);
                            ToastUtil.INSTANCE.showMsg(context, "该任务截止时间已到 已完成");
                        }
                    }
                    //回调改变集合以及数据库中任务完成状态
                    changeTaskPositionInList = taskFragment.changeFinishState(groupPosition, childPosition, holder.taskCheckBox.isChecked());
                    mChangeTaskPositionInList = changeTaskPositionInList;

                    //初始化当前点击的任务在总任务组中对应任务的对象
                    ChildHolder allTasksHolder = new ChildHolder();
                    if (taskInAllTasks.size() != 0) {
                        changeTaskInAllTasks = (View) taskInAllTasks.get(mChangeTaskPositionInList);
                        allTasksHolder.taskTime = changeTaskInAllTasks.findViewById(R.id.tv_time);
                        allTasksHolder.taskCheckBox = changeTaskInAllTasks.findViewById(R.id.cb_task);
                    }


                    //通过动画改变透明度
                    if (holder.taskCheckBox.isChecked()) {
                        //此任务状态改变
                        finalConvertView.animate().alpha(0.3f);
                        holder.taskTime.setText("已完成");
                        //总任务组中该任务状态改变
                        if (taskInAllTasks.size() != 0) {
                            changeTaskInAllTasks.animate().alpha(0.3f);
                            allTasksHolder.taskCheckBox.setChecked(true);
                            allTasksHolder.taskTime.setText("已完成");
                        }

                    } else {
                        //此任务状态改变
                        finalConvertView.animate().alpha(1f);
                        holder.taskTime.setText("截止至" + task.getEndTime());
                        //总任务组中该任务状态改变
                        if (taskInAllTasks.size() != 0) {
                            changeTaskInAllTasks.animate().alpha(1f);
                            allTasksHolder.taskCheckBox.setChecked(false);
                            allTasksHolder.taskTime.setText("截止至" + task.getEndTime());
                        }
                    }


                } else {//总任务组
                    //避免勾选框的状态因为点击而发送改变
                    if (holder.taskCheckBox.isChecked()) {
                        holder.taskCheckBox.setChecked(false);
                    } else {
                        holder.taskCheckBox.setChecked(true);
                    }
                    ToastUtil.INSTANCE.showMsg(context, "请到具体任务组中设置任务完成情况!");
                }
            }
        });

        //为任务信息布局设置点击事件 用于弹出Dialog修改任务信息
        holder.taskLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groupPosition != getGroupCount() - 1) {//非总任务组
                    View view = LayoutInflater.from(context).inflate(R.layout.item_add_task_dialog, null);
                    EditText taskNameEditText = view.findViewById(R.id.et_task_group_name);
                    EditText taskStartTimeEditText = view.findViewById(R.id.et_start_time);
                    EditText taskEndTimeEditText = view.findViewById(R.id.et_end_time);
                    TextView determine = view.findViewById(R.id.tv_determine);
                    TextView cancel = view.findViewById(R.id.tv_cancel);
                    Dialog dialog= taskFragment.buildTaskDialog(view,taskStartTimeEditText,taskEndTimeEditText);
                    //填充原来的信息
                    taskNameEditText.setText(task.getTaskName());
                    taskEndTimeEditText.setText(task.getEndTime());
                    taskStartTimeEditText.setText(task.getStartTime());

                    //点击确定
                    determine.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //若三个输入框都不为空 才可添加任务
                            if (!TextUtils.isEmpty(taskNameEditText.getText()) && !TextUtils.isEmpty(taskStartTimeEditText.getText()) && !TextUtils.isEmpty(taskEndTimeEditText.getText())) {
                                //判断时间格式是否设置正确
                                Log.d("zz",TimeUtil.INSTANCE.getMaxTime(
                                        taskStartTimeEditText.getText().toString(),
                                        taskEndTimeEditText.getText().toString()
                                        )
                                );
                                if (TimeUtil.INSTANCE.getMaxTime(
                                        taskStartTimeEditText.getText().toString(),
                                        taskEndTimeEditText.getText().toString()
                                ) .equals(taskEndTimeEditText.getText().toString())
                                ) {
                                    //修改集合中任务信息
                                    task.setTaskName(taskNameEditText.getText().toString());
                                    task.setStartTime(taskStartTimeEditText.getText().toString());
                                    task.setEndTime(taskEndTimeEditText.getText().toString());
                                    ContentValues values =new ContentValues();
                                    values.put("taskName",task.getTaskName());
                                    values.put("startTime",task.getStartTime());
                                    values.put("endTime",task.getEndTime());

                                    //回调修改数据库中任务信息
                                    taskFragment.changeTaskData(task.getBelongingToTaskGroupName(),childPosition,values);
                                    dialog.dismiss();
                                } else {
                                    ToastUtil.INSTANCE.showMsg(context, "时间设置不正确!");

                                }

                            } else {
                                ToastUtil.INSTANCE.showMsg(context, "请完整填写信息!");
                            }


                            //刷新数据
                            notifyDataSetChanged();
                        }
                    });
                    //取消的点击事件
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    } else {
                    ToastUtil.INSTANCE.showMsg(context,"请到具体任务组中操作!");
                }

            }
        });

        //设置删除的点击事件
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groupPosition != getGroupCount() - 1) {//非总任务组
                    ObjectAnimator moveX =
                            ObjectAnimator.ofFloat(finalConvertView, "translationX", -1500f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(moveX);
                    animatorSet.start();
                    //回调删除List以及数据库中任务
                    taskFragment.deleteTask(groupPosition, childPosition);
                }else {
                    ToastUtil.INSTANCE.showMsg(context,"请到具体任务组中操作!");
                }

            }
        });



        //初始化任务完成状态 即是否勾选
        initFinishState(holder, finalConvertView, groupPosition, childPosition);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }



    //初始化任务完成状态
    public void initFinishState(ChildHolder holder, View finalConvertView, int groupPosition, int childPosition) {
        TaskGroup taskGroup = (TaskGroup) taskGroupList.get(groupPosition);
        Task task = taskGroup.getTaskList().get(childPosition);
        //判断是否自动完成 即系统时间大于截至时间
        if (TimeUtil.INSTANCE.getMaxTime(task.getEndTime(), TimeUtil.INSTANCE.getSystemTime()).equals(TimeUtil.INSTANCE.getSystemTime())) {
            //自动完成 回调修改状态
            taskFragment.changeFinishState(groupPosition, childPosition, true);

        }
        //直接设置透明度 不使用动画 避免每次重构都加载动画
        if (task.isFinish()) {
            holder.taskTime.setText("已完成");
            holder.taskCheckBox.setChecked(true);
            finalConvertView.setAlpha(0.3f);
        } else {
            holder.taskCheckBox.setChecked(false);
            holder.taskTime.setText("截止至" + task.getEndTime());
            finalConvertView.setAlpha(1f);
        }

    }


    class GroupHolder {
        TextView taskGroupName;
        ImageView arrow;
    }

    class ChildHolder {
        TextView taskName;
        CheckBox taskCheckBox;
        TextView taskTime;
        ViewGroup taskLayout;
        ImageView delete;
    }

    //通过group位置来获取groupName
    public String getTaskGroupName(int position) {
        TaskGroup taskGroup = (TaskGroup) taskGroupList.get(position);
        return taskGroup.getTaskGroupName();
    }
}
