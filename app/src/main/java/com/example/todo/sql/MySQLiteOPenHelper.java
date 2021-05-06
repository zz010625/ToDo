package com.example.todo.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MySQLiteOPenHelper extends SQLiteOpenHelper {
    private Context context;
    public static final String CREATE_USER = "create table User("
            + "id integer primary key autoincrement,"
            + "account text,"
            + "password text,"
            + "userName text,"
            + "userProfileUrl text)";
    public static final String CREATE_STATE = "create table State("
            + "userName text,"
            + "userProfileUrl text,"
            + "isLogin integer)";
    public static final String CREATE_TASK_GROUP = "create table TaskGroup("
            + "id integer primary key autoincrement,"
            + "taskGroupName text)";
    public static final String CREATE_TASKS = "create table Tasks("
            + "id integer primary key autoincrement,"
            +"isFinish integer,"
            +"belongingToTaskGroupName text,"
            + "startTime text,"
            + "endTime text,"
            + "taskName text)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER);
        db.execSQL(CREATE_STATE);
        db.execSQL(CREATE_TASK_GROUP);
        db.execSQL(CREATE_TASKS);

    }

    public MySQLiteOPenHelper(@Nullable Context context, @Nullable String name,
                              @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists User");
        db.execSQL("drop table if exists State");
        db.execSQL("drop table if exists TaskGroup");
        db.execSQL("drop table if exists Tasks");
        onCreate(db);

    }
}
