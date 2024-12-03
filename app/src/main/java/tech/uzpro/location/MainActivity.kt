package tech.uzpro.location

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Geo
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.runtime.image.ImageProvider
import tech.uzpro.location.databinding.ActivityMainBinding
import tech.uzpro.location.databinding.DialogViewBinding

class MainActivity : AppCompatActivity(), InputListener {


    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var mapKit: MapKit
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private val pointsList = mutableListOf<Point>()
    private val listDistance = mutableListOf<String>()
    private var myLocation: Point? = null
    private lateinit var adapter: AdapterDistance
    private val dialogBinding: DialogViewBinding by lazy {
        DialogViewBinding.inflate(layoutInflater)
    }
    private lateinit var dialog: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey(MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        setContentView(binding.root)
        binding.mapview.map.addInputListener(this)
        mapKit = MapKitFactory.getInstance()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        showMyDialog()
        adapter = AdapterDistance(listDistance)
        dialogBinding.recyclerView.adapter = adapter
        binding.btnGps.setOnClickListener {
            getMyLocation()
        }
        binding.btnList.setOnClickListener {
            calculateDistances()
        }
    }

    private fun showMyDialog() {
        dialog = AlertDialog.Builder(this).create()
        dialog.setView(dialogBinding.root)
        dialog.setTitle("Masofalar")
    }


    override fun onStart() {
        binding.mapview.onStart()
        MapKitFactory.getInstance().onStart()
        super.onStart()
    }

    override fun onStop() {
        binding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }


    private fun getMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val tasc = fusedLocationProviderClient.lastLocation
            tasc.addOnSuccessListener { location ->
                Log.d(TAG, "getMyLocation: $location")
                if (location != null) {
                    val newPosition = Point(location.latitude, location.longitude)
                    myLocation = newPosition
                    val placemark = binding.mapview.map.mapObjects.addPlacemark(newPosition)
                    placemark.setIcon(ImageProvider.fromResource(this, R.drawable.own_location))
                    binding.mapview.map.move(
                        CameraPosition(newPosition, 15f, 0f, 0f),
                        Animation(Animation.Type.SMOOTH, 0.5f),
                        null
                    )
                } else {
                    Toast.makeText(this, "Joylashuvni yoqing", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            gettingPermissionLocation()
        }
    }

    private fun gettingPermissionLocation() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            val alertdialog = AlertDialog.Builder(this).create()
            alertdialog.setTitle("Joylashuni yoqish")
            alertdialog.setMessage("Joylashuv bo'limidan foydalanish uchun ruxsat oling")
            alertdialog.setButton(
                AlertDialog.BUTTON_NEGATIVE, "No"
            ) { _, _ -> alertdialog.dismiss() }
            alertdialog.setButton(
                AlertDialog.BUTTON_POSITIVE, "Ok"
            ) { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), TWO_REQUEST_LOCATION
                )
            }
            alertdialog.show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                FORST_REQUEST_LOCATION
            )
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: ok")
                getMyLocation()
            } else {
                Toast.makeText(this, "Iltimos joylashuni yoqing", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    override fun onMapTap(p0: Map, p1: Point) {
        pointsList.add(p1)
        val pointText = Point(p1.latitude + 0.0006, p1.longitude)
        val placemarkIcon = binding.mapview.map.mapObjects.addPlacemark(p1)
        val placemarkText = binding.mapview.map.mapObjects.addPlacemark(pointText)
        placemarkIcon.setIcon(ImageProvider.fromResource(this, R.drawable.my_l))
        if (myLocation != null) {
            val distance = Geo.distance(myLocation!!, p1)
            Toast.makeText(this, "Masofa sizdan ${distance.toInt()} metr", Toast.LENGTH_SHORT)
                .show()
            println("Masofa sizdan $distance metr")
            placemarkText.setText("${distance.toInt()} m")
        }
    }

    override fun onMapLongTap(p0: Map, p1: Point) {

    }

    private fun calculateDistances() {
        if (pointsList.size > 0 && myLocation != null) {
            listDistance.clear()
            for (i in 0 until  pointsList.size) {
                val distance = Geo.distance(myLocation, pointsList[i])
                println("Masofa ${i + 2}-nuqtagacha: ${distance} metr")
                listDistance.add("Masofa sizdan ${i + 1}-nuqtagacha: ${distance.toInt()} metr")
                adapter.notifyItemInserted(listDistance.size - 1)
            }
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok") { _, _ ->
                dialog.dismiss()
            }
            dialog.show()
        } else {
            Toast.makeText(this, "Yetarli nuqta mavjud emas", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val MAPKIT_API_KEY = "d45386b4-4153-4038-b6bb-5fb94e0d50bb"
        private const val TAG = "MainActivity"
        private const val FORST_REQUEST_LOCATION = 10
        private const val TWO_REQUEST_LOCATION = 20

    }

}