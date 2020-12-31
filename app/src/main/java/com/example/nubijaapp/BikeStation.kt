package com.example.nubijaapp

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject


class BikeStation: AppCompatActivity()  {
    var names = ArrayList<String>()
    var lats = ArrayList<Double>()
    var lngs = ArrayList<Double>()



    private fun parseData() {

        // Asset 디렉토리 불러오기 위한 준비
        val assetManager = resources.assets
        val inputStream = assetManager.open("nubija.json")                                 // nubija.json 파일 열기
        val jsonString = inputStream.bufferedReader().use { it.readLine() }

        val jObject = JSONObject(jsonString)                                                        //JSON 파일에 대한 객체 생성
        val jArray = jObject.getJSONArray("Terminalinfo")                                    // 객체 안에 TerminalInfo 라는 부분 가져오기



        // JSON 을 클래스 변수 리스트에 하나씩 저장
        for (i in 0 until jArray.length())  {

            val obj = jArray.getJSONObject(i)
            val name = obj.getString("Tmname")
            val lats = obj.getDouble("latitude")
            val lng = obj.getDouble("longitude")

            this.names.add(name)
            this.lats.add(lats)
            this.lngs.add(lng)
        }



    }


}