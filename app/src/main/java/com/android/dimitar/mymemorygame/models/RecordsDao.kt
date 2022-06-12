package com.android.dimitar.mymemorygame.models
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RecordsDao {
    @Query("SELECT * FROM records")
    fun getAll(): LiveData<List<Records>>

    @Query("SELECT * FROM records WHERE id IN (:recordsIds)")
    fun loadAllByIds(recordsIds: IntArray):List<Records>

    @Query("SELECT * FROM records WHERE game LIKE :game LIMIT 1")
    fun findByGame(game: String): Records

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addRecord(record: Records)

    @Insert
    fun insertAll(vararg records: Records)

    @Delete
    fun delete(record: Records)

}