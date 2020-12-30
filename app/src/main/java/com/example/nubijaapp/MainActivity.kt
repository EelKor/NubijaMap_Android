package com.example.nubijaapp

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    //프레그먼트 멤버 변수 선언
    private lateinit var bikeFragment: BikeFragment
    private lateinit var busFragment: BusFragment

    //위치정보 멤버 변수 선언
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    //네이버 지도 마커
    private var greenMarker = Marker()
    private var blueMarker = Marker()


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

        // (초기화면) bikeFragment.kt 최초로 불러오기
        bikeFragment = BikeFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.info_frame, bikeFragment).commit()

        // 현재 화면에 Fragment 추가
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
                ?: MapFragment.newInstance().also {
                    fm.beginTransaction().add(R.id.map, it).commit()
                }


        //NaverMap 객체 얻어오기
        //OnMapReady() 호출됨
        mapFragment.getMapAsync(this)

        //LOCATION_PERMISSION_CODE
        locationSource =
                FusedLocationSource(this, 1000)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        Log.d(TAG, "MainActivity : onRequestPermissionResult() called ")

        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults))  {
            if (locationSource.isActivated) {   //권한 거부됨
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }




    //바텀 내비게이션 아이템이 눌러졌을때
    private val onBottomNavItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener {
        //스위치 문
        // 하단 내비게이션 바 버튼이 클릭 됬을때 실행할 동작
        val marker = Marker()
        marker.map = null

        when(it.itemId){
            R.id.menu_bike -> {
                Log.d(TAG, "MainActivity - 자전거 클릭")
                bikeFragment = BikeFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.info_frame, bikeFragment).commit()

                
            }

            R.id.menu_bus -> {
                Log.d(TAG, "MainActivity - 버스 클릭")
                busFragment = BusFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.info_frame, busFragment).commit()


            }

            R.id.menu_menu1 -> {
                Log.d(TAG, "MainActivity - 메뉴1클릭")
                val menuIntent = Intent(this, MenuActivity::class.java)
                startActivity(menuIntent)
            }

        }
        true
    }


    // 뒤로가기 버튼 이 눌러졌을때
    override fun onBackPressed() {

        //뒤로가기 2번 누를때 종료 기능 구현
        if (System.currentTimeMillis() - mBackWait >= 2000){

            //디버깅을 위해 로그 삽입
            Log.d(TAG, "MainActivity - onBackPressed() called")
            mBackWait = System.currentTimeMillis()
            Toast.makeText(this, "종료하시려면 뒤로가기 버튼을 한번 더 눌러주세요.", Toast.LENGTH_SHORT).show()
        }

        else{
            Log.d(TAG,"MainActivity - 프로그램 종료")
            finish()
        }
    }

    // mapFragment.getMapAsync(this) 에 의해 호출됨
    override fun onMapReady(naverMap: NaverMap) {
        Log.d(TAG, "MainActivity : onMapReady() called")

        //지도 UI 세팅
        val uiSettings = naverMap.uiSettings
        uiSettings.isLocationButtonEnabled = true
        uiSettings.isZoomControlEnabled = false

        //지도 오버레이 활성화
        val locationOverlay = naverMap.locationOverlay
        locationOverlay.isVisible = true

        this.naverMap = naverMap
        naverMap.locationSource = locationSource

        locationOverlay.position = LatLng(35.22773370309257, 128.6821961402893)

        //자전거 마커 초기설정
        greenMarker.map = null
        greenMarker.icon = MarkerIcons.BLACK
        greenMarker.iconTintColor = Color.BLUE
        greenMarker.width = Marker.SIZE_AUTO
        greenMarker.height = Marker.SIZE_AUTO
        greenMarker.position = LatLng(35.22773370309257, 128.6821961402893)
        greenMarker.map = naverMap

        // 버스 마커 초기 설정
        blueMarker.map = null
        blueMarker.icon = MarkerIcons.BLACK
        blueMarker.iconTintColor = Color.BLUE
        blueMarker.width = Marker.SIZE_AUTO
        blueMarker.height = Marker.SIZE_AUTO
        blueMarker.position = LatLng(35.22773370309257, 128.6821961402893)
        

        }
    }

