package com.gmail.zajcevserg.maptestapp.ui.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.forEach

import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gmail.zajcevserg.maptestapp.R

import com.gmail.zajcevserg.maptestapp.databinding.FragmentLayersSettingsBinding
import com.gmail.zajcevserg.maptestapp.ui.adapter.LayersAdapter
import com.gmail.zajcevserg.maptestapp.ui.custom.*
import com.gmail.zajcevserg.maptestapp.viewmodel.LayersVM

class LayersSettingsFragment : Fragment(), OnStartDragListener, View.OnClickListener {


    private var _binding: FragmentLayersSettingsBinding? = null
    private val binding get() = _binding!!
    private val mViewModel: LayersVM by activityViewModels()
    private lateinit var mAdapter: LayersAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mAdapter = LayersAdapter(mViewModel, context, this)
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
        itemTouchHelper =
            ItemTouchHelper(SwipeCallback(binding.layersRecyclerView, mViewModel, mAdapter))

        itemTouchHelper.attachToRecyclerView(binding.layersRecyclerView)

        mViewModel.liveDataLayers.observe(viewLifecycleOwner) { layers ->
            mAdapter.submitList(layers)
        }

        binding.buttonSearch.setOnClickListener(this)
        binding.buttonDrag.setOnClickListener(this)
        binding.buttonDel.setOnClickListener(this)
        binding.buttonAdd.setOnClickListener(this)

        mViewModel.liveDataDragMode.observe(viewLifecycleOwner) { isDragMode ->

            mAdapter.currentList.forEachIndexed { adapterPosition, item ->
                val holder = binding.layersRecyclerView.findViewHolderForAdapterPosition(adapterPosition)
                        as? LayersAdapter.LayerItemViewHolder
                holder?.setDragMode(isDragMode)
            }
            binding.buttonDrag.isActivated = isDragMode
            binding.mainSwitch.visibility = if (isDragMode) View.GONE else View.VISIBLE
        }

        mViewModel.liveDataSearchMode.observe(viewLifecycleOwner) { isSearchMode ->
            binding.buttonSearch.isActivated = isSearchMode
        }

        val toast = Toast
            .makeText(requireActivity().applicationContext, "", Toast.LENGTH_SHORT)

        mViewModel.liveDataBackgroundBtn.observe(viewLifecycleOwner) {
            it ?: return@observe
            toast.setText(it)
            toast.show()
        }

        mViewModel.liveDataMainSwitchPosition.observe(viewLifecycleOwner) {
            binding.mainSwitch.switchPosition = it
        }

        binding.mainSwitch.setOnPositionChangeByClickListener { position ->
            when(position) {
                Switch3Way.SwitchPositions.START -> {
                    mViewModel.setCheckedStatesForAll(false)
                    setSwitchPositionsOnViewHolders(position)
                }
                Switch3Way.SwitchPositions.MIDDLE -> {
                    mViewModel.setSavedCheckedStates()
                    setSavedCheckedStatesOnViewHolders(mViewModel.mSavedCheckedStates)
                }
                Switch3Way.SwitchPositions.END -> {
                    mViewModel.setCheckedStatesForAll(true)
                    setSwitchPositionsOnViewHolders(position)
                }
            }
        }

        mViewModel.liveDataIsSwitchTreeWay.observe(viewLifecycleOwner) {
            binding.mainSwitch.isThreeWay = it
        }

        binding.buttonAdd.setOnClickListener {

            // add layer
        }
    }

    private fun setSwitchPositionsOnViewHolders(position: Switch3Way.SwitchPositions) {
        mViewModel.liveDataLayers.value?.forEachIndexed { adapterPosition, item ->
            val holder =
                binding.layersRecyclerView.findViewHolderForAdapterPosition(adapterPosition)
                        as? LayersAdapter.LayerItemViewHolder
            holder?.binding?.layerSwitch?.switchPosition = position
        }




    }


    private fun setSavedCheckedStatesOnViewHolders(idFlagsDic: Map<Int, Boolean>) {

        val holdersToSetSwitchByFlag = mutableListOf<LayersAdapter.LayerItemViewHolder>()
        binding.layersRecyclerView.forEach { view ->
            val holder = binding.layersRecyclerView.findContainingViewHolder(view)
                    as? LayersAdapter.LayerItemViewHolder?
            if (holder != null) holdersToSetSwitchByFlag.add(holder)
        }

        holdersToSetSwitchByFlag.forEach {
            val itemModel = mAdapter.currentList[it.adapterPosition]
            val flag = idFlagsDic[itemModel.id]
            if (flag != null)
            it.binding.layerSwitch.switchPosition =
                if (flag) Switch3Way.SwitchPositions.END else Switch3Way.SwitchPositions.START
        }
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.button_del -> {
                mViewModel.removeLayers()
            }
            R.id.button_search -> {
                mViewModel.liveDataSearchMode.value = mViewModel.liveDataSearchMode.value?.not()
            }
            R.id.button_add -> {

            }
            R.id.button_drag -> {
                mViewModel.liveDataDragMode.value = mViewModel.liveDataDragMode.value?.not()
            }
        }
    }

}
