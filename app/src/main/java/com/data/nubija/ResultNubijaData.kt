package com.data.nubija

import com.google.gson.annotations.SerializedName

data class nubija (
    @SerializedName("Emptycnt")  var Emptycnt: String,
    @SerializedName("Parkcnt") var Parkcnt: String,
    @SerializedName("Vno")    var Vno: String,
    @SerializedName("Lat") var Lat: String,
    @SerializedName("Lng") var Lng: String,
    @SerializedName("Tmname") var Tmname: String

        )
