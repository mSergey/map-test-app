package com.gmail.zajcevserg.maptestapp.ui.fragment


import android.graphics.Color
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.gmail.zajcevserg.maptestapp.R
import com.gmail.zajcevserg.maptestapp.databinding.FragmentMapsBinding
import com.gmail.zajcevserg.maptestapp.model.application.findExceptHeader
import com.gmail.zajcevserg.maptestapp.model.database.DataItem
import com.gmail.zajcevserg.maptestapp.ui.activity.log
import com.gmail.zajcevserg.maptestapp.viewmodel.LayersVM
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

class MapsFragment : Fragment() {


    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private var mGoogleMap: GoogleMap? = null
    private val mViewModel: LayersVM by activityViewModels()



    private val callback = OnMapReadyCallback { googleMap ->

        mGoogleMap ?: run { mGoogleMap = googleMap }

        mViewModel.liveDataLayersFlowable.observe(viewLifecycleOwner) {
            with(googleMap) {
                clear()
                if (it.isEmpty()) return@observe
                val layer = it.findExceptHeader { it.id == 1 }
                layer ?: return@observe
                if (layer.turnedOn) {
                    val polygon = addPolygon(mViewModel.getCoordinates())
                    val alpha = 255 * layer.transparency / 100
                    val color = Color.argb(alpha, 255, 0, 0)
                    polygon.fillColor = color
                }
            }
        }

        mViewModel.liveDataMapType.observe(viewLifecycleOwner) {
            mGoogleMap?.mapType = it
        }

        mViewModel.liveDataMapInteraction.observe(viewLifecycleOwner) { layerId ->
            layerId ?: return@observe
            if (layerId == 1) {
                val minS: Double = mViewModel.getCoordinates().points.minOf { it.latitude }
                val minW: Double = mViewModel.getCoordinates().points.minOf { it.longitude }
                val maxN: Double = mViewModel.getCoordinates().points.maxOf { it.latitude }
                val maxE: Double = mViewModel.getCoordinates().points.maxOf { it.longitude }
                val bounds = LatLngBounds(
                    LatLng(minS, minW),
                    LatLng(maxN, maxE)
                )
                binding.root.post {
                    mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)


    }
}