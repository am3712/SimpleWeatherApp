package com.myapps.simpleweatherapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.myapps.simpleweatherapp.data.repository.UserPreferencesRepository
import com.myapps.simpleweatherapp.ui.firstopen.StartUpActivity
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.*

class MainActivity : LocaleAwareCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var bottomNavigationView: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_weather,
                R.id.navigation_favourite,
                R.id.navigation_alarms,
                R.id.navigation_settings
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavigationView = findViewById(R.id.bottom_nav)

        bottomNavigationView.setupWithNavController(navController)

        handleNavigation()

        lifecycleScope.launchWhenCreated {
            val homeLocationName =
                UserPreferencesRepository.getInstance(this@MainActivity).homeLocationName.first()
            if (homeLocationName.isEmpty()) {
                startActivity(Intent(this@MainActivity, StartUpActivity::class.java))
                finish()
            }
        }
    }

    private fun handleNavigation() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.navigation_maps ||
                destination.id == R.id.navigation_details ||
                destination.id == R.id.navigation_addAlarm
            )
                bottomNavigationView.visibility = View.GONE
            else if (destination.id == R.id.startUpFragment) {
                bottomNavigationView.visibility = View.GONE
                supportActionBar?.hide()
            } else
                bottomNavigationView.visibility = View.VISIBLE
        }


    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        Timber.i("30.06747761015243 to float : ${30.06747761015243.toFloat()}")
        Timber.i("31.259144426449467 to float : ${31.259144426449467.toFloat()}")
        Timber.i("language : ${Locale.getDefault().language}")
//        val dayDisplayName = SimpleDateFormat("EEEE", Locale.getDefault())
//        val c = Calendar.getInstance()
//        c.timeInMillis = System.currentTimeMillis()
//        c.add(Calendar.DAY_OF_MONTH, 1)
//        Timber.i(DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()).format(c.time))
//        Timber.i(dayDisplayName.format(c.time))


//        val weekdays: Array<String> = DateFormatSymbols(Locale.getDefault()).weekdays
//        val c = Calendar.getInstance()
//        val date = Date()
//        c.time = date
//        c.set(Calendar.DAY_OF_WEEK, 1)
//        val dayOfWeek = c[Calendar.DAY_OF_WEEK]
//        Timber.i(DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()).format(c.time))
//        Timber.i("dayOfWeek : $dayOfWeek")
//        Timber.i(weekdays[dayOfWeek])

    }
}