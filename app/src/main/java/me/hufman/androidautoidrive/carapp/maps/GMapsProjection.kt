package me.hufman.androidautoidrive.carapp.maps

import android.Manifest
import android.app.Presentation
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.Display
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.gmaps_projection.*
import me.hufman.androidautoidrive.R

class GMapsProjection(val parentContext: Context, display: Display): Presentation(parentContext, display) {
	val TAG = "TestProjection"
	var map: GoogleMap? = null
	var view: ImageView? = null
	val locationProvider = LocationServices.getFusedLocationProviderClient(context)!!
	val locationCallback = LocationCallbackImpl()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		window.setType(WindowManager.LayoutParams.TYPE_PRIVATE_PRESENTATION)
		setContentView(R.layout.gmaps_projection)

		gmapView.onCreate(savedInstanceState)
		gmapView.getMapAsync {
			map = it

			it.isIndoorEnabled = false
			it.isTrafficEnabled = true

			if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
				it.isMyLocationEnabled = true
			}

			with (it.uiSettings) {
				isCompassEnabled = true
				isMyLocationButtonEnabled = false
			}

			locationProvider.lastLocation.addOnCompleteListener { location ->
				it.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.result?.latitude ?: 0.0, location.result?.longitude ?: 0.0), 10f))
				it.animateCamera(CameraUpdateFactory.zoomTo(15f))
			}
		}
	}

	override fun onStart() {
		super.onStart()
		Log.i(TAG, "Projection Start")
		gmapView.onStart()
		gmapView.onResume()

		val locationRequest = LocationRequest()
		locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
		locationRequest.fastestInterval = 5

		if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			locationProvider.requestLocationUpdates(locationRequest, locationCallback, null)
		}
	}

	override fun onStop() {
		super.onStop()
		Log.i(TAG, "Projection Stopped")
		gmapView.onPause()
		gmapView.onStop()
		gmapView.onDestroy()
		locationProvider.removeLocationUpdates(locationCallback)
	}

	override fun onSaveInstanceState(): Bundle {
		val output = super.onSaveInstanceState()
		gmapView.onSaveInstanceState(output)
		return output
	}

	inner class LocationCallbackImpl: LocationCallback() {
		override fun onLocationResult(location: LocationResult?) {
			if (location != null && location.lastLocation != null) {
				map?.animateCamera(CameraUpdateFactory.newLatLng(LatLng(location.lastLocation.latitude, location.lastLocation.longitude)))
			}
		}
	}
}