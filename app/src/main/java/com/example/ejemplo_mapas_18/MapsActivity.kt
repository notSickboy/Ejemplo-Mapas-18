package com.example.ejemplo_mapas_18

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.LatLngBounds.builder
import com.google.gson.Gson
import okhttp3.OkHttpClient
import java.util.stream.Stream.builder


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private lateinit var mMap: GoogleMap

    private val permisoFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION

    private val permisoCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION

    private val CODIGO_DE_SOLICITUD_DE_PERMISO = 100

    private var fusedLocationClient: FusedLocationProviderClient? = null

    private var locationRequest: LocationRequest? = null

    private var callback: LocationCallback? = null

    private var lista_de_marcadores: ArrayList<Marker>? = null

    var contador:Int = 0

    var ultima_posicion:LatLng? = null

    var ruta_marcada:Polyline? = null

    // Marcadores de mMap
    private var marcador_combis_de_la_petrolera:Marker? = null
    private var marcador_bodega_aurrera:Marker? = null
    private var marcador_hospital_de_pemex:Marker? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = FusedLocationProviderClient(this)
        iniciar_el_location_request()

        callback = object: LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                if(mMap != null){

                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true

                    for(ubicacion in locationResult?.locations!!){
                            Toast.makeText(applicationContext, ubicacion.latitude.toString() + "," + ubicacion.longitude.toString(), Toast.LENGTH_LONG).show()
                            if(contador == 0){
                                // Add a marker and move the camera
                                ultima_posicion = LatLng(ubicacion.latitude, ubicacion.longitude)
                                mMap.addMarker(MarkerOptions().position(ultima_posicion!!).title("¡Aquí estoy!"))
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(ultima_posicion))
                                contador = 1
                            }
                    }
                }
            }
        }
    }

    private fun iniciar_el_location_request(){
        locationRequest = LocationRequest()
        locationRequest?.interval = 10000
        locationRequest?.fastestInterval = 5000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        cambiar_estilo_del_mapa()

        marcadores_estaticos()

        crear_listeners()

        preparar_marcadores()

        dibujar_las_lineas()

    }

    private fun dibujar_las_lineas(){
        var coordenadas_de_lineas = PolylineOptions()
            .add(LatLng(17.857388663615584, -93.16025376319885))
            .add(LatLng(17.855553056136316, -93.16238276660442))
            .add(LatLng(17.854022198624097, -93.16469684243201))
            .color(Color.CYAN)
            .width(20f)
            .pattern(arrayListOf<PatternItem>(Dot(), Gap(10f)))

      var coordenadas_de_poligono = PolygonOptions()
          .add(LatLng(17.853101826832205, -93.15922547131777))
          .add(LatLng(17.849145195110186, -93.1583835929632))
          .add(LatLng(17.84705450831396, -93.15940149128437))
          .add(LatLng(17.84376412413436, -93.16129513084888))
          .strokePattern(arrayListOf<PatternItem>(Dash(10f), Gap(10f)))
          .strokeColor(Color.GREEN)
          .fillColor(Color.BLUE)
          .strokeWidth(10f)

        var coordenadas_de_circulo = CircleOptions()
            .center(LatLng(17.853245116650903, -93.15848618745804))
            .radius(120.0)
            .strokePattern(arrayListOf<PatternItem>(Dash(10f), Gap(10f)))
            .strokeWidth(15f)
            .strokeColor(Color.MAGENTA)
            .fillColor(Color.YELLOW)

        mMap.addCircle(coordenadas_de_circulo)
        mMap.addPolyline(coordenadas_de_lineas)
        mMap.addPolygon(coordenadas_de_poligono)
 }

    private fun cambiar_estilo_del_mapa(){
        //mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN

        val agrega_estilo = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(applicationContext,R.raw.estilo_mapa))

        if(!agrega_estilo){
            // Mencionar el error al cambiar el estilo
            Toast.makeText(applicationContext, "Error al cargar estilo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun crear_listeners(){
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMarkerDragListener(this)
    }

    private fun marcadores_estaticos(){
        // COORDENADAS
        // Hospital de Pemex = 17.859209, -93.154097
        // Bodega Aurrerá = 17.861113, -93.152514
        // Combis de la Petrolera = 17.864303, -93.147914

        val hospital_de_pemex = LatLng(17.859209, -93.154097)
        val bodega_aurrera = LatLng(17.861113, -93.152514)
        val combis_de_la_petrolera = LatLng(17.864303, -93.147914)

        marcador_hospital_de_pemex = mMap.addMarker(MarkerOptions()
                .position(hospital_de_pemex)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapas_icono))
                .snippet("Zona Industrial de Reforma, Chiapas")
                .alpha(0.5F)
                .title("Hospital de Pemex"))
        marcador_hospital_de_pemex?.tag = 0

        marcador_bodega_aurrera = mMap.addMarker(MarkerOptions()
                .position(bodega_aurrera)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapas_icono))
                .snippet("Zona Industrial de Reforma, Chiapas")
                .alpha(0.5F)
                .title("Bodega Aurrerá"))
        marcador_bodega_aurrera?.tag = 1

        marcador_combis_de_la_petrolera = mMap.addMarker(MarkerOptions()
                .position(combis_de_la_petrolera)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapas_icono))
                .snippet("Centro de Reforma, Chiapas")
                .alpha(0.5F)
                .title("Sitio de combis de la Petrolera"))
        marcador_combis_de_la_petrolera?.tag = 2
    }

    private fun preparar_marcadores(){
        lista_de_marcadores = ArrayList()
        mMap.setOnMapLongClickListener {
                location: LatLng? ->

            lista_de_marcadores?.add(mMap.addMarker(MarkerOptions()
                .position(location!!)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapas_icono))
                .snippet("Ubicación personalizada")
                .alpha(0.5F)
                .title("Sitio personalizado"))
            )
            lista_de_marcadores?.last()!!.isDraggable = true

            val coordenadas = LatLng(lista_de_marcadores?.last()!!.position.latitude, lista_de_marcadores?.last()!!.position.longitude)

            val origen = "origin=" + ultima_posicion?.latitude + "," + ultima_posicion?.longitude + "&"

            val destino = "destination=" + coordenadas.latitude + "," + coordenadas.longitude + "&"

            val parametros = origen + destino + "sensor=false&mode=driving"

            Log.d("URL", "https://maps.googleapis.com/maps/api/directions/json?" + parametros + "&key=AIzaSyAjXPplYmkUvnAG8hpH8IDsL66GXVAiTcs")
            mapear_direcciones_json("https://maps.googleapis.com/maps/api/directions/json?" + parametros + "&key=AIzaSyAjXPplYmkUvnAG8hpH8IDsL66GXVAiTcs").execute()
        }
    }

    // DRAG O SOSTENER MARCADORES, INICIO, DURANTE Y FINAL

    override fun onMarkerDragEnd(marcador: Marker?) {
        Toast.makeText(this,"Terminando de mover el marcador", Toast.LENGTH_LONG).show()

        Log.d("MARCADOR FINAL",marcador?.position?.latitude.toString() + ", " + marcador?.position?.longitude.toString())
    }

    override fun onMarkerDragStart(marcador: Marker?) {
        Toast.makeText(this,"Empezando a mover el marcador", Toast.LENGTH_LONG).show()

        Log.d("MARCADOR INICIAL",marcador?.position?.latitude.toString() + ", " + marcador?.position?.longitude.toString())
    }

    override fun onMarkerDrag(marcador: Marker?) {
        title = marcador?.position?.latitude.toString() + " - " + marcador?.position?.longitude.toString()
    }

    override fun onMarkerClick(marcador: Marker?): Boolean {
        var numero_de_clics = marcador?.tag as? Int

        if(numero_de_clics != null){
            numero_de_clics++
            marcador?.tag = numero_de_clics
            Toast.makeText(this, "Se han dado " + numero_de_clics.toString() + " clicks", Toast.LENGTH_LONG).show()
        }

        return false
    }

    private fun validarPermisosUbicacion():Boolean{
        val hayUbicacionPrecisa = ActivityCompat.checkSelfPermission(this,permisoFineLocation) == PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria:Boolean = ActivityCompat.checkSelfPermission(this,permisoCoarseLocation) == PackageManager.PERMISSION_GRANTED

        return hayUbicacionOrdinaria && hayUbicacionPrecisa
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion(){
        validarPermisosUbicacion()

        fusedLocationClient?.requestLocationUpdates(locationRequest,callback, null)
    }

    private fun pedirPermiso(){
        val mostrar_contexto_al_usuario = ActivityCompat.shouldShowRequestPermissionRationale(this,permisoFineLocation)
        if (mostrar_contexto_al_usuario){
            solicitarPermiso()
        } else {
            solicitarPermiso()
        }

    }

    private fun solicitarPermiso(){
        requestPermissions(arrayOf(permisoCoarseLocation,permisoFineLocation),CODIGO_DE_SOLICITUD_DE_PERMISO)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CODIGO_DE_SOLICITUD_DE_PERMISO ->{
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Obtener ubicación
                    obtenerUbicacion()
                } else{
                    Toast.makeText(this,"Diste permisos para acceder a la ubicación", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun detener_actualizacion_de_ubicacion(){
        fusedLocationClient?.removeLocationUpdates(callback)
    }

    override fun onStart() {
        super.onStart()

        if(validarPermisosUbicacion()){
            obtenerUbicacion()
        } else{
            pedirPermiso()
        }
    }

    override fun onPause() {
        super.onPause()
        detener_actualizacion_de_ubicacion()
    }


    // OBTENER LA UBICACIÓN Y COLOCAR MARCADORES

    private inner class mapear_direcciones_json(val url : String) : AsyncTask<Void,Void,List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()!!.string()
            Log.d("GoogleMap" , " data : $data")
            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data,GoogleMapDTO::class.java)

                val path =  ArrayList<LatLng>()

                for (i in 0..(respObj.routes[0].legs?.get(0)?.steps.size-1)){
                    path.addAll(decodePolyline(respObj.routes[0].legs?.get(0)?.steps?.get(i)?.polyline.points))
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.CYAN)
                lineoption.geodesic(true)
            }

            if(ruta_marcada != null){
                ruta_marcada?.remove()
            }
            ruta_marcada = mMap.addPolyline(lineoption)
        }
    }

    fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }
}