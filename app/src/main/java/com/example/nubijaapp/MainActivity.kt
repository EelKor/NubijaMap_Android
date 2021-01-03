package com.example.nubijaapp

import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.Toast
import com.example.nubijaapp.R.id.info_window
import com.example.nubijaapp.R.id.menu_bike
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import org.json.JSONObject


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    //위치정보 멤버 변수 선언
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    //하단 내비게이션 바 인덱스
    private var bottomNavigationIndex: Int? = 1

    //누비자 정류장 정보
    private lateinit var bikeStationResult: BikeStationResult

    //누비자 마커 리스트
    private var nubijaMarkerList = ArrayList<Marker>()
    private var nubijaMarkerClicked:Boolean = false

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

        val infoWindow:LinearLayout = findViewById(info_window)
        infoWindow.visibility = View.GONE

        //FindViewById 로 Id값을 불러온후
        //Id값의 옵션에 접근
        val bottom_nav : com.google.android.material.bottomnavigation.BottomNavigationView = findViewById(R.id.bottom_nav)
        bottom_nav.setOnNavigationItemSelectedListener(onBottomNavItemSelectedListener)



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
        when(it.itemId){
            menu_bike -> {
                Log.d(TAG, "MainActivity - 자전거 클릭")

                bottomNavigationIndex = 1

                if (nubijaMarkerList != null) {
                    for (marker in nubijaMarkerList) {
                        marker.map = naverMap
                    }
                }

                else {
                    fetchBikeStation()
                }

            }

            R.id.menu_bus -> {
                Log.d(TAG, "MainActivity - 버스 클릭")

                val infoWindow:LinearLayout = findViewById(info_window)
                val params = infoWindow.layoutParams
                val anim = TranslateAnimation(0f,0f,0f,params.height.toFloat() * -1)
                anim.duration = 500
                anim.fillAfter = true
                infoWindow.animation = anim
                infoWindow.visibility = View.GONE

                bottomNavigationIndex = 2
                resetNubijaMarkerList()

            }

            R.id.menu_menu1 -> {
                Log.d(TAG, "MainActivity - 메뉴1클릭")

                bottomNavigationIndex = 3
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
        val locationOverlay = naverMap.locationOverlay                               // 오버레이 객체 선언
        locationOverlay.isVisible = true                                                            // 오버레이 활성화

        this.naverMap = naverMap
        naverMap.locationSource = locationSource

        locationOverlay.position =  LatLng(35.22773370309257, 128.6821961402893)
        fetchBikeStation()


        }

    // 누비자 스테이션 정보 추출 및 마커 생성
    private fun fetchBikeStation()  {
        val bikeStationlists = ArrayList<BikeStation>()

        Log.d(TAG, "MainActivity - fetchBikeStation() called")

        val assetManager:AssetManager = resources.assets
        val inputStream = assetManager.open("nubija.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }


        val jObject = JSONObject(jsonString)
        val jArray = jObject.getJSONArray("TerminalInfo")

        for (i in 0 until jArray.length()) {

            val obj = jArray.getJSONObject(i)
            val name = obj.getString("Tmname")
            val lats = obj.getString("Latitude")
            val lngs = obj.getString("Longitude")
            val vno = obj.getString("Vno")

            bikeStationlists.add(BikeStation(name, lats.toDouble(), lngs.toDouble(), vno.toInt()))
        }
        bikeStationResult = BikeStationResult(bikeStationlists)
        updateMapMarker(bikeStationResult)



    }

    // 마커 생성 메소드 - fetchBikeStation 에 의해 호출됨
    private fun updateMapMarker(result: BikeStationResult){

        Log.d(TAG, "MainAcitivity - updateMapMarker() called")
        val infoWindow:LinearLayout = findViewById(info_window)

         if (result.stations.isNotEmpty()){


            Log.d(TAG, "updateMapMarker() - If gate passed")

             for (bikestations in result.stations)  {
                 val marker = Marker()
                 marker.position = LatLng(bikestations.lat, bikestations.lng)
                 marker.icon = MarkerIcons.GREEN
                 marker.map = naverMap

                 // 마커가 클릭됬을때 마커 색깔 변경
                 marker.setOnClickListener {
                     if (nubijaMarkerClicked) {
                         for (marker in nubijaMarkerList) {
                             marker.icon = MarkerIcons.GREEN
                         }

                         marker.icon = MarkerIcons.YELLOW


                         val params = infoWindow.layoutParams
                         val anim = TranslateAnimation(0f,0f,params.height.toFloat() * -1,0f)
                         anim.duration = 500
                         anim.fillAfter = false
                         infoWindow.animation = anim
                         infoWindow.visibility = View.VISIBLE

                         //진동 효과
                         val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                         vibrator.vibrate(50)
                     }

                     else {
                         Log.d(TAG,"nubijaMarkerClicked =  $nubijaMarkerClicked")
                         marker.icon = MarkerIcons.YELLOW

                         //진동 효과
                         val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                         vibrator.vibrate(50)
                         nubijaMarkerClicked = true
                     }


                     false
                 }

                 nubijaMarkerList.add(marker)
             }
        }

    }

    private fun resetNubijaMarkerList(){
        if (nubijaMarkerList != null && nubijaMarkerList.size > 0) {
            for (marker in nubijaMarkerList) {
                marker.icon = MarkerIcons.GREEN
                marker.map = null
            }
        }
    }



}

