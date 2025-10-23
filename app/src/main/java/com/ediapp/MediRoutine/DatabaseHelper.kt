package com.ediapp.MediRoutine

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Action(
    val id: Long,
    val actType: String?,
    val actValue: Int,
    val actCreatedAt: String?,
    val actDeletedAt: String?,
    val actMessage: String?,
    val actStatus: String?,
    val actRef: String?
)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "MediRoutine.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "tb_actions"
        const val COL_ID = "_id"
        const val COL_ACT_TYPE = "act_type"
        /* act_type : install, alarm, do */

        const val COL_ACT_VALUE = "act_value"
        const val COL_ACT_CREATED_AT = "act_created_at"
        const val COL_ACT_DELETED_AT = "act_deleted_at"
        const val COL_ACT_MESSAGE = "act_message"
        const val COL_ACT_STATUS = "act_status"
        const val COL_ACT_REF = "act_ref"

        const val CREATE_ACT_TABLE = "CREATE TABLE $TABLE_NAME (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_ACT_TYPE VARCHAR(50), " +
                "$COL_ACT_VALUE INTEGER DEFAULT 0, " +
                "$COL_ACT_CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "$COL_ACT_DELETED_AT DATETIME DEFAULT NULL, " +
                "$COL_ACT_MESSAGE VARCHAR(250), " +
                "$COL_ACT_STATUS VARCHAR(50), " +
                "$COL_ACT_REF VARCHAR(50))"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_ACT_TABLE)
        val values = ContentValues().apply {
            put(COL_ACT_TYPE, "install")
            put(COL_ACT_VALUE, 1)
            put(COL_ACT_MESSAGE, "앱 설치")
            put(COL_ACT_STATUS, "complete")
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

    fun addDoAction(): Long {
        return addDoAction(Date())
    }

    fun addDoAction(createdAt: Date): Long {
        val db = this.writableDatabase
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val values = ContentValues().apply {
            put(COL_ACT_TYPE, "drug")
            put(COL_ACT_STATUS, "complete")
            put(COL_ACT_VALUE, 1)
            put(COL_ACT_MESSAGE, "약복용")
            put(COL_ACT_CREATED_AT, sdf.format(createdAt))
        }
        val id = db.insert(TABLE_NAME, null, values)
        db.close()
        return id
    }

    fun getDrugActionCount(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME WHERE $COL_ACT_TYPE = 'drug' AND $COL_ACT_DELETED_AT IS NULL", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count
    }

    fun getAllActions(): List<Action> {
        val actions = mutableListOf<Action>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COL_ACT_DELETED_AT IS NULL ORDER BY $COL_ID DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val action = Action(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                    actType = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACT_TYPE)),
                    actValue = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ACT_VALUE)),
                    actCreatedAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACT_CREATED_AT)),
                    actDeletedAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACT_DELETED_AT)),
                    actMessage = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACT_MESSAGE)),
                    actStatus = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACT_STATUS)),
                    actRef = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACT_REF))
                )
                actions.add(action)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return actions
    }

    fun deleteAction(id: Long) {
        val db = this.writableDatabase
        val values = ContentValues()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        values.put(COL_ACT_DELETED_AT, sdf.format(Date()))
        db.update(TABLE_NAME, values, "$COL_ID = ?", arrayOf(id.toString()))
        db.close()
    }
}
