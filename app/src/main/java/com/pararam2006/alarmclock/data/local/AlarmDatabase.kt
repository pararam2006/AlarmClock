package com.pararam2006.alarmclock.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pararam2006.alarmclock.data.local.converters.AlarmTypeConverters
import com.pararam2006.alarmclock.data.local.dao.AlarmDao
import com.pararam2006.alarmclock.domain.model.Alarm

@Database(
    entities = [Alarm::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(AlarmTypeConverters::class)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alarms ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}