package com.underbar.nubijaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // About 버튼 클릭시
        val btn_about:Button = findViewById(R.id.btn_about)
        btn_about.setOnClickListener {

            // AboutActivity 로 인텐트
            val aboutIntent = Intent(this, MenuAboutActivity::class.java)
            startActivity(aboutIntent)



        }

        // 라이센스 정보 버튼 클릭시
        val btn_license:Button = findViewById(R.id.btn_license)
        btn_license.setOnClickListener {
            val licenseIntent = Intent(this, MenuLicenseActivity::class.java)
            startActivity(licenseIntent)
        }


    }
}