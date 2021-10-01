package com.underbar.nubijaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class RentPageManualActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rent_page_manual)

        val Before: Button = findViewById(R.id.btnBefore)
        val Next: Button = findViewById(R.id.btnNext)

        Before.setOnClickListener {
            finish()
        }

        Next.setOnClickListener {
            val intent = Intent( this, RentPageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}