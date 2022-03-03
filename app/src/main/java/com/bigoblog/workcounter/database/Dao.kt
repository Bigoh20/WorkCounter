package com.bigoblog.workcounter.database

import androidx.room.*
import androidx.room.Dao

@Dao
interface Dao {

    //Acción para recuperarlos:
    @Query("SELECT * FROM WorkEntity")
    fun getAllWorks() : MutableList<WorkEntity>

    //Acción para recuperar por su id.
    @Query("SELECT * FROM WorkEntity WHERE id = :id")
    fun getWorkById(id : Long) : WorkEntity

    //Acción para insertar un nuevo trabajo.
    @Insert
    fun insertWork(work : WorkEntity) : Long

    //Acción para eliminar
    @Delete
    fun deleteWork(work : WorkEntity)

    //Acción para actualizar.
    @Update
    fun updateWork(work : WorkEntity)






}