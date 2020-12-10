package com.example.nubijaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // About 버튼 클릭시
        val btn_about:Button = findViewById(R.id.btn_about)
        btn_about.setOnClickListener {

            // AboutActivity 로 인텐트
            val aboutIntent = Intent(this, AboutActivity::class.java)
            startActivity(aboutIntent)

        }
    }
}