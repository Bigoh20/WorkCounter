package com.bigoblog.workcounter

import com.bigoblog.workcounter.database.WorkEntity

interface OnClickListener {
    fun setOnClickListener(work : WorkEntity)

}