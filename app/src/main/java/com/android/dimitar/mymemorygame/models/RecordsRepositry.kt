package com.android.dimitar.mymemorygame.models

import androidx.lifecycle.LiveData

class RecordsRepositry(private val recordsDao: RecordsDao) {

    val readAllData: LiveData<List<Records>> = recordsDao.getAll();

    suspend fun addRecord(record: Records){
        recordsDao.addRecord(record)
    }

}