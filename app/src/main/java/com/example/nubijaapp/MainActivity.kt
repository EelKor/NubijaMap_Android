package com.example.nubijaapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    //멤버 변수 선언
    private lateinit var bikeFragment: BikeFragment
    private lateinit var busFragment: BusFragment
    private lateinit var menuFragment: MenuFragment


    // 뒤로가기 버튼 시간 측정 을 위해 선언된 변수
    private var mBackWait:Long = 0


    //로그 남기기 위해 만듦
        companion object {
            const val TAG: String = "로그"
        }


    //메모리에 올라갔을때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //레이아웃과 연결
        setContentView(R.layout.activity_main)
        Log.d(TAG, "MainActivity - OnCreate() called")

        //FindViewById 로 Id값을 불러온후
        //Id값의 옵션에 접근
        val bottom_nav : com.google.android.material.bottomnavigation.BottomNavigationView = findViewById(R.id.bottom_nav)
        bottom_nav.setOnNavigationItemSelectedListener(onBottomNavItemSelectedListener)
        bikeFragment = BikeFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.fragment_frame, bikeFragment).commit()
    }



    //바텀 내비게이션 아이템이 눌러졌을때
    private val onBottomNavItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener {
        //스위치 문
        // 하단 내비게이션 바 버튼이 클릭 됬을때 실행할 동작
        when(it.itemId){
            R.id.menu_bike -> {
                Log.d(TAG, "MainActivity - 자전거 클릭")
                bikeFragment = BikeFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_frame, bikeFragment).commit()
            }

            R.id.menu_bus -> {
                Log.d(TAG, "MainActivity - 버스 클릭")
                busFragment = BusFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_frame, busFragment).commit()

            }

            R.id.menu_menu1 -> {
                Log.d(TAG, "MainActivity - 메뉴1클릭")
                menuFragment = MenuFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_frame, menuFragment).commit()
            }
        }
        true
    }



    // 뒤로가기 버튼 이 눌러졌을때
    override fun onBackPressed() {
        if (System.currentTimeMillis() - mBackWait >= 2000){
            mBackWait = System.currentTimeMillis()
            Toast.makeText(this, "종료하시려면 뒤로가기 버튼을 한번 더 눌러주세요.", Toast.LENGTH_SHORT).show()
        }

        else{
            finish()
        }
    }


}