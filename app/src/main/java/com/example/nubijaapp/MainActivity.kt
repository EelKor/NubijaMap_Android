package com.example.nubijaapp

import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ActionMenuView
import android.widget.Toast
import com.example.nubijaapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener{
    //멤버 변수 선언
    private lateinit var BikeFragment: BikeFragment
    private lateinit var BusFragment: BusFragment
    private lateinit var MenuFragment: MenuFragment


    // 뒤로가기 버튼 시간 측정 을 위해 선언된 변수
    var mBackWait:Long = 0

        companion object {
            const val TAG: String = "로그"
        }


    //메모리에 올라갔을때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //레이아웃과 연결
        setContentView(R.layout.activity_main)
        Log.d(TAG, "MainActivity - OnCreate() called")

        //뷰 바인딩으로 ActivityMain.xml 파일일 불러온 후
        //그 안에있는 id값 bottomNav 에 접근
        ActivityMainBinding.inflate(layoutInflater).bottomNav.setOnNavigationItemSelectedListener(this)
        BikeFragment = com.example.nubijaapp.BikeFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.fragment_frame, BikeFragment).commit()
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

    // 하단 내비게이션 바가 눌려졌을때
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d(TAG,"MainActivity - onNavigaionItemSelected() called")

        //스위치 문
        // 하단 내비게이션 바 버튼이 클릭 됬을때 실행할 동작
        when(item.itemId){
            R.id.menu_bike -> {
                Log.d(TAG, "MainActivity - 자전거 클릭")
                BikeFragment = com.example.nubijaapp.BikeFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_frame, BikeFragment).commit()
            }

            R.id.menu_bus -> {
                Log.d(TAG, "MainActivity - 버스 클릭")
                BusFragment = com.example.nubijaapp.BusFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_frame, BusFragment).commit()

            }

            R.id.menu_menu1 -> {
                Log.d(TAG, "MainActivity - 메뉴1클릭")
                MenuFragment = com.example.nubijaapp.MenuFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_frame, MenuFragment).commit()
            }
        }
        return true
    }
}