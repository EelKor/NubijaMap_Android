package com.example.nubijaapp

data class BikeStation(
        var name: String, var lat: Double, var lng: Double, var tmid: Int
)

data class BikeStationInfo(
        var Vno:Int, var Emptycnt:Int, var Parkcnt:Int
)