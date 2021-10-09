package com.underbar.nubijaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)


        val btn_about:Button = findViewById(R.id.btn_about)
        val btn_license:Button = findViewById(R.id.btn_license)
        val btn_guide:Button = findViewById(R.id.btn_guide)
        
        // ABOUT 페이지 버튼 클릭시
        btn_about.setOnClickListener {
            val aboutIntent = Intent(this, MenuAboutActivity::class.java)
            startActivity(aboutIntent)
        }

        // 라이센스 정보 버튼 클릭시
        btn_license.setOnClickListener {
            val licenseIntent = Intent(this, MenuLicenseActivity::class.java)
            startActivity(licenseIntent)
        }
        
        // 대여 기능 사용법 버튼 클릭시
        btn_guide.setOnClickListener {
            val manualActivity = Intent(this, RentPageManualActivity::class.java)
            startActivity(manualActivity)
        }


    }
}