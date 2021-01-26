package com.underbar.nubijaapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.data.nubija.BikeStation
import com.data.nubija.BikeStationResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.underbar.nubijaapp.R.id.menu_bike
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
                        GoogleApiClient.OnConnectionFailedListener{

    //위치정보 멤버 변수 선언
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private var initLocation: Location? = null

    lateinit var providerClient: FusedLocationProviderClient
    lateinit var googleApiClient: GoogleApiClient

    //하단 내비게이션 바 인덱스
    private var bottomNavigationIndex: Int? = 1

    //누비자 정류장 정보
    private lateinit var bikeStationResult: BikeStationResult

    //누비자 마커 리스트
    private var nubijaMarkerMap = mutableMapOf<Int, Marker>()
    private val infoWindow = InfoWindow()

    // 뒤로가기 버튼 시간 측정 을 위해 선언된 변수
    private var mBackWait:Long = 0

    companion object    {
        private const val LOCATION_PERMISSION_CODE = 1000
    }




    //메모리에 올라갔을때
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //레이아웃과 연결
        setContentView(R.layout.activity_main)


        //FindViewById 로 Id값을 불러온후
        //Id값의 옵션에 접근
        val bottom_nav : BottomNavigationView = findViewById(R.id.bottom_nav)
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
                FusedLocationSource(this, LOCATION_PERMISSION_CODE)

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleConfirmText("원활한 사용을 위해 위치 권한이 필요합니다")
                .setDeniedMessage("위치 정보 이용 거절됨")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .check()

        googleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()

        providerClient = LocationServices.getFusedLocationProviderClient(this)
        googleApiClient.connect()


    }

    private val permissionListener = object : PermissionListener {
        override fun onPermissionGranted() {

        }

        override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
            Toast.makeText(this@MainActivity, "위치 권한 거부됨", Toast.LENGTH_SHORT).show()
            naverMap.locationTrackingMode = LocationTrackingMode.None
        }
    }


    /**
     * 위치 정보 제공자가 사용 가능 상태가 되었을때 호출
     */
    override fun onConnected(bundle: Bundle?) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        providerClient.lastLocation.addOnSuccessListener {
            it?.let {
                val locationOverlay = naverMap.locationOverlay
                locationOverlay.position = LatLng(it.latitude, it.longitude)

                val cameraUpdate = CameraUpdate.scrollTo(LatLng(it.latitude, it.longitude))
                naverMap.moveCamera(cameraUpdate)

            }
        }
    }

    /**
     * 함수와 사용 불가능 상태가 되었을 때 호출
     */
    override fun onConnectionSuspended(p0: Int) {
        naverMap.locationTrackingMode = LocationTrackingMode.None
        Toast.makeText(this, "위치 정보를 얻는데 실패 했습니다", Toast.LENGTH_LONG).show()
    }

    /**
     * 위치 정보 제공자를 얻지 못할때 호출
     */
    override fun onConnectionFailed(p0: ConnectionResult) {
        naverMap.locationTrackingMode = LocationTrackingMode.None
        Toast.makeText(this, "위치 정보를 얻는데 실패 했습니다", Toast.LENGTH_LONG).show()
    }




    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {


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

                // 자전거 지도로 지도 옵션 변경
                naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BICYCLE, true)
                naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRAFFIC, false)
                naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, false)

                bottomNavigationIndex = 1
                visualMarker()

            }

            R.id.menu_bus -> {

                //교통량 지도로 지도 옵션 변경
                naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BICYCLE, false)
                naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRAFFIC, true)
                naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true)
                bottomNavigationIndex = 2

                Toast.makeText(this, "다음 업데이트를 기다려 주세요~", Toast.LENGTH_SHORT).show()

                // 마커 삭제
                clearMarker()

            }

            R.id.menu_menu1 -> {

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

            mBackWait = System.currentTimeMillis()
            Toast.makeText(this, "종료하시려면 뒤로가기 버튼을 한번 더 눌러주세요.", Toast.LENGTH_SHORT).show()
        }

        else{
            finish()
        }
    }

    // mapFragment.getMapAsync(this) 에 의해 호출됨
    override fun onMapReady(naverMap: NaverMap) {

        this.naverMap = naverMap
        //지도 범위 제한
        naverMap.extent = LatLngBounds(LatLng(31.43, 122.37), LatLng(44.35, 132.0))

        //지도 옵션 지정 - 자전거 지도
        naverMap.mapType = NaverMap.MapType.Basic
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BICYCLE, true)

        //지도 UI 세팅
        val uiSettings = naverMap.uiSettings
        uiSettings.isLocationButtonEnabled = true
        uiSettings.isZoomControlEnabled = false

        //지도 위치 표시
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        if (initLocation != null)   {
            val cameraUpdate = CameraUpdate.scrollTo(LatLng(initLocation!!.latitude, initLocation!!.longitude))
            naverMap.moveCamera(cameraUpdate)
        }




        //지도 오버레이 활성화
        val locationOverlay = naverMap.locationOverlay                                              // 오버레이 객체 선언
        locationOverlay.isVisible = true                                                            // 오버레이 활성화


        //InfoWindow 내용구성 함수 실행
        infoWindowSetting()
        fetchBikeStation()

    }

    // 누비자 스테이션 정보 추출 및 마커 생성
    private fun fetchBikeStation()  {
        val bikeStationlists = ArrayList<BikeStation>()

        //서버와 통신 
        if (false) {
            val retrofit = Retrofit.Builder()
                .baseUrl(" http://api.nubija.com:1577/ubike/nubijaInfoApi.do?apikey=aMEEZeshtbWikWmkRmXD")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        }

        else {
            // 서버와 통신 실패시
            val assetManager:AssetManager = resources.assets
            val inputStream = assetManager.open("nubijaData.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }


            val jObject = JSONObject(jsonString)
            val jArray = jObject.getJSONArray("TerminalInfo")

            for (i in 0 until jArray.length()) {

                val obj = jArray.getJSONObject(i)
                val name = obj.getString("Tmname")
                val lats = obj.getString("Latitude")
                val lngs = obj.getString("Longitude")
                val vno = obj.getString("Vno")

                bikeStationlists.add(BikeStation(name, lats.toDouble(), lngs.toDouble(), vno.toInt(),0,0))

            }
        }

        bikeStationResult = BikeStationResult(bikeStationlists)
        updateMapMarker(bikeStationResult)



    }

    // 마커 생성 메소드 - fetchBikeStation 에 의해 호출됨
    private fun updateMapMarker(result: BikeStationResult){


         if (result.stations.isNotEmpty()){
            resetNubijaMarkerList()

             // 반복문으로 마커 생성
             for (bikestations in result.stations)  {

                 val marker = Marker()

                 marker.position = LatLng(bikestations.lat, bikestations.lng)
                 marker.icon = OverlayImage.fromResource(R.drawable.ic_bike_green)
                 marker.map = naverMap

                 // 마커 테그로 고유의 ID 부여
                 marker.tag = bikestations.tmid
                 marker.onClickListener = listener
                 nubijaMarkerMap.put(bikestations.tmid , marker)

             }

        }

    }


    // 마커가 클릭되면 호출 됨
    private val listener = Overlay.OnClickListener {overlay ->

        // 진동 효과
        val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(50)

        // 마커 초기화
        for (marker in nubijaMarkerMap.values) {
            marker.icon = OverlayImage.fromResource(R.drawable.ic_bike_green)
        }

        val marker = overlay as Marker

        //Info Window 가 존재 하면 닫고, 존재 하지 않으면 열기
        if (marker.hasInfoWindow()) {
            marker.icon = OverlayImage.fromResource(R.drawable.ic_bike_green)
            infoWindow.close()
        }
        else {
            marker.icon = OverlayImage.fromResource(R.drawable.ic_bike_blue)
            infoWindow.open(marker)
        }

        true

    }



    private fun resetNubijaMarkerList(){
        if (nubijaMarkerMap.isEmpty() ) {
            for (marker in nubijaMarkerMap.values) {
                marker.icon = OverlayImage.fromResource(R.drawable.ic_bike_green)
                marker.map = null
            }
        }
    }


    // 지도에서 마커 없앨때 사용
    private fun clearMarker() {
        for (marker in nubijaMarkerMap.values) {
            marker.icon = OverlayImage.fromResource(R.drawable.ic_bike_green)
            marker.map = null
        }
    }


    // 마커 재생성 때 사용
    private fun visualMarker() {
        if (nubijaMarkerMap.isEmpty()) {
            fetchBikeStation()
        }

        else {
            for (marker in nubijaMarkerMap.values) {
                marker.map = naverMap
            }
        }
    }

    //InfoWindow 내용 구성
    private fun infoWindowSetting() {
        // InfoWindow 설정

        naverMap.setOnMapClickListener { pointF, latLng ->

            if (infoWindow.isAdded) {
                val marker = infoWindow.marker!!
                marker.icon = OverlayImage.fromResource(R.drawable.ic_bike_green)

                // 진동 효과
                val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(50)
                
                infoWindow.close()
            }
        }
        infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(applicationContext) {
            override fun getText(infoWindow: InfoWindow): CharSequence {
               val tag = infoWindow.marker?.tag.toString()
                val result = bikeStationResult.stations

                for (station in result) {
                    if (tag.toInt() == station.tmid) {
                        return "${station.name}\n반납가능:${station.empty}\n대여가능:${station.park}"
                    }
                }
                return "load fail"
            }

        }

    }



}
