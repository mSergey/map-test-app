package com.gmail.zajcevserg.maptestapp.ui.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.forEach

import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager

import com.gmail.zajcevserg.maptestapp.databinding.FragmentLayersSettingsBinding
import com.gmail.zajcevserg.maptestapp.model.database.LayerItem

import com.gmail.zajcevserg.maptestapp.ui.activity.log
import com.gmail.zajcevserg.maptestapp.ui.adapter.LayersAdapter
import com.gmail.zajcevserg.maptestapp.ui.custom.*
import com.gmail.zajcevserg.maptestapp.viewmodel.LayersVM

class LayersSettingsFragment : Fragment() {


    private var _binding: FragmentLayersSettingsBinding? = null
    private val binding get() = _binding!!
    private val mViewModel: LayersVM by activityViewModels()
    private var decorator: HeaderItemDecorator? = null
    private lateinit var mAdapter: LayersAdapter


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mAdapter = LayersAdapter(mViewModel, context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLayersSettingsBinding.inflate(inflater, container, false)

        with(binding) {
            layersRecyclerView.adapter = mAdapter
            layersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            layersRecyclerView.isScrollbarFadingEnabled = false

        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val helper = ItemTouchHelper(SwipeCallback(binding.layersRecyclerView, mViewModel))
        helper.attachToRecyclerView(binding.layersRecyclerView)

        mViewModel.liveDataLayers.observe(viewLifecycleOwner) { layers ->
            log("${layers[2].id}")
            log("${layers[2].expanded}")
            mAdapter.submitList(layers)
            decorator ?: run {
                decorator = HeaderItemDecorator(layers, requireContext())
                binding.layersRecyclerView.addItemDecoration(
                    HeaderItemDecorator(
                        layers,
                        requireContext()
                    )
                )
            }

            binding.undefinedImageView.setOnClickListener {
                it as ImageView
                val currentSwitchStateLevel = SwitchStates.values()[it.drawable.level]
                //mViewModel.onSwitchControlAllLayersClick(currentSwitchStateLevel)
            }

        }

        mViewModel.liveDataSwitchControlAllAppearance.observe(viewLifecycleOwner) {
            if (it == SwitchStates.STATE_NONE || it == null) return@observe
            binding.undefinedImageView.drawable.level = it.ordinal
        }


        binding.buttonDrag.setOnClickListener {
            mViewModel.liveDataDragMode.value = mViewModel.liveDataDragMode.value?.not()
        }

        mViewModel.liveDataDragMode.observe(viewLifecycleOwner) { isDragMode ->

            mAdapter.currentList.forEachIndexed { adapterPosition, _ ->
                val holder = binding.layersRecyclerView.findViewHolderForAdapterPosition(adapterPosition)
                        as? LayersAdapter.LayerItemViewHolder
                holder?.setDragMode(isDragMode)
            }
            if (isDragMode) {
                binding.undefinedImageView.visibility = View.GONE
                binding.buttonDrag.drawable.level = 1

            } else {

                binding.undefinedImageView.visibility = View.VISIBLE
                binding.buttonDrag.drawable.level = 0
            }
        }

        binding.buttonSearch.setOnClickListener {
            mViewModel.liveDataSearchMode.value = mViewModel.liveDataSearchMode.value?.not()
        }

        mViewModel.liveDataSearchMode.observe(viewLifecycleOwner) { isSearchMode ->
            binding.buttonSearch.drawable.level = if (isSearchMode) 1 else 0
        }



        val toast = Toast
            .makeText(requireActivity().applicationContext, "", Toast.LENGTH_SHORT)

        mViewModel.liveDataBackgroundBtn.observe(viewLifecycleOwner) {
            it ?: return@observe
            toast.setText(it)
            toast.show()

        }


    }

    companion object {
        private val instance = LayersSettingsFragment()
        @JvmStatic
        fun getInstance() {

        }



    }
}
