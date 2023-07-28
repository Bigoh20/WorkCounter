package com.bigoblog.workcounter.database

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.room.Room

//Seguir el patrón singletón. //TODO
class WorkInit : Application(){

    companion object{
        lateinit var database : Database
        lateinit var spItem : SharedManager
    }
    override fun onCreate() {

        super.onCreate()
        //Inicializar database.
        database = Room.databaseBuilder(this,
        Database::class.java,
        "Database",).build()

        //Inicializar sharedPreferences:
        spItem = SharedManager(applicationContext)
    }
}