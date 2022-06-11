package com.android.dimitar.mymemorygame.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Records (
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "game") val game: String?,
    @ColumnInfo(name = "score") val score: Int?
        )