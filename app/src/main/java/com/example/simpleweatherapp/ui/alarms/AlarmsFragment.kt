package com.example.simpleweatherapp.ui.alarms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.simpleweatherapp.data.local.Alarm
import com.example.simpleweatherapp.databinding.AlarmsFragmentBinding

class AlarmsFragment : Fragment() {

    private var _binding: AlarmsFragmentBinding? = null

    private val binding get() = _binding!!

    private val viewModel: AlarmsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AlarmsFragmentBinding.inflate(inflater, container, false)

        //alarms
        binding.alertsList.adapter = AlertAdapter(object : AlarmListener {
            override fun onDelete(alarm: Alarm) {
                viewModel.deleteAlarm(alarm)
            }

            override fun startStop(alarm: Alarm) {
                viewModel.startStopAlarm(alarm)
            }
        })

        binding.addAlertFab.setOnClickListener {
            findNavController().navigate(AlarmsFragmentDirections.actionAlertsFragmentToAddAlertFragment())
        }
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}