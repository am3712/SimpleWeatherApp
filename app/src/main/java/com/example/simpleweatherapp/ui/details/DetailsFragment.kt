package com.example.simpleweatherapp.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simpleweatherapp.R
import com.example.simpleweatherapp.databinding.DetailsFragmentBinding
import com.example.simpleweatherapp.ui.weather.DayAdapter
import com.example.simpleweatherapp.ui.weather.HourAdapter
import com.example.simpleweatherapp.utils.Status
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class DetailsFragment : Fragment() {


    private var _binding: DetailsFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var detailsViewModel: DetailsViewModel

    private lateinit var daysAdapter: DayAdapter

    private lateinit var hoursAdapter: HourAdapter

    private val args: DetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DetailsFragmentBinding.inflate(inflater, container, false)

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
        val factory =
            DetailsViewModel.Factory(
                args.cityLat,
                args.cityLng,
                args.loc,
                requireActivity().application
            )
        detailsViewModel = ViewModelProvider(this, factory).get(DetailsViewModel::class.java)

        binding.detailsViewModel = detailsViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupObserver()
    }

    private fun setupObserver() {

        // observe any other network problem
        detailsViewModel.networkRequest.observe(viewLifecycleOwner, {
            if (it.status == Status.ERROR) {
                binding.connectionError.visibility = View.VISIBLE
                binding.includedView.root.visibility = View.GONE
                //Timber.i("status : ${detailsViewModel.status.value}")
                //Handle Error
                it.message?.let { message ->
                    Snackbar.make(
                        binding.root,
                        message, Snackbar.LENGTH_SHORT
                    ).setAction(R.string.retry) {
                        detailsViewModel.retryFetchData()
                    }.show()
                }
                detailsViewModel.clearStatus()
            } else if (it.status == Status.SUCCESS) {
                binding.connectionError.visibility = View.GONE
                binding.includedView.root.visibility = View.VISIBLE
            }
        })

        detailsViewModel.isLoading.observe(viewLifecycleOwner, {
            Timber.i("isLoading : $it")
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}