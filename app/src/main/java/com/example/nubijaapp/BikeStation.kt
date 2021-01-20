package com.example.nubijaapp

data class BikeStation(
        var name: String, var lat: Double, var lng: Double, var tmid: Int
)

data class BikeStationRT(
        var vno:Int, var park:Int, var empty:Int
)
