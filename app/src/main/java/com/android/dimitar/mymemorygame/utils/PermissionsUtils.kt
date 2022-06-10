package com.android.dimitar.mymemorygame.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun isPermissionGranted(context: Context, premission:String) : Boolean{
    return ContextCompat.checkSelfPermission(context,premission) == PackageManager.PERMISSION_GRANTED
}

fun requestPermission(activty: Activity?, permission:String, requestCode:Int){
    ActivityCompat.requestPermissions(activty!!, arrayOf(permission), requestCode)
}