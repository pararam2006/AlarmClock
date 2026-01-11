package com.pararam2006.alarmclock.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pararam2006.alarmclock.data.local.dao.AlarmDao
import com.pararam2006.alarmclock.data.local.converters.AlarmTypeConverters
import com.pararam2006.alarmclock.domain.model.Alarm

@Database(entities = [Alarm::class], version = 1)
@TypeConverters(AlarmTypeConverters::class)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}