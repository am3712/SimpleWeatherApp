package com.myapps.simpleweatherapp.ui.addalarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.myapps.simpleweatherapp.R
import com.myapps.simpleweatherapp.databinding.AddAlarmFragmentBinding
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.util.*

class AddAlarmFragment : Fragment() {

    private var _binding: AddAlarmFragmentBinding? = null

    private val binding get() = _binding!!

    private val viewModel: AddAlarmViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddAlarmFragmentBinding.inflate(inflater, container, false)
        binding.recyclerView.adapter = RepeatDayAdapter()
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerView.setHasFixedSize(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        initAndSetupObservers()
    }

    private fun initAndSetupObservers() {
        binding.timeFrom.setOnClickListener { initPicker("FROM") }
        binding.timeTo.setOnClickListener { initPicker("TO") }
        binding.saveButton.setOnClickListener {

            val alarmName = binding.alertName.text.toString()
            val alarmType = when (binding.radioGroup.checkedRadioButtonId) {
                binding.notificationButton.id -> getString(R.string.notification_type)
                binding.defaultAlarmButton.id -> getString(R.string.sound_alarm_type)
                else -> ""
            }
            viewModel.addAlarm(alarmName, alarmType)
        }
        viewModel.navigateTo.observe(viewLifecycleOwner, {
            when (it) {
                "" -> findNavController().navigate(AddAlarmFragmentDirections.actionAddAlertFragmentToAlertsFragment())
                " " -> Timber.i("Do not make any thing ")
                else -> {
                    Snackbar.make(
                        binding.root,
                        it, Snackbar.LENGTH_SHORT
                    ).show()
                    viewModel.clearNavigate()
                }
            }
        })
    }

    private fun initPicker(fromOrTo: String) {
        viewModel.getTimePickerBuilderInstance(fromOrTo)
            .setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
            .setMinute(Calendar.getInstance().get(Calendar.MINUTE))
        viewModel.timePicker.show(childFragmentManager, "MaterialTimePicker")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     *Timber.i("time : $it")
     * Timber.i(SimpleDateFormat("hh:mm aa").format(it))
     * Timber.i(DateFormat.getDateInstance().format(it))
     * Timber.i(DateFormat.getDateTimeInstance().format(it))
     * Timber.i(DateFormat.getTimeInstance(DateFormat.SHORT).format(it))
     */
}