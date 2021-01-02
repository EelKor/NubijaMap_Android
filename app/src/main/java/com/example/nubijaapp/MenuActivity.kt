package com.example.nubijaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class MenuActivity : AppCompatActivity() {

    companion object {
        const val TAG : String = "로그"

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // About 버튼 클릭시
        val btn_about:Button = findViewById(R.id.btn_about)
        btn_about.setOnClickListener {

            Log.d(TAG, "MenuActivity - About 버튼 클릭")

            // AboutActivity 로 인텐트
            val aboutIntent = Intent(this, MenuAboutActivity::class.java)
            startActivity(aboutIntent)

        }
    }
}