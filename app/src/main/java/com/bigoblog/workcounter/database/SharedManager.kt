package com.bigoblog.workcounter.database

import android.content.Context

class SharedManager(context : Context) {

    private val preferences = context.getSharedPreferences("sharedPreferences", 0)

    fun putString(key : String, value : String){
        preferences.edit().putString(key, value).apply()
    }
    fun getString(key : String) : String{
        return preferences.getString(key, "")!!
    }

    fun putBoolean(key : String, value : Boolean){
        preferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key : String) : Boolean{
        return preferences.getBoolean(key, false)
    }

}