package com.gmail.zajcevserg.maptestapp.ui.fragment


import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.forEach
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

//import org.koin.androidx.viewmodel.ext.android.getViewModel

import com.gmail.zajcevserg.maptestapp.R
import com.gmail.zajcevserg.maptestapp.databinding.FragmentLayersSettingsBinding
import com.gmail.zajcevserg.maptestapp.model.application.noneExceptHeader
import com.gmail.zajcevserg.maptestapp.ui.adapter.LayersAdapter
import com.gmail.zajcevserg.maptestapp.ui.adapter.SearchAdapter
import com.gmail.zajcevserg.maptestapp.ui.custom.*
import com.gmail.zajcevserg.maptestapp.viewmodel.LayersVM
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel


class LayersSettingsFragment : Fragment(), OnStartDragListener, View.OnClickListener {

    private var _binding: FragmentLayersSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mLayersAdapter: LayersAdapter
    private lateinit var mSearchAdapter: SearchAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var swipeCallback: SwipeCallback
    private lateinit var mToast: Toast

    override fun onAttach(context: Context
    ) {
        super.onAttach(context)
        mLayersAdapter = LayersAdapter(getSharedViewModel<LayersVM>(), context, this)
        mSearchAdapter = SearchAdapter(context)
        mToast = Toast.makeText(
            requireActivity().applicationContext,
            "",
            Toast.LENGTH_SHORT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLayersSettingsBinding.inflate(inflater, container, false)
        with(binding) {
            layersRecyclerView.adapter = mLayersAdapter
            layersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            layersRecyclerView.isScrollbarFadingEnabled = false
        }
        return binding.root
    }

    override fun onViewCreated(view: View,
                               savedInstanceState: Bundle?
    ) {
        swipeCallback =
            SwipeCallback(binding.layersRecyclerView, getSharedViewModel<LayersVM>(), mLayersAdapter)
        itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.layersRecyclerView)
        getSharedViewModel<LayersVM>().liveDataLayers.observe(viewLifecycleOwner) { layers ->
            mLayersAdapter.submitList(layers)
        }
        binding.buttonSearch.setOnClickListener(this)
        binding.buttonDrag.setOnClickListener(this)
        binding.buttonDel.setOnClickListener(this)
        binding.buttonAddNewLayer.setOnClickListener(this)
        getSharedViewModel<LayersVM>().liveDataDragMode.observe(viewLifecycleOwner) { isDragMode ->
            mLayersAdapter.currentList.forEachIndexed { adapterPosition, item ->
                val holder = binding.layersRecyclerView.findViewHolderForAdapterPosition(adapterPosition)
                        as? LayersAdapter.LayerItemViewHolder
                holder?.setDragMode(isDragMode)
            }
            binding.buttonDrag.isActivated = isDragMode
            binding.mainSwitch.visibility = if (isDragMode) View.GONE else View.VISIBLE
        }

        //search mode on/off
        getSharedViewModel<LayersVM>().liveDataSearchMode.observe(viewLifecycleOwner) { isSearchMode ->

            val params = binding.searchInclude.itemSearchCl.layoutParams
                    as CoordinatorLayout.LayoutParams
            val behavior = params.behavior as SearchBarHideOnScrollBehavior
            behavior.isSearchMode = isSearchMode
            binding.buttonSearch.isActivated = isSearchMode

            setRVAdapter(
                binding.layersRecyclerView,
                if (isSearchMode && mSearchAdapter.currentList.isNotEmpty()) mSearchAdapter
                else mLayersAdapter
            )
        }

        // search
        val spanStr = SpannableString(
            requireContext().getString(R.string.search_hint)
        ).apply {
            setSpan(
                ForegroundColorSpan(Color.WHITE), 12, 13,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(Color.WHITE), 30, 31,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(Color.WHITE), 38, 39,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }

        binding.searchInclude.searchEt.hint = spanStr
        mSearchAdapter.setOnSearchItemClickListener {
            val text = "${mSearchAdapter.currentList[it].objectName} is clicked"
            mToast.setText(text)
            mToast.show()
        }

        var nothingFoundToast: Toast? = Toast
            .makeText(requireActivity().applicationContext, "", Toast.LENGTH_SHORT)

        binding.searchInclude.searchEt.doOnTextChanged { text, start, before, count ->
            text ?: return@doOnTextChanged
            if (!getSharedViewModel<LayersVM>().liveDataSearchMode.value!!) return@doOnTextChanged
            getSharedViewModel<LayersVM>().onSearchTextChange(text)
            val notFoundText = "\"$text\" not found"
            nothingFoundToast = if (text.isNotEmpty())
                Toast.makeText(
                    requireActivity().applicationContext,
                    notFoundText,
                    Toast.LENGTH_SHORT
                ) else null
        }

        getSharedViewModel<LayersVM>().liveDataSearch.observe(viewLifecycleOwner) {
            it ?: return@observe
            if (!getSharedViewModel<LayersVM>().liveDataSearchMode.value!!) return@observe
            if (it.isEmpty()) {
                setRVAdapter(binding.layersRecyclerView, mLayersAdapter)
                nothingFoundToast?.show()
            } else {
                setRVAdapter(binding.layersRecyclerView, mSearchAdapter)
                mSearchAdapter.submitList(it)
            }
        }

        //background buttons click
        getSharedViewModel<LayersVM>().liveDataBackgroundBtn.observe(viewLifecycleOwner) {
            it ?: return@observe
            mToast.setText(it)
            mToast.show()
        }

        getSharedViewModel<LayersVM>().liveDataMainSwitchPosition.observe(viewLifecycleOwner) {
            binding.mainSwitch.switchPosition = it
        }

        binding.mainSwitch.setOnPositionChangeByClickListener { position ->
            when(position) {
                Switch3Way.SwitchPositions.START -> {
                    getSharedViewModel<LayersVM>().setCheckedStatesForAll(false)
                    setSwitchPositionsOnViewHolders(position)
                }
                Switch3Way.SwitchPositions.MIDDLE -> {
                    getSharedViewModel<LayersVM>().setSavedCheckedStates()
                    setSavedCheckedStatesOnViewHolders(getSharedViewModel<LayersVM>().mSavedCheckedStates)
                }
                Switch3Way.SwitchPositions.END -> {
                    getSharedViewModel<LayersVM>().setCheckedStatesForAll(true)
                    setSwitchPositionsOnViewHolders(position)
                }
            }
        }
        getSharedViewModel<LayersVM>().liveDataIsSwitchTreeWay.observe(viewLifecycleOwner) {
            binding.mainSwitch.isThreeWay = it
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val params = binding.searchInclude.itemSearchCl.layoutParams
                as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as SearchBarHideOnScrollBehavior
        behavior.searchBarHideDisposable.dispose()
    }

    private fun setSwitchPositionsOnViewHolders(
        position: Switch3Way.SwitchPositions
    ) {
        getSharedViewModel<LayersVM>().liveDataLayers.value?.forEachIndexed { adapterPosition, item ->
            val holder =
                binding.layersRecyclerView.findViewHolderForAdapterPosition(adapterPosition)
                        as? LayersAdapter.LayerItemViewHolder
            holder?.binding?.layerSwitch?.switchPosition = position
        }
    }

    private fun setRVAdapter(
        rv: RecyclerView,
        _adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>,
    ) {
        if (_adapter.javaClass == rv.adapter?.javaClass) return
        else rv.adapter = _adapter
    }

    private fun setSavedCheckedStatesOnViewHolders(idFlagsDic: Map<Int, Boolean>
    ) {
        val holdersToSetSwitchByFlag = mutableListOf<LayersAdapter.LayerItemViewHolder>()
        binding.layersRecyclerView.forEach { view ->
            val holder = binding.layersRecyclerView.findContainingViewHolder(view)
                    as? LayersAdapter.LayerItemViewHolder?
            if (holder != null) holdersToSetSwitchByFlag.add(holder)
        }
        holdersToSetSwitchByFlag.forEach {
            val itemModel = mLayersAdapter.currentList[it.adapterPosition]
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
                if (mLayersAdapter.currentList.noneExceptHeader { it.selectedToRemove }) {
                    val text = "Nothing selected to remove." +
                            "\nTo select perform long click on layer icon."
                    mToast.setText(text)
                    mToast.duration = Toast.LENGTH_LONG
                    mToast.show()
                } else getSharedViewModel<LayersVM>().removeLayers()
            }
            R.id.button_search -> {
                getSharedViewModel<LayersVM>().liveDataSearchMode.value =
                    getSharedViewModel<LayersVM>().liveDataSearchMode.value?.not()
            }
            R.id.button_add_new_layer -> {
                getSharedViewModel<LayersVM>().addNewLayer {
                    binding.layersRecyclerView.smoothScrollToPosition(it)
                }
            }
            R.id.button_drag -> {
                getSharedViewModel<LayersVM>().liveDataDragMode.value =
                    getSharedViewModel<LayersVM>().liveDataDragMode.value?.not()
            }
        }
    }


}
