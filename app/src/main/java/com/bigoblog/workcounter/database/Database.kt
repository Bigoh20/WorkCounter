package com.bigoblog.workcounter.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WorkEntity::class], version = 2)
abstract class Database : RoomDatabase(){

//Retornar el objeto.
    abstract fun getDao() : Dao
}