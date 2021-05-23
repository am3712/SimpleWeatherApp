package com.myapps.simpleweatherapp.ui.settings

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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.myapps.simpleweatherapp.BuildConfig
import com.myapps.simpleweatherapp.R
import com.myapps.simpleweatherapp.data.repository.LocationOptions
import com.myapps.simpleweatherapp.data.repository.UserPreferencesRepository
import com.myapps.simpleweatherapp.ui.firstopen.ui.detectlocation.REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.*


class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModels()

    private lateinit var loading: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataStore = object : PreferenceDataStore() {
            override fun putString(key: String, value: String?) {
                viewModel.saveToDataStore(key, value!!)
            }

            override fun putBoolean(key: String, value: Boolean) {
                viewModel.saveToDataStore(key, value)
            }
        }
        val preferenceManager = preferenceManager
        preferenceManager.preferenceDataStore = dataStore
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initPreferences()
        initAndSetupObservers()
    }

    private fun initPreferences() {
        //home location preference
        val locationName: Preference? =
            findPreference(UserPreferencesRepository.PreferencesKeys.LOCATION_NAME.name)

        //temperature unit ListPreference
        val temperatureUnit: ListPreference? =
            findPreference(UserPreferencesRepository.PreferencesKeys.TEMPERATURE_UNIT.name)

        //speed unit ListPreference
        val speedUnit: ListPreference? =
            findPreference(UserPreferencesRepository.PreferencesKeys.WIND_SPEED_UNIT.name)


        //weather Alarms SwitchPreferenceCompat
        val weatherAlarms: SwitchPreferenceCompat? =
            findPreference(UserPreferencesRepository.PreferencesKeys.ALARMS_KEY.name)

        //weather Alerts SwitchPreferenceCompat
        val weatherAlerts: SwitchPreferenceCompat? =
            findPreference(UserPreferencesRepository.PreferencesKeys.ALERTS_KEY.name)

        //weather Alerts SwitchPreferenceCompat
        val languageSettings: ListPreference? =
            findPreference(UserPreferencesRepository.PreferencesKeys.USER_LANG_KEY.name)


        viewModel.userLocationNamePreferences.observe(viewLifecycleOwner,
            { locationName?.summary = it })


        //current location option ListPreference
        val currentLocationOption: ListPreference? =
            findPreference(UserPreferencesRepository.PreferencesKeys.CURRENT_LOCATION_OPTION.name)


        currentLocationOption?.setOnPreferenceChangeListener { _, newValue ->
            when (newValue.toString()) {
                LocationOptions.CURRENT_LOCATION.name -> {
                    Timber.i("current location clicked")
                    viewModel.subscribeLocation()
                }

                LocationOptions.FROM_MAP.name -> {
                    Timber.i("select location from the map")
                    lifecycleScope.launchWhenStarted {
                        val latLng = viewModel.currentLocationLatLng.first()
                        findNavController().navigate(
                            SettingsFragmentDirections.actionNavigationSettingsToNavigationMaps(
                                navigateTo = 2,
                                latLng = latLng
                            )
                        )
                    }

                }
            }
            true
        }
        languageSettings?.setOnPreferenceChangeListener { _, newValue ->
            viewModel.updateLanguageSettings(newValue as String)
            Timber.i("update language")
            (requireActivity() as LocaleAwareCompatActivity).updateLocale(Locale(newValue))
            true
        }

        viewModel.userPreferences.observe(viewLifecycleOwner, {
            currentLocationOption?.value = it.currentOption.name
            temperatureUnit?.value = it.temperatureUnit.name
            speedUnit?.value = it.windSpeedUnit.name
            Timber.i("settings lang value :${it.lang} ")
            languageSettings?.value = it.lang
        })

        viewModel.alarmsPreferences.observe(viewLifecycleOwner, { weatherAlarms?.isChecked = it })

        weatherAlarms?.setOnPreferenceChangeListener { _, newValue ->
            viewModel.changeAlarms(newValue as Boolean)
            true
        }


        viewModel.alertsPreferences.observe(viewLifecycleOwner, { weatherAlerts?.isChecked = it })

        weatherAlerts?.setOnPreferenceChangeListener { _, newValue ->
            viewModel.changeAlerts(newValue as Boolean)
            true
        }
    }

    private fun initAndSetupObservers() {

        loading = MaterialAlertDialogBuilder(requireContext())
            .setView(R.layout.loading_layout)
            .setCancelable(false).create()

        viewModel.permissionStatus.observe(viewLifecycleOwner, {
            if (!it) {
                Timber.d("Request location permission : $it")
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE
                )
                viewModel.resetPermissionStatus()
            }
        })

        viewModel.locationName.observe(viewLifecycleOwner, { locationName ->
            if (!locationName.isNullOrEmpty() && locationName != "RESET") {
                Timber.i("!locationName.isNullOrEmpty() && locationName != \"RESET\"")
                viewModel.updateLocationOptions(LocationOptions.CURRENT_LOCATION)
                Snackbar.make(
                    requireView(),
                    "Home location details update", Snackbar.LENGTH_SHORT
                ).show()
                viewModel.resetLocation()
            } else if (locationName != "RESET") {
                Timber.i("Location information isn't available")
                Snackbar.make(
                    requireView(),
                    "Location information isn't available", Snackbar.LENGTH_SHORT
                ).setAction(getString(R.string.retry)) { viewModel.subscribeLocation() }.show()
            }

        })

        viewModel.startFetching.observe(viewLifecycleOwner, {
            if (it)
                loading.show()
            else
                loading.dismiss()
        })


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
                        requireView(),
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
}