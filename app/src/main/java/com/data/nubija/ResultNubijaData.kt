package com.data.nubija

import com.google.gson.annotations.SerializedName

data class nubija (
    @SerializedName("Emptycnt")  var Emptycnt: String,
    @SerializedName("Parkcnt") var Parkcnt: String,
    @SerializedName("Vno")    var Vno: String

        )
