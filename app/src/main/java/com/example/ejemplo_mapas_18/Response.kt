package com.example.ejemplo_mapas_18

import com.google.android.gms.maps.model.LatLng

class Response{
    var routes:ArrayList<Routes>? = null
}

class Routes1{
    var legs:ArrayList<Legs>? = null
}

class Legs1{
    var steps:ArrayList<Steps>? = null
}

class Steps1{
    var end_location:LatLon? = null
    var start_location:LatLon? = null
}

class LatLon{
    var lat:Double = 0.0
    var lng:Double = 0.0

    fun toLatLng():LatLng{
        return LatLng(lat,lng)
    }

}

class Polyline{
    var points:String = ""
}