package com.android.dimitar.mymemorygame.models

enum class BoardSize (val numCards: Int) {
    EASY(8),
    MEDIUM(18),
    HARD(24);

    fun getWidht():Int {
        return  when (this){
            EASY -> 2
            MEDIUM -> 3
            HARD -> 4
        }
    }

    fun getHeight(): Int{
        return numCards / getWidht();
    }

    fun getNumPairs(): Int {
        return  numCards / 2;
    }

}