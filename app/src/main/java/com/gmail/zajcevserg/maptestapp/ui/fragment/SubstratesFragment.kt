package com.gmail.zajcevserg.maptestapp.ui.fragment


import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.Fragment

import com.google.android.gms.maps.GoogleMap

import com.gmail.zajcevserg.maptestapp.R
import com.gmail.zajcevserg.maptestapp.databinding.FragmentSubstratesBinding
import com.gmail.zajcevserg.maptestapp.viewmodel.LayersVM


class SubstratesFragment : Fragment() {
    private var _binding: FragmentSubstratesBinding? = null
    private val binding get() = _binding!!
    private val mViewModel: LayersVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubstratesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.liveDataMapType.observe(viewLifecycleOwner) {
            binding.mapTypeRadioGroup.check(when(it) {
                GoogleMap.MAP_TYPE_NORMAL -> R.id.scheme
                GoogleMap.MAP_TYPE_SATELLITE -> R.id.satellite
                GoogleMap.MAP_TYPE_HYBRID -> R.id.hybrid
                else -> R.id.scheme
            })
        }
        binding.mapTypeRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            mViewModel.liveDataMapType.value = when(i) {
                R.id.scheme -> GoogleMap.MAP_TYPE_NORMAL
                R.id.satellite -> GoogleMap.MAP_TYPE_SATELLITE
                R.id.hybrid -> GoogleMap.MAP_TYPE_HYBRID
                else -> GoogleMap.MAP_TYPE_NONE
            }
        }
    }
}