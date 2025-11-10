package com.ediapp.m1routine

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.ediapp.m1routine.model.Action
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "MediRoutine.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "tb_actions"
        const val COL_ID = "_id"
        const val COL_ACT_TYPE = "act_type"
        const val COL_ACT_KEY = "act_key"
        /* act_type : install, alarm, drug, log */

        const val COL_ACT_VALUE = "act_value"
        const val COL_ACT_REGISTERED_AT = "act_registered_at"
        const val COL_ACT_CREATED_AT = "created_at"
        const val COL_ACT_DELETED_AT = "deleted_at"
        const val COL_ACT_MESSAGE = "act_message"
        const val COL_ACT_STATUS = "act_status"
        const val COL_ACT_REF = "act_ref"

        const val CREATE_ACT_TABLE = "CREATE TABLE $TABLE_NAME (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_ACT_TYPE VARCHAR(50), " +
                "$COL_ACT_KEY VARCHAR(50), " +
                "$COL_ACT_VALUE INTEGER DEFAULT 0, " +
                "$COL_ACT_REGISTERED_AT DATETIME DEFAULT CURRENT_TIMESTAMP, " +
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
            put(COL_ACT_KEY, "install")
            put(COL_ACT_VALUE, 1)
            put(COL_ACT_MESSAGE, "앱 설치")
            put(COL_ACT_STATUS, "complete")
        }
        db?.insert(TABLE_NAME, null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
//        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")

        if (oldVersion < 2) {
            Log.d("DatabaseHelper", "Upgrading database from version $oldVersion to $newVersion")

            // 'my_table' 테이블에 'new_column'이라는 TEXT 타입의 컬럼을 추가합니다.
            // 추가할 컬럼에 데이터가 없을 경우를 대비하여 DEFAULT 값(선택 사항)을 지정할 수 있습니다.
//            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN new_column TEXT DEFAULT 'default_value';");
        }
        onCreate(db)
    }

    fun addDrugAction(): Long {
        return addDrugAction(Date())
    }

    fun addDrugAction(registedAt: Date): Long {
        val actKeyDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(registedAt)
        val actKey = "drug-${actKeyDate}"

        if (isDrugExists(actKey)) {
            return -1L
        }

        val db = this.writableDatabase
        val actRegisteredAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(registedAt)
        val values = ContentValues().apply {
            put(COL_ACT_TYPE, "drug")
            put(COL_ACT_KEY, actKey)
            put(COL_ACT_STATUS, "complete")
            put(COL_ACT_VALUE, 1)
            put(COL_ACT_MESSAGE, "약복용")
            put(COL_ACT_REGISTERED_AT, actRegisteredAt)
        }
        val id = db.insert(TABLE_NAME, null, values)
        db.close()
        return id
    }

    fun isDrugExists(actKey: String): Boolean {
        val db = this.readableDatabase
        val sql = "SELECT COUNT(*) FROM $TABLE_NAME WHERE $COL_ACT_KEY = ? AND $COL_ACT_DELETED_AT IS NULL"

        val cursor = db.rawQuery(sql, arrayOf(actKey))
        var count= 0
        if (cursor.moveToFirst()) {
            count  = cursor.getInt(0)
        }

        Log.d("DatabaseHelper", "isDrugExists: $sql, $actKey, count=$count | ${count > 0}")

        cursor.close()
        db.close()
        return count > 0
    }


    fun getDrugTodayCount(): Int {
        val actKeyDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val actKey = "drug-${actKeyDate}"

        if (isDrugExists(actKey)) {
            return 1
        } else
            return 0
    }

    fun getDrugListsByMonthOrWeek(monthOrWeek: Int = 1, month: String?, orderBy: String = COL_ID, orderDirection: String = "DESC"): List<Action> {
        val actions = mutableListOf<Action>()
        val db = this.readableDatabase

        var whereSql = if (monthOrWeek == 1) "strftime('%Y-%m', $COL_ACT_REGISTERED_AT) = '$month'" else "1=1"

        if (monthOrWeek == 2) {
            whereSql = "strftime('%Y-%m-%d', $COL_ACT_REGISTERED_AT) >= '$month'"
        }

        val sql = "SELECT * FROM $TABLE_NAME WHERE $whereSql AND $COL_ACT_DELETED_AT IS NULL ORDER BY $orderBy $orderDirection"

        Log.d("DatabaseHelper", "SQL: $sql")

        val cursor = db.rawQuery(sql, null)
        if (cursor.moveToFirst()) {
            do {
                val action = Action(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                    actType = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACT_TYPE)),
                    actKey = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACT_KEY)),
                    actValue = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ACT_VALUE)),
                    actRegisteredAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACT_REGISTERED_AT)),
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

    fun getDrugActionsByDateRange(startDate: String): Map<String, List<Action>> {
        val actionsByDate = mutableMapOf<String, MutableList<Action>>()
        val db = this.readableDatabase
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val sql = "SELECT * FROM $TABLE_NAME WHERE $COL_ACT_TYPE = 'drug' AND date($COL_ACT_REGISTERED_AT) >= date('$startDate') AND $COL_ACT_DELETED_AT IS NULL ORDER BY $COL_ACT_REGISTERED_AT DESC"

        Log.d("DatabaseHelper", "SQL: $sql")

        val cursor = db.rawQuery(sql, null)
        if (cursor.moveToFirst()) {
            do {
                val action = Action(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                    actType = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACT_TYPE)),
                    actKey = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACT_KEY)),
                    actValue = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ACT_VALUE)),
                    actRegisteredAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACT_REGISTERED_AT)),
                    actCreatedAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACT_CREATED_AT)),
                    actDeletedAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACT_DELETED_AT)),
                    actMessage = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACT_MESSAGE)),
                    actStatus = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACT_STATUS)),
                    actRef = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACT_REF))
                )
                val date = sdf.format(sdf.parse(action.actRegisteredAt!!)!!)
                if (actionsByDate.containsKey(date)) {
                    actionsByDate[date]?.add(action)
                } else {
                    actionsByDate[date] = mutableListOf(action)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return actionsByDate
    }

    fun deleteDrugAction(id: Long) {
        val db = this.writableDatabase
        val values = ContentValues()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        values.put(COL_ACT_DELETED_AT, sdf.format(Date()))
        db.update(TABLE_NAME, values, "$COL_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    fun getAchievementStats(): Pair<Date?, Int> {
        val db = this.readableDatabase
        var startDate: Date? = null
        var drugCount = 0

        val startDateCursor = db.rawQuery("SELECT MIN($COL_ACT_REGISTERED_AT) FROM $TABLE_NAME WHERE $COL_ACT_TYPE = 'drug' AND $COL_ACT_DELETED_AT IS NULL", null)
        if (startDateCursor.moveToFirst()) {
            val dateString = startDateCursor.getString(0)
            if (dateString != null) {
                try {
                    startDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateString)
                } catch (e: ParseException) {
                     Log.e("DatabaseHelper", "Error parsing date: $dateString", e)
                }
            }
        }
        startDateCursor.close()

        val drugCountCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME WHERE $COL_ACT_TYPE = 'drug' AND $COL_ACT_DELETED_AT IS NULL", null)
        if (drugCountCursor.moveToFirst()) {
            drugCount = drugCountCursor.getInt(0)
        }
        drugCountCursor.close()

        db.close()

        return Pair(startDate, drugCount)
    }


    fun getLatestStats(): Pair<Date?, Int> {
        val db = this.readableDatabase
        var startDate: Date? = null
        var drugCount = 0

        val sql = "SELECT max($COL_ACT_REGISTERED_AT) FROM $TABLE_NAME WHERE $COL_ACT_TYPE = 'drug' AND $COL_ACT_DELETED_AT IS NULL"

        val startDateCursor = db.rawQuery(sql, null)
        if (startDateCursor.moveToFirst()) {
            val dateString = startDateCursor.getString(0)
            if (dateString != null) {
                try {
                    startDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateString)
                } catch (e: ParseException) {
                    Log.e("DatabaseHelper", "Error parsing date: $dateString", e)
                }
            }
        }
        startDateCursor.close()
        db.close()

        return Pair(startDate, drugCount)
    }

}
