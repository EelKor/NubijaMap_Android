package com.underbar.nubijaapp

import android.content.Context
import android.content.SharedPreferences

class MySharedPreferences(context: Context) {
    // 파일 이름 및 KEY 정의
    val PREFS_FILENAME = "prefs"
    val PREFS_KEY = "isFirstUseofRentPage"
    
    // SharedPreferences 객체 생성
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    // 대여 기능 교육 페이지를 열었는지 기록하는 변수
    // getter, setter 를 이용
    var myEditSetting: Boolean
        get() = prefs.getBoolean(PREFS_FILENAME, true)
        set(value) = prefs.edit().putBoolean(PREFS_FILENAME, value).apply()


}