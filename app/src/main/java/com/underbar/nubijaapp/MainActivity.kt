 package com.underbar.nubijaapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.api.NubijaAPI
import com.data.nubija.BikeStation
import com.data.nubija.BikeStationResult
import com.data.nubija.nubija
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


 class MainActivity : AppCompatActivity(), OnMapReadyCallback    {

    //위치정보 멤버 변수 선언
    private lateinit var locationSource: FusedLocationSource
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var naverMap: NaverMap

    //누비자 정류장 정보
    private lateinit var bikeStationResult: BikeStationResult

    //누비자 마커 리스트
    private var nubijaMarkerMap = mutableMapOf<Int, Marker>()
    private val infoWindow = InfoWindow()

    // 뒤로가기 버튼 시간 측정 을 위해 선언된 변수
    // 시간측정을 토스트 메시지 겹침을 방지하기위한 시간 측정 함수
    private var mBackWait:Long = 0

    // 현재위치와 마커 사이의 거리, 현위치와 가까운 Top3 마커
     // findNearestStation() 에서 사용됨
    private val distances = mutableMapOf<Int, Double>()
    private val nearestMarkers = mutableMapOf<Int, Int>()
    private var botNavMenuBusCallCount = 0


    companion object    {
        private const val LOCATION_PERMISSION_CODE = 1000
        private const val NUBIJA_API_SERVER_URL = "http://api.lessnas.me"

        // 마커 색깔 결정
        private const val MAX_RED_BIKE_INDEX = 5
        private const val MIN_RED_BIKE_INDEX = 1

        private const val MAX_YELLOW_BIKE_INDEX = 10
        private const val MIN_YELLOW_BIKE_INDEX = 5

        private const val MAX_GREEN_BIKE_INDEX = 100
        private const val MIN_GREEN_BIKE_INDEX = 10

        // 마커 이미지 결정
        private val redMarkerOverlayImage = OverlayImage.fromResource(R.drawable.ic_bike_red)
        private val redMarkerOverlayImageClicked = OverlayImage.fromResource(R.drawable.ic_bike_red_clicked)

        private val yellowMarkerOverlayImage = OverlayImage.fromResource(R.drawable.ic_bike_yellow)
        private val yellowMarkerOverlayImageClicked = OverlayImage.fromResource(R.drawable.ic_bike_yellow_clicked)

        private val greenMarkerOverlayImage = OverlayImage.fromResource(R.drawable.ic_bike_green)
        private val greenMarkerOverlayImageClicked = OverlayImage.fromResource(R.drawable.ic_bike_green_clicked)

        private val blueMarkerOverlayImage = OverlayImage.fromResource(R.drawable.ic_bike_blue)
        private val blueMarkerOverlayImageClicked = OverlayImage.fromResource(R.drawable.ic_bike_blue_clicked)

        private val grayMarkerOverlayImage = OverlayImage.fromResource(R.drawable.ic_bike_gray)
        private val grayMarkerOverlayImageClicked = OverlayImage.fromResource(R.drawable.ic_bike_gray_clicked)


    }




    //메모리에 올라갔을때
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

        //위치 서비스 클라이언트 생성 - FusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleConfirmText("원활한 사용을 위해 위치 권한이 필요합니다")
                .setDeniedMessage("위치 정보 이용 거절됨")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .check()



    }

    private val permissionListener = object : PermissionListener {
        override fun onPermissionGranted() {

            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // 권한은 허용 되었으나 위치 설정이 꺼저있을때 위치 설정 페이지로 이동
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))  {

                // 팝업 메시지 구현
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("앗...!")
                builder.setMessage("위치 설정이 꺼져 있어요ㅠ\n원활한 사용을 위해 위치 설정을 켜주세요")
                builder.setPositiveButton(
                        "설정"
                ) { dialog, which ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    startActivity(intent)
                }
                builder.setNegativeButton(
                        "아니오"
                ) { dialog, which ->
                    Toast.makeText(this@MainActivity, "서비스 이용을 위해 위치 설정을 켜주세요", Toast.LENGTH_LONG).show()

                    //네이버 지도 UI 세팅
                    val uiSettings = naverMap.uiSettings
                    uiSettings.isLocationButtonEnabled = false
                    uiSettings.isZoomControlEnabled = false

                    //지도 오버레이 활성화
                    val locationOverlay = naverMap.locationOverlay                                              // 오버레이 객체 선언
                    locationOverlay.isVisible = false                                                            // 오버레이 활성화
                }

                builder.show()

            }


        }

        override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {

            // 구글 정책상 최소 사용
            infoWindowSetting()
            fetchBikeStation()

        }
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


                //네이버 지도 UI 세팅
                val uiSettings = naverMap.uiSettings
                uiSettings.isLocationButtonEnabled = true
                uiSettings.isZoomControlEnabled = false

                // 마커 새로고침
                visualMarker()

                // 인포 윈도우 열여 있다면, 닫기
                if (infoWindow.isAdded) {
                    infoWindow.close()
                }

            }

            R.id.menu_bus -> {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)   {
                    Toast.makeText(this, "주변 정류장 찾기 기능을 이용하기 위해 위치 권한을 허용해 주세요 :)", Toast.LENGTH_SHORT).show()
                }

                else    {
                    if (nubijaMarkerMap.isNotEmpty())   {

                        // 최단직선거리 정류장 찾기
                        findNearestStation()
                        botNavMenuBusCallCount += 1
                    }
                }
            }

            R.id.menu_menu1 -> {

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
        naverMap.extent = LatLngBounds(LatLng(35.092098, 128.453699), LatLng(35.394740, 129.044587))
        naverMap.minZoom = 9.0
        naverMap.maxZoom = 18.0

        //지도 옵션 지정 - 자전거 지도
        naverMap.mapType = NaverMap.MapType.Basic
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BICYCLE, true)

        //지도 UI 세팅
        val uiSettings = naverMap.uiSettings
        uiSettings.isLocationButtonEnabled = true
        uiSettings.isZoomControlEnabled = false

        //InfoWindow 내용구성 함수 실행
        infoWindowSetting()
        fetchBikeStation()

        //지도 위치 표시
        naverMap.locationSource = locationSource

        // FusedLocationClient 마지막 위치 불러오기
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
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null)   {
                val cameraUpdate = CameraUpdate.scrollTo(LatLng(location.latitude, location.longitude))
                naverMap.moveCamera(cameraUpdate)

                //지도 오버레이 활성화
                val locationOverlay = naverMap.locationOverlay
                locationOverlay.position = LatLng(location.latitude, location.longitude)
                locationOverlay.isVisible = true

                naverMap.locationTrackingMode = LocationTrackingMode.Follow


            }

        }

    }

    // 누비자 스테이션 정보 추출 및 마커 생성
    private fun fetchBikeStation()  {
        val bikeStationlists = ArrayList<BikeStation>()

        val retrofit = Retrofit.Builder()
            .baseUrl(NUBIJA_API_SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(NubijaAPI::class.java)
        val callGetNubijaData = api.getNubijaData()
        callGetNubijaData.enqueue(object : Callback<List<nubija>> {
            override fun onResponse(
                    call: Call<List<nubija>>,
                    response: Response<List<nubija>>
            ) {
                    if (response.body() != null) {

                        Toast.makeText(this@MainActivity, "실시간 터미널 현황은 실제와 차이가 있을수 있습니다", Toast.LENGTH_SHORT).show()
                        val rawdata: List<nubija> = response.body()!!

                        // 좌표값 불러오기
                        val assetManager: AssetManager = resources.assets
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

                            if (rawdata[i].Vno == vno.toString()) {
                                val empty: String = rawdata[i].Emptycnt
                                val park: String = rawdata[i].Parkcnt

                                bikeStationlists.add(BikeStation(name, lats.toDouble(), lngs.toDouble(), vno.toInt(), empty, park))

                            } else {
                                bikeStationlists.add(BikeStation(name, lats.toDouble(), lngs.toDouble(), vno.toInt(), "null", "null"))
                            }

                        }

                        bikeStationResult = BikeStationResult(bikeStationlists)
                        updateMapMarker(bikeStationResult)

                    }
                else    {
                        Toast.makeText(this@MainActivity, "서버로 부터 데이터를 받아오는데 실패 했습니다", Toast.LENGTH_LONG).show()
                        // 서버와 통신 실패시
                        val assetManager: AssetManager = resources.assets
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

                            bikeStationlists.add(BikeStation(name, lats.toDouble(), lngs.toDouble(), vno.toInt(), "null", "null"))

                        }
                        bikeStationResult = BikeStationResult(bikeStationlists)
                        updateMapMarker(bikeStationResult)

                }


            }

            override fun onFailure(call: Call<List<nubija>>, t: Throwable) {

                // 팝업 메시지 구현
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("앗...!")
                builder.setMessage("인터넷 연결 상태를 확인해 주세요")
                builder.setPositiveButton(
                        "설정"
                ) { dialog, which ->
                    val intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    startActivity(intent)
                }
                builder.setNegativeButton(
                        "아니오"
                ) { dialog, which ->
                    Toast.makeText(this@MainActivity, "서비스 이용을 위해 인터넷을 켜주세요", Toast.LENGTH_LONG).show()

                    //네이버 지도 UI 세팅
                    val uiSettings = naverMap.uiSettings
                    uiSettings.isLocationButtonEnabled = false
                    uiSettings.isZoomControlEnabled = false

                    //지도 오버레이 활성화
                    val locationOverlay = naverMap.locationOverlay                                              // 오버레이 객체 선언
                    locationOverlay.isVisible = false                                                            // 오버레이 활성화
                }

                builder.show()

                // 서버와 통신 실패시
                val assetManager: AssetManager = resources.assets
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

                    bikeStationlists.add(BikeStation(name, lats.toDouble(), lngs.toDouble(), vno.toInt(), "null", "null"))

                }
                bikeStationResult = BikeStationResult(bikeStationlists)
                updateMapMarker(bikeStationResult)

            }
        })

    }

    // 마커 생성 메소드 - fetchBikeStation 에 의해 호출됨
    private fun updateMapMarker(result: BikeStationResult){


         if (result.stations.isNotEmpty()){
            resetNubijaMarkerList()

             // 반복문으로 마커 생성
             for (bikestations in result.stations)  {

                 val marker = Marker()

                 // 대여 가능 자전거 댓수 기준으로 마커 색깔 결정
                 // 서버와 통신이 성공적 이면
                 if (bikestations.park != "null")  {

                     // 터미널이 가득 찬 경우 파란색 마커 표시
                     if (bikestations.empty.toInt() == 0)    {
                         marker.icon = blueMarkerOverlayImage
                     }

                     else   {
                         when(bikestations.park.toInt())   {
                             in MIN_GREEN_BIKE_INDEX until MAX_GREEN_BIKE_INDEX -> marker.icon = greenMarkerOverlayImage
                             in MIN_YELLOW_BIKE_INDEX until MAX_YELLOW_BIKE_INDEX  ->  marker.icon = yellowMarkerOverlayImage
                             in MIN_RED_BIKE_INDEX until  MAX_RED_BIKE_INDEX  ->  marker.icon = redMarkerOverlayImage
                             else           -> marker.icon = grayMarkerOverlayImage
                         }
                     }

                 }

                 // 서버와 통신 문제로 주차 가능 댓수가 null 이면
                 else   {
                     marker.icon = grayMarkerOverlayImage
                 }


                 marker.position = LatLng(bikestations.lat, bikestations.lng)
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
        // val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        //vibrator.vibrate(50)

        // 마커 초기화
        for (marker in nubijaMarkerMap.values) {

            // 마커 태그 INT 형으로 저장
            val vno = marker.tag.toString().toInt()

            // bikeStationResult 안 마커 태그가 일치하는 BikeStaion 객체 불러옴
            for (i in bikeStationResult.stations.indices)   {
                if (vno == bikeStationResult.stations[i].tmid)  {

                    // 서버로 부터 성공적으로 Parkcnt 받았을떄
                    if (bikeStationResult.stations[i].park != "null")   {

                        // 터미널이 가득 찬 경우 파란색 마커 표시
                        if (bikeStationResult.stations[i].empty.toInt() == 0)    {
                            marker.icon = blueMarkerOverlayImage
                        }

                        else   {
                            when(bikeStationResult.stations[i].park.toInt())   {
                                in MIN_GREEN_BIKE_INDEX until MAX_GREEN_BIKE_INDEX -> marker.icon = greenMarkerOverlayImage
                                in MIN_YELLOW_BIKE_INDEX until MAX_YELLOW_BIKE_INDEX  ->  marker.icon = yellowMarkerOverlayImage
                                in MIN_RED_BIKE_INDEX until  MAX_RED_BIKE_INDEX  ->  marker.icon = redMarkerOverlayImage
                                else           -> marker.icon = grayMarkerOverlayImage
                            }
                        }
                    }

                    // 통신에 실패해 Parkcnt 가 null 일때
                    else    {
                        marker.icon = grayMarkerOverlayImage
                    }
                }
            }

        }

        val marker = overlay as Marker

        //Info Window 가 존재 하면 닫고, 존재 하지 않으면 열기
        if (marker.hasInfoWindow()) {
            val vno = marker.tag.toString().toInt()

            // bikeStationResult 안 마커 태그가 일치하는 BikeStaion 객체 불러옴
            for (i in bikeStationResult.stations.indices)   {

                if (vno == bikeStationResult.stations[i].tmid)  {

                    // 서버로 부터 성공적으로 Parkcnt 받았을떄
                    if (bikeStationResult.stations[i].park != "null") {

                        // 터미널이 가득 찬 경우 파란색 마커 표시
                        if (bikeStationResult.stations[i].empty.toInt() == 0) {
                            marker.icon = blueMarkerOverlayImage
                        } else {
                            when (bikeStationResult.stations[i].park.toInt()) {
                                in MIN_GREEN_BIKE_INDEX until MAX_GREEN_BIKE_INDEX -> marker.icon =
                                    greenMarkerOverlayImage

                                in MIN_YELLOW_BIKE_INDEX until MAX_YELLOW_BIKE_INDEX -> marker.icon =
                                    yellowMarkerOverlayImage

                                in MIN_RED_BIKE_INDEX until MAX_RED_BIKE_INDEX -> marker.icon =
                                    redMarkerOverlayImage

                                else -> marker.icon =
                                    grayMarkerOverlayImage

                            }
                        }
                    }
                    // 통신에 실패해 Parkcnt 가 null 일때
                    else    {
                        marker.icon = grayMarkerOverlayImage
                    }
                }

            }
            infoWindow.close()
        }
        else {
            val vno = marker.tag.toString().toInt()

            // bikeStationResult 안 마커 태그가 일치하는 BikeStaion 객체 불러옴
            for (i in bikeStationResult.stations.indices)   {
                if (vno == bikeStationResult.stations[i].tmid)  {

                    // 해당 마커를 애니매이션 효과와 함께 가운데 정렬
                    val location = LatLng(bikeStationResult.stations[i].lat, bikeStationResult.stations[i].lng)
                    val cameraUpdate = CameraUpdate.scrollTo(location)
                            .animate(CameraAnimation.Easing)
                    naverMap.moveCamera(cameraUpdate)


                    // 서버로 부터 성공적으로 Parkcnt 받았을떄
                    if (bikeStationResult.stations[i].park != "null")   {
                        // 터미널이 가득 찬 경우 파란색 마커 표시
                        if (bikeStationResult.stations[i].empty.toInt() == 0)    {
                            marker.icon = blueMarkerOverlayImageClicked
                        }

                        else {
                            when (bikeStationResult.stations[i].park.toInt()) {
                                in MIN_GREEN_BIKE_INDEX until MAX_GREEN_BIKE_INDEX -> marker.icon =
                                    greenMarkerOverlayImageClicked

                                in MIN_YELLOW_BIKE_INDEX until MAX_YELLOW_BIKE_INDEX -> marker.icon =
                                    yellowMarkerOverlayImageClicked

                                in MIN_RED_BIKE_INDEX until MAX_RED_BIKE_INDEX -> marker.icon =
                                    redMarkerOverlayImageClicked

                                else -> marker.icon =
                                    grayMarkerOverlayImageClicked

                            }
                        }
                    }

                    // 통신에 실패해 Parkcnt 가 null 일때
                    else    {
                        marker.icon = grayMarkerOverlayImageClicked
                    }
                }
            }
            infoWindow.open(marker)
        }

        true

    }



    private fun resetNubijaMarkerList(){
        if (nubijaMarkerMap.isEmpty() ) {
            for (marker in nubijaMarkerMap.values) {
                marker.map = null
            }
        }
    }


    // 바텀 네비게이션 클릭시 마커 재생성 때 사용
    private fun visualMarker() {

        // 순서 제어 변수 초기화 - findNearestStation() 에서 사용
        botNavMenuBusCallCount = 0

        // InfoWindow 가 있으면 끄기
        if (infoWindow.isAdded) {
            infoWindow.close()
        }

        // 누비자 맵 마커 데이터가 비어있으면 생성
        if (nubijaMarkerMap.isEmpty()) {
            fetchBikeStation()
        }

        else {
            for (marker in nubijaMarkerMap.values) {
                val vno = marker.tag.toString().toInt()

                // 마커 캡션 모두 지우기
                marker.captionText = ""

                // bikeStationResult 안 마커 태그가 일치하는 BikeStaion 객체 불러옴
                for (i in bikeStationResult.stations.indices)   {
                    if (vno == bikeStationResult.stations[i].tmid)  {

                        // 서버로 부터 성공적으로 Parkcnt 받았을떄
                        if (bikeStationResult.stations[i].park != "null")   {
                            // 터미널이 가득 찬 경우 파란색 마커 표시
                            if (bikeStationResult.stations[i].empty.toInt() == 0)    {
                                marker.icon = blueMarkerOverlayImage
                            }

                            else   {
                                when(bikeStationResult.stations[i].park.toInt())   {
                                    in MIN_GREEN_BIKE_INDEX until MAX_GREEN_BIKE_INDEX -> marker.icon = greenMarkerOverlayImage
                                    in MIN_YELLOW_BIKE_INDEX until MAX_YELLOW_BIKE_INDEX  ->  marker.icon = yellowMarkerOverlayImage
                                    in MIN_RED_BIKE_INDEX until  MAX_RED_BIKE_INDEX  ->  marker.icon = redMarkerOverlayImage
                                    else           -> marker.icon = grayMarkerOverlayImage
                                }
                            }
                        }

                        // 통신에 실패해 Parkcnt 가 null 일때
                        else    {
                            marker.icon = grayMarkerOverlayImage
                        }
                    }
                }
                marker.map = naverMap
            }
        }
    }

    //InfoWindow 내용 구성
    private fun infoWindowSetting() {
        // InfoWindow 설정

        // 지도 가 클릭 됬을때 -> InfoWindow 닫기
        naverMap.setOnMapClickListener { pointF, latLng ->

            if (infoWindow.isAdded) {

                visualMarker()
                infoWindow.close()
            }
        }

        // 지도가 스크롤 됬을때 -> InfoWindow 닫기
        naverMap.addOnCameraChangeListener { reason, animate ->
            if (reason == CameraUpdate.REASON_GESTURE)  {
                if (infoWindow.isAdded) {
                        visualMarker()
                        infoWindow.close()
                    }

                    // 진동 효과
                    // val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    // vibrator.vibrate(50)
            }
        }

        infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(applicationContext) {
            override fun getText(infoWindow: InfoWindow): CharSequence {
               val tag = infoWindow.marker?.tag.toString()
                val result = bikeStationResult.stations

                for (station in result) {
                    if (tag.toInt() == station.tmid) {
                        return "${station.name}\n반납가능: ${station.empty}\n대여가능: ${station.park}"
                    }
                }
                return "load fail"
            }

        }

    }


    //현재 위치에서 최단직선거리 정류장 찾기 메소드
    private fun findNearestStation() {

        // 최초 실행시( distances 가 비어 있을때 ) distances 초기화

            // 현재 위치 불러오기 LatLng 형식
            val currentLocation = naverMap.locationOverlay.position

            for (marker in bikeStationResult.stations)  {
                distances.put(marker.tmid, currentLocation.distanceTo(LatLng(marker.lat, marker.lng)))
            }

            for (index in 1..3)   {
                for (tag in distances.keys)   {

                    if (distances[tag] == distances.values.min()!!)    {
                        nearestMarkers.put(index, tag)
                        distances.remove(tag)
                        break
                    }


                }


            }

        if (botNavMenuBusCallCount > 2) {
            botNavMenuBusCallCount -= 3
        }

        val markerTag = nearestMarkers[botNavMenuBusCallCount+1]
        //찾은 최단거리 마커 인포윈도우 띄우기
        if (nubijaMarkerMap[markerTag] != null)  {
            val marker = nubijaMarkerMap[markerTag]!!

            // 마커에 캡션 추가 - 가까운 순으로
            when (botNavMenuBusCallCount+1) {
                1 -> marker.captionText = "1st"
                2 -> marker.captionText = "2nd"
                3 -> marker.captionText = "3rd"
                else -> marker.captionText = ""
            }

            // bikeStationResult 안 마커 태그가 일치하는 BikeStaion 객체 불러옴
            for (i in bikeStationResult.stations.indices)   {
                if (markerTag == bikeStationResult.stations[i].tmid)  {

                    // 서버로 부터 성공적으로 Parkcnt 받았을떄
                    if (bikeStationResult.stations[i].park != "null")   {
                        // 터미널이 가득 찬 경우 파란색 마커 표시
                        if (bikeStationResult.stations[i].empty.toInt() == 0)    {
                            marker.icon = blueMarkerOverlayImageClicked
                        }

                        else {
                            when (bikeStationResult.stations[i].park.toInt()) {
                                in MIN_GREEN_BIKE_INDEX until MAX_GREEN_BIKE_INDEX -> marker.icon =
                                        greenMarkerOverlayImageClicked

                                in MIN_YELLOW_BIKE_INDEX until MAX_YELLOW_BIKE_INDEX -> marker.icon =
                                        yellowMarkerOverlayImageClicked

                                in MIN_RED_BIKE_INDEX until MAX_RED_BIKE_INDEX -> marker.icon =
                                        redMarkerOverlayImageClicked

                                else -> marker.icon =
                                        grayMarkerOverlayImageClicked

                            }
                        }
                    }

                    // 통신에 실패해 Parkcnt 가 null 일때
                    else    {
                        marker.icon = grayMarkerOverlayImageClicked
                    }
                }
            }
            infoWindow.open(marker)
        }



    }



}
