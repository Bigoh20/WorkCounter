package com.bigoblog.workcounter.database

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "WorkEntity")
data class WorkEntity(
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0,
    var price : Double = 0.0,
    var date : String = "",
    var isEco : Boolean = false,
    var commentary : String = "",
    var gallonsUsed : Double = 0.0
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WorkEntity

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}


/*


recyclerView.scrollToPosition(items.size() - 1);
APUNTA ESO:
 */




