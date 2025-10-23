package com.ediapp.MediRoutine

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "MediRoutine.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "tb_actions"
        const val COL_ID = "_id"
        const val COL_ACT_TYPE = "act_type"
        const val COL_ACT_VALUE = "act_value"
        const val COL_ACT_CREATED_AT = "act_created_at"
        const val COL_ACT_MESSAGE = "act_message"
        const val COL_ACT_STATUS = "act_status"
        const val COL_ACT_REF = "act_ref"

        const val CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_ACT_TYPE VARCHAR(50), " +
                "$COL_ACT_VALUE INTEGER DEFAULT 0, " +
                "$COL_ACT_CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "$COL_ACT_MESSAGE VARCHAR(250), " +
                "$COL_ACT_STATUS VARCHAR(50), " +
                "$COL_ACT_REF VARCHAR(50))"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE)
        val values = ContentValues().apply {
            put(COL_ACT_TYPE, "install")
            put(COL_ACT_STATUS, "C")
        }
        db?.insert(TABLE_NAME, null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
//        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")

        if (oldVersion < 2) {
            // 'my_table' 테이블에 'new_column'이라는 TEXT 타입의 컬럼을 추가합니다.
            // 추가할 컬럼에 데이터가 없을 경우를 대비하여 DEFAULT 값(선택 사항)을 지정할 수 있습니다.
            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN new_column TEXT DEFAULT 'default_value';");
        }

        onCreate(db)
    }
}
