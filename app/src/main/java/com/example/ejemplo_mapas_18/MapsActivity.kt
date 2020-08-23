package com.example.ejemplo_mapas_18

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private lateinit var mMap: GoogleMap

    private val permisoFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION

    private val permisoCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION

    private val CODIGO_DE_SOLICITUD_DE_PERMISO = 100

    private var fusedLocationClient: FusedLocationProviderClient? = null

    private var locationRequest: LocationRequest? = null

    private var callback: LocationCallback? = null

    private var lista_de_marcadores: ArrayList<Marker>? = null

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

                        // Add a marker and move the camera
                        val ultima_posicion = LatLng(ubicacion.latitude, ubicacion.longitude)
                        mMap.addMarker(MarkerOptions().position(ultima_posicion).title("¡Aquí estoy!"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(ultima_posicion))
                    }
                }

            }
        }

    }

    private fun iniciar_el_location_request(){
        locationRequest = LocationRequest()
        locationRequest?.interval = 1000
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
     /* var coordenadas = PolygonOptions()
         .add(LatLng(17.853245116650903, -93.15848618745804))
         .add(LatLng(17.851502334428435, -93.15631963312626))
         .add(LatLng(17.848491598824335, -93.1560031324625))
     */
        var coordenadas = CircleOptions()
            .center(LatLng(17.853245116650903, -93.15848618745804))
            .radius(30.0)

        mMap.addCircle(coordenadas)
 }

    private fun cambiar_estilo_del_mapa(){
        //mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN

        val agrega_estilo = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(applicationContext,R.raw.estilo_mapa))

        if(!agrega_estilo){
            // Mencionar el error al cambiar el estilo
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
        }
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

    fun solicitarPermiso(){
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
                    Toast.makeText(this,"No diste permisos para acceder a la ubicación", Toast.LENGTH_SHORT).show()
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

}