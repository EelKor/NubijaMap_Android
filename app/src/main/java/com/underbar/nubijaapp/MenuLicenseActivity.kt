package com.underbar.nubijaapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MenuLicenseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_license)

        val btn_navermap: Button = findViewById(R.id.navermap_sdk)
        btn_navermap.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/navermaps/android-map-sdk/blob/master/LICENSE"))
            startActivity(intent)
        }

        val btn_tedpermission: Button = findViewById(R.id.tedpermission)
        btn_tedpermission.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ParkSangGwon/TedPermission"))
            startActivity(intent)
        }

        val btn_retrofit2: Button = findViewById(R.id.retrofit2)
        btn_retrofit2.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/square/retrofit/blob/master/LICENSE.txt"))
            startActivity(intent)
        }

        val btn_androidx: Button = findViewById(R.id.androidx)
        btn_androidx.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/androidx/androidx/blob/androidx-main/LICENSE.txt"))
            startActivity(intent)
        }

        val btn_gson: Button = findViewById(R.id.gson)
        btn_gson.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/google/gson/blob/master/LICENSE"))
            startActivity(intent)
        }

        val btn_junit: Button = findViewById(R.id.Junit)
        btn_junit.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/junit-team/junit4/blob/main/LICENSE-junit.txt"))
            startActivity(intent)
        }

        val btn_kotlin: Button = findViewById(R.id.kotlin)
        btn_kotlin.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/JetBrains/kotlin/blob/master/license/LICENSE.txt"))
            startActivity(intent)

        }

        val btn_android: Button = findViewById(R.id.android)
        btn_android.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.apache.org/licenses/LICENSE-2.0"))
            startActivity(intent)
        }
    }
}