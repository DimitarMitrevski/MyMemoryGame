package com.android.dimitar.mymemorygame.models

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.Surface
import android.view.WindowManager


enum class BoardSize (val numCards: Int) {
    EASY(8),
    MEDIUM(18),
    HARD(24);
 var  orientation:String="SCREEN_ORIENTATION_PORTRAIT";


    companion object {
        fun getByValue(value:Int)= values().first{
            it.numCards==value
        }
        private const val TAG = "BoardSize"
    }


    fun getWidht():Int {
        Log.d(TAG,"The orentation is $orientation")

        if(orientation=="SCREEN_ORIENTATION_PORTRAIT") {
            return when (this) {
                EASY -> 2
                MEDIUM -> 3
                HARD -> 4
            }
        }else{
            return when (this) {
                EASY -> 4
                MEDIUM -> 6
                HARD ->8
            }
        }
    }

    fun getHeight(): Int{
        return numCards / getWidht();
    }

    fun getNumPairs(): Int {
        return  numCards / 2;
    }

}