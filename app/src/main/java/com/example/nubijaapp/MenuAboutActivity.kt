package com.example.nubijaapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MenuAboutActivity : AppCompatActivity() {

    companion object {
        const val TAG : String = "로그"

        fun newInstance() : BusFragment {
            return BusFragment()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_about)
        Log.d(TAG, "MenuAboutActivity - onCreate() called")
    }
}