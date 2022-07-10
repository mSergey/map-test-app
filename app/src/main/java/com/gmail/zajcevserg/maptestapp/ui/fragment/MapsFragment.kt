package com.gmail.zajcevserg.maptestapp.ui.fragment


import android.graphics.Color

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment

import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

import com.gmail.zajcevserg.maptestapp.R
import com.gmail.zajcevserg.maptestapp.databinding.FragmentMapsBinding
import com.gmail.zajcevserg.maptestapp.model.application.findExceptHeader
import com.gmail.zajcevserg.maptestapp.viewmodel.LayersVM


class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private var mGoogleMap: GoogleMap? = null
    //private val mViewModel: LayersVM by viewModel()
    private val callback = OnMapReadyCallback { googleMap ->
        mGoogleMap ?: run { mGoogleMap = googleMap }
        getSharedViewModel<LayersVM>().liveDataLayersFlowable.observe(viewLifecycleOwner) {
            with(googleMap) {
                clear()
                if (it.isEmpty()) return@observe
                val layer = it.findExceptHeader { it.id == 1 }
                layer ?: return@observe
                if (layer.turnedOn) {
                    val polygon = addPolygon(getSharedViewModel<LayersVM>().getCoordinates())
                    val alpha = 255 * layer.transparency / 100
                    val color = Color.argb(alpha, 255, 0, 0)
                    polygon.fillColor = color
                }
            }
        }

        getSharedViewModel<LayersVM>().liveDataMapType.observe(viewLifecycleOwner) {
            mGoogleMap?.mapType = it
        }

        getSharedViewModel<LayersVM>().liveDataMapInteraction.observe(viewLifecycleOwner) { layerId ->
            layerId ?: return@observe
            if (layerId == 1) {
                val minS: Double = getSharedViewModel<LayersVM>().getCoordinates().points.minOf { it.latitude }
                val minW: Double = getSharedViewModel<LayersVM>().getCoordinates().points.minOf { it.longitude }
                val maxN: Double = getSharedViewModel<LayersVM>().getCoordinates().points.maxOf { it.latitude }
                val maxE: Double = getSharedViewModel<LayersVM>().getCoordinates().points.maxOf { it.longitude }
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