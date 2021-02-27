package com.example.simpleweatherapp.ui.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.simpleweatherapp.MainActivity
import com.example.simpleweatherapp.R
import com.example.simpleweatherapp.databinding.FragmentDetectLocationBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class DetectLocationFragment : Fragment() {
    private val viewModel: DetectLocationViewModel by viewModels()

    private val args: DetectLocationFragmentArgs by navArgs()

    private var _binding: FragmentDetectLocationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap

    private var markerOptions: MarkerOptions? = null

    private var dialog: AlertDialog? = null


    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap

        //These coordinates represent the lattitude and longitude of the Googleplex.
        //30.06747761015243, 31.259144426449467
        if (args.latLng != null) {
            val zoomLevel = 3f
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(args.latLng, zoomLevel))
            viewModel.setLatLon(args.latLng!!)
        }
        setMapClick(map)
        enableMyLocation()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetectLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (args.navigateTo == 1)
            initAlertsDialogPermission()
        saveLocation()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.showDialog.observe(viewLifecycleOwner, {
            if (it)
                dialog?.show()
        })
    }

    private fun navigate(status: Boolean) {
        lifecycleScope.launch {
            viewModel.updateAlertsStatus(status)
            //update home location
            viewModel.updateHomeLocation()
            (activity as MainActivity).supportActionBar?.show()
            //navigate to Weather screen
            findNavController().navigate(DetectLocationFragmentDirections.actionNavigationMapsToNavigationWeather())
        }
    }

    @SuppressLint("ShowToast")
    private fun saveLocation() {
        binding.saveLocation.setOnClickListener {
            Timber.i("saveLocation setOnClickListener Called")
            val latLng = viewModel.latLon.value
            val placeName = viewModel.placeDisplayName.value
            if (latLng != null && !placeName.isNullOrEmpty()) {
                when (args.navigateTo) {
                    //navigate to favourite screen
                    0 -> findNavController().navigate(
                        DetectLocationFragmentDirections.actionMapsFragmentToNavigationFavourite(
                            latLng,
                            placeName
                        )
                    )

                    //navigate to Weather screen
                    1 -> viewModel.showDialog()


                    2 -> {
                        lifecycleScope.launch {
                            viewModel.updateHomeLocation()

                            //get latest alerts of new location
                            viewModel.fetchAlerts()

                            //navigate to Settings screen
                            findNavController().navigate(DetectLocationFragmentDirections.actionNavigationMapsToNavigationSettings())
                        }
                    }
                }
            } else
                Toast.makeText(
                    requireContext(),
                    "picked location info not completed!!! ",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun setObservable() {
        viewModel.placeDisplayName.observe(viewLifecycleOwner, { name ->
            var placeName: String? = name
            if (placeName == null) {
                placeName = "failed to get place info, retrying... "
                viewModel.retryFetchPlaceInfo()
            }
            val snippet = viewModel.latLon.value?.let { latLng ->
                String.format(
                    Locale.getDefault(),
                    placeName,
                    latLng.latitude,
                    latLng.longitude
                )
            }
            map.addMarker(
                viewModel.latLon.value?.let { latLng ->
                    getMarker()
                        .position(latLng)
                        .snippet(snippet)
                }
            ).showInfoWindow()
        })
    }

    // Called when user makes a long press gesture on the map.
    private fun setMapClick(map: GoogleMap) {
        setObservable()
        map.setOnMapClickListener { latLng ->
            map.clear()
            viewModel.setLatLon(latLng)
        }
    }


    // Checks if users have given their location and sets location enabled if so.
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == Companion.REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
                Timber.i("PERMISSION_GRANTED")
            }
        }
    }

    private fun initAlertsDialogPermission() {
        // setup the alert builder
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle("Receive notification about weather Alerts ?")
        // add OK and Cancel buttons
        builder.setPositiveButton("Receive") { _, _ -> navigate(true) }
        builder.setNegativeButton("Disable") { _, _ -> navigate(false) }
        builder.setNeutralButton("Cancel") { _, _ -> viewModel.hideDialog() }
        builder.setCancelable(false)
        dialog = builder.create()
    }


    private fun getMarker(): MarkerOptions {
        if (markerOptions == null)
            markerOptions = MarkerOptions()
                .title(getString(R.string.dropped_pin))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
        return markerOptions!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (dialog != null)
            dialog?.dismiss()
        _binding = null
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}