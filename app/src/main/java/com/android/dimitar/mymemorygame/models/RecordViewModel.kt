package com.android.dimitar.mymemorygame.models

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecordViewModel(application: Application): AndroidViewModel(application) {
    val readAllData: LiveData<List<Records>>
    private val repositry: RecordsRepositry

    init {
        val recordsDao = AppDatabase.getDatabase(application).recordsDao();
        repositry = RecordsRepositry(recordsDao)
        readAllData = repositry.readAllData;
    }

    fun addRecord(record: Records){
        viewModelScope.launch(Dispatchers.IO) {
            repositry.addRecord(record);
        }
    }
}