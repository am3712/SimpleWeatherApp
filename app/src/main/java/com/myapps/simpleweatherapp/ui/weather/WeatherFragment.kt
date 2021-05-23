package com.myapps.simpleweatherapp.ui.weather

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.myapps.simpleweatherapp.R
import com.myapps.simpleweatherapp.databinding.FragmentWeatherBinding
import com.myapps.simpleweatherapp.utils.Status
import com.google.android.material.snackbar.Snackbar

class WeatherFragment : Fragment() {


    private var _binding: FragmentWeatherBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var daysAdapter: DayAdapter

    private lateinit var hoursAdapter: HourAdapter

    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentWeatherBinding.inflate(inflater, container, false)

        //days
        daysAdapter = DayAdapter()
        binding.includedView.dayList.adapter = daysAdapter

        //hours
        hoursAdapter = HourAdapter()
        binding.includedView.hoursList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.includedView.hoursList.adapter = hoursAdapter


        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.weatherViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        setupObserver()
    }

    private fun setupObserver() {
        //observer to detect if first time open app or should fetch data from network
        viewModel.hoursList.observe(viewLifecycleOwner, {
            if (it.isEmpty()) {
                viewModel.fetchWeatherAction()
                viewModel.updateHomeLocationName()
            }
        })

//        viewModel.homeName.observe(viewLifecycleOwner, { homeName ->
//            if (homeName.isNullOrEmpty())
//                findNavController().navigate(WeatherFragmentDirections.actionNavigationWeatherToStartUpFragment())
//        })

        //observe no internet Connection
        viewModel.networkStatus.observe(viewLifecycleOwner, {
            if (!it)
                AlertDialog.Builder(requireContext()).setTitle("No Internet Connection")
                    .setMessage("Please check your internet connection and try again")
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .setIcon(android.R.drawable.ic_dialog_alert).show()
        })

        // observe any other network problem
        viewModel.networkRequest.observe(viewLifecycleOwner, {
            if (it.status == Status.ERROR) {
                //Handle Error
                it.message?.let { message ->
                    Snackbar.make(
                        binding.root,
                        message, Snackbar.LENGTH_SHORT
                    ).setAnchorView(binding.root.rootView.findViewById(R.id.bottom_nav))
                        .show()
                }
                viewModel.clearStatus()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}