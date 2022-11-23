package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {

    private lateinit var geofencingClient: GeofencingClient
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = context?.getString(R.string.geofence_event)
        PendingIntent.getBroadcast(
            requireContext(),
            0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var reminder: ReminderDataItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude
            val longitude = _viewModel.longitude.value
            reminder =
                ReminderDataItem(title, description.value, location, latitude.value, longitude)

            if (checkFineAndAccessBackLocationAreApproved()) {
                checkIfSettingIsEnabledAndAddGeoFence()
            } else {
                requestFineAndBackgroundLocationPermissions()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    private fun checkFineAndAccessBackLocationAreApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ))

        val backgroundPermissionApproved =
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )

        return foregroundLocationApproved && backgroundPermissionApproved
    }

    private fun checkIfSettingIsEnabledAndAddGeoFence() {

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val locationSettingsRequestBuilder =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settings = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsTask =
            settings.checkLocationSettings(locationSettingsRequestBuilder.build())

        locationSettingsTask.addOnCompleteListener {
            if (it.isSuccessful) {
                addGeoFence()
            }
        }
    }

    private fun requestFineAndBackgroundLocationPermissions() {
        if (checkFineAndAccessBackLocationAreApproved())
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val requestCode = run {
            permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            3000
        }

        requestPermissions(permissionsArray, requestCode)
    }

    @SuppressLint("MissingPermission")
    private fun addGeoFence() {
        val geofence = Geofence.Builder()
            .setRequestId(reminder.id)
            .setCircularRegion(
                reminder.latitude!!,
                reminder.longitude!!,
                100f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                _viewModel.saveReminder(reminder)
            }
            addOnFailureListener {
                _viewModel.showSnackBar.value = getString(R.string.error_adding_geofence)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantedResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantedResults)
        if (grantedResults.isEmpty() ||
            grantedResults[0] == PackageManager.PERMISSION_DENIED ||
            (requestCode == 3000 &&
                    grantedResults[1] == PackageManager.PERMISSION_DENIED)
        ) {

            _viewModel.showSnackBar.value = getString(R.string.permission_denied_explanation)

        } else {
            checkIfSettingIsEnabledAndAddGeoFence()
        }
    }

}
