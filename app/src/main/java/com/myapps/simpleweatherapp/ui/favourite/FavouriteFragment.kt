package com.myapps.simpleweatherapp.ui.favourite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.myapps.simpleweatherapp.R
import com.myapps.simpleweatherapp.data.local.City
import com.myapps.simpleweatherapp.databinding.FragmentFavouriteBinding

class FavouriteFragment : Fragment() {

    private var _binding: FragmentFavouriteBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: FavouriteViewModel by viewModels()

    private val args: FavouriteFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouriteBinding.inflate(inflater, container, false)
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(FavouriteFragmentDirections.actionNavigationFavouriteToMapsFragment())
        }
        val citiesAdapter = CityAdapter(object : FavouriteCityListener {
            override fun onClick(city: City) {
                Navigation.findNavController(binding.root).navigate(
                    FavouriteFragmentDirections.actionNavigationFavouriteToDetailsFragment(
                        city.lat,
                        city.lng,
                        city.location
                    )
                )
            }

            override fun onRemove(city: City) {
                viewModel.removeCity(city)
                Toast.makeText(requireContext(), getString(R.string.city_removed_message), Toast.LENGTH_SHORT).show()
            }

        })
        binding.cities.adapter = citiesAdapter

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        if (args.latLng != null && args.locationName != null) {
            viewModel.addCity(args.latLng!!, args.locationName!!)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}