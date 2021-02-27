package com.example.simpleweatherapp.ui.firstopen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.simpleweatherapp.BuildConfig
import com.example.simpleweatherapp.MainActivity
import com.example.simpleweatherapp.R
import com.example.simpleweatherapp.databinding.FragmentStartUpBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber


const val REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE = 34

class StartUpFragment : androidx.fragment.app.Fragment() {


    private var _binding: FragmentStartUpBinding? = null

    private lateinit var loading: AlertDialog

    private var alertDialog: AlertDialog? = null

    private val binding get() = _binding!!

    private val viewModel: StartUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartUpBinding.inflate(inflater, container, false)

        binding.pickFromMap.setOnClickListener {
            findNavController().navigate(
                StartUpFragmentDirections.actionStartUpFragmentToNavigationMaps(
                    navigateTo = 1
                )
            )
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alertDialog = initAlertDialog()
        initAndSetupObservers()
    }

    private fun initAndSetupObservers() {
        loading = MaterialAlertDialogBuilder(requireContext())
            .setView(R.layout.loading_layout)
            .setCancelable(false).create()

        viewModel.permissionStatus.observe(viewLifecycleOwner, {
            if (!it) {
                Timber.d("Request location permission")
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE
                )
                //chane status to when rotate not fire request permissions again
                viewModel.resetPermissionStatus()
            }
        })

        binding.autoDetect.setOnClickListener { viewModel.subscribeLocation() }

        viewModel.locationName.observe(viewLifecycleOwner, { locationName ->
            if (!locationName.isNullOrEmpty() && locationName != "RESET") {
                Timber.i("locationName is : $locationName")
                alertDialog?.show()
            } else if (locationName == "RESET")
                Timber.i("Do Noting")
            else {
                Snackbar.make(
                    binding.root,
                    "Location information isn't available", Snackbar.LENGTH_SHORT
                ).setAction(getString(R.string.retry)) { viewModel.subscribeLocation() }.show()
            }

        })

        viewModel.loading.observe(viewLifecycleOwner, {
            if (it)
                loading.show()
            else
                loading.dismiss()
        })

        viewModel.finishAndNavigate.observe(viewLifecycleOwner, {
            (activity as MainActivity).supportActionBar?.show()
            findNavController().navigate(StartUpFragmentDirections.actionStartUpFragmentToNavigationWeather())
        })
    }

    private fun initAlertDialog(): AlertDialog {
        // setup the alert builder
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle("Receive notification about weather Alerts ?")
        // add OK and Cancel buttons
        builder.setPositiveButton("Receive") { _, _ -> viewModel.updateAlertsStatus(true) }
        builder.setNegativeButton("Disable") { _, _ -> viewModel.updateAlertsStatus(false) }
        builder.setNeutralButton("cancel") { _, _ -> viewModel.resetLocation() }
        builder.setCancelable(false)

        // create and show the alert dialog
        return builder.create()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Timber.i("onRequestPermissionResult")

        when (requestCode) {
            REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive empty arrays.
                    Timber.i("User interaction was cancelled.")

                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    // Permission was granted.
                    viewModel.subscribeLocation()

                else -> {
                    // Permission denied.
                    Snackbar.make(
                        binding.root,
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.title_settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }.show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (alertDialog != null && alertDialog!!.isShowing)
            alertDialog?.dismiss()
        _binding = null
    }
}