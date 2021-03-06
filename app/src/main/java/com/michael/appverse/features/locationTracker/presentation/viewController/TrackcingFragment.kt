package com.michael.appverse.features.locationTracker.presentation.viewController

import `in`.myinnos.savebitmapandsharelib.SaveAndShare
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.michael.appverse.R
import com.michael.appverse.commons.ui.snack
import com.michael.appverse.commons.utils.*
import com.michael.appverse.core.baseClasses.BaseFragment
import com.michael.appverse.databinding.FragmentTrackcingBinding
import com.michael.appverse.features.locationTracker.data.mediator.viewModel.RunMediatorViewModel
import com.michael.appverse.features.locationTracker.model.Run
import com.michael.appverse.features.locationTracker.services.PolyLine
import com.michael.appverse.features.locationTracker.services.TrackingService
import com.michael.appverse.features.locationTracker.utils.LocationProvider
import com.michael.appverse.features.locationTracker.utils.TrackingUtils
import com.michael.appverse.features.locationTracker.utils.TrackingUtils.ACTION_STOP_SERVICE
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.michael.appverse.commons.ui.hideView
import com.michael.appverse.commons.ui.showView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt

@AndroidEntryPoint
class TrackcingFragment : BaseFragment() {

    private var isTracking = false
    private var pathPoints = mutableListOf<PolyLine>()
    private var map: GoogleMap? = null
    private lateinit var  marker : MarkerOptions
    private var mark : Marker? = null
    private var currentTimeMills = 0L
    private var buttonText = "start"

    private val viewModel: RunMediatorViewModel by viewModels()
    private lateinit var binding : FragmentTrackcingBinding

    private var weigth: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocationProvider.provide(requireContext(), requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTrackcingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        weigth = sharedPreference.loadFromSharedPref("Float", "runner_weight")
        binding.btnToggleRun.setOnClickListener { toggleRun() }
        binding.mapView.onCreate(savedInstanceState)
//        (activity as MainActivity).supportActionBar?.title = "Tracking Run"
        binding.mapView.getMapAsync {
            map = it
            addAllPolylines()
        }
        subscribeToObservers()
        binding.cancelLayout?.setOnClickListener {
            if (isTracking){ toggleRun() }
            showTrackingCancelDialogue()
        }

        binding.finishLayout?.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }

        binding.downloadAndShareLayout?.setOnClickListener {
            if (isTracking){ toggleRun() }
            shareLocationSnapShot()
        }
    }

    private fun addAllPolylines(){
        pathPoints.forEach {
            val polylineOptions = PolylineOptions()
                .color(TrackingUtils.POLY_LINE_COLOR)
                .width(TrackingUtils.POLY_LINE_WIDTH)
                .addAll(it)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun subscribeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner) { updateTracking(it) }
        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            addLatestPolyLine()
            moveCameraToUser()
        }
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner) {
            currentTimeMills = it
            val formattedTime = TrackingUtils.getFormattedStopWatchTime(currentTimeMills, true)
            binding.tvTimer.text = formattedTime

            if(currentTimeMills > 0L){
                binding.mapBottomBar!!.showView()
            } else {
                binding.mapBottomBar!!.hideView()
            }
        }
    }

    private fun toggleRun(){
        isTracking = if(isTracking){
            sendCommandToService(TrackingUtils.ACTION_PAUSE_SERVICE)
            false
        } else {
            sendCommandToService(TrackingUtils.ACTION_START_OR_RESUME_SERVICE)
            true
        }
    }

    private fun updateTracking(isTracking: Boolean){
        if(!isTracking){
            binding.btnToggleRun.text = buttonText
        } else {
            binding.btnToggleRun.text = "Pause"
            buttonText = "Resume"
        }
    }

    private fun moveCameraToUser(){
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                pathPoints.last().last(),
                TrackingUtils.MAP_ZOOM
            ))
            addMarker()
        }
    }

    private fun showTrackingCancelDialogue(){
        GenericDialogueBuilder.showDialogue(
            requireContext(),
            R.style.AlertDialogTheme,
            "Cancel Run",
            "Are you sure you want to cancel the current run?",
            R.drawable.ic_delete,
            "Yes", "No",
            { cancelRunDialoguePositiveClick() }, { toggleRun() })
     }

    private fun cancelRunDialoguePositiveClick(){
        sendCommandToService(TrackingUtils.ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackcingFragment_to_runFragment)
    }

    private fun addMarker(){
        TrackingService.userCurrentLocation.observe(viewLifecycleOwner) {
            marker = MarkerOptions()
                .position(it)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            mark?.remove()
            mark = map?.addMarker(marker)
        }
    }

    private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints){
            for (point in polyline){
                bounds.include(point)
            }
        }
        map?.moveCamera(CameraUpdateFactory.newLatLngBounds(
            bounds.build(),
            binding.mapView.width,
            binding.mapView.height,
            (binding.mapView.height * 0.05f).toInt())
        )
    }

    private fun shareLocationSnapShot(){
        map?.snapshot { bitmap ->
            SaveAndShare.save(requireActivity(), bitmap, "run_snapshot","my current location", "image/jpeg")
        }
    }

    private fun endRunAndSaveToDb(){
        map?.snapshot { bitmap ->
            var distanceInMeters = 0
            for (polyline in pathPoints){
                distanceInMeters += TrackingUtils.calculatePolylineLength(polyline).toInt()
            }
//            val avgSpeed = round((distanceInMeters / 1000f) / (currentTimeMills / 1000f / 60 / 60) * 10) / 10f
            val avgSpeed = ((distanceInMeters / 1000f) / (currentTimeMills / 1000f / 60 / 60) * 10).roundToInt() / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weigth).toInt()
            val run = Run(
                bitmap,
                dateTimeStamp,
                avgSpeed,
                distanceInMeters,
                caloriesBurned,
                currentTimeMills,
            )
            viewModel.insertRun(run)
            sendCommandToService(ACTION_STOP_SERVICE)
            snack(requireView(), "Run saved to database")
            lifecycleScope.launch {
                delay(1500)
                findNavController().navigate(R.id.action_trackcingFragment_to_runFragment)
            }
        }
    }

    private fun addLatestPolyLine(){ // add the latest polyline to the map
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1){
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(TrackingUtils.POLY_LINE_COLOR)
                .width(TrackingUtils.POLY_LINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(command: String) { // send command to service
        Intent(this.context, TrackingService::class.java).apply {
            this.action = command
            this@TrackcingFragment.context?.startService(this)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        binding.mapView.onDestroy()
//    }


}