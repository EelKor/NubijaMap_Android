package com.underbar.nubijaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashActivity : AppCompatActivity() {

    val SPLASH_TIME:Long = 1500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 화면에 컨텐트 표시 ( 매우 중요 ) 이 코드 없으면 화면에 아무것도 안뜸
        setContentView(R.layout.activity_splash)


        // 2 초간 홀딩 후 메인 엑티비티로 이동
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, SPLASH_TIME)


    }
}