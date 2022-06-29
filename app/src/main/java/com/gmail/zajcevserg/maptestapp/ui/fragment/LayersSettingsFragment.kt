package com.gmail.zajcevserg.maptestapp.ui.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.forEach
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.gmail.zajcevserg.maptestapp.R
import com.gmail.zajcevserg.maptestapp.databinding.FragmentLayersSettingsBinding
import com.gmail.zajcevserg.maptestapp.model.application.noneExceptHeader
import com.gmail.zajcevserg.maptestapp.ui.adapter.LayersAdapter
import com.gmail.zajcevserg.maptestapp.ui.adapter.SearchAdapter
import com.gmail.zajcevserg.maptestapp.ui.custom.*
import com.gmail.zajcevserg.maptestapp.viewmodel.LayersVM

class LayersSettingsFragment : Fragment(), OnStartDragListener, View.OnClickListener {

    private var _binding: FragmentLayersSettingsBinding? = null
    private val binding get() = _binding!!
    private val mViewModel: LayersVM by activityViewModels()
    private lateinit var mLayersAdapter: LayersAdapter
    private lateinit var mSearchAdapter: SearchAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var swipeCallback: SwipeCallback
    private lateinit var mToast: Toast

    override fun onAttach(context: Context
    ) {
        super.onAttach(context)
        mLayersAdapter = LayersAdapter(mViewModel, context, this)
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
            SwipeCallback(binding.layersRecyclerView, mViewModel, mLayersAdapter)
        itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.layersRecyclerView)
        mViewModel.liveDataLayers.observe(viewLifecycleOwner) { layers ->
            mLayersAdapter.submitList(layers)
        }
        binding.buttonSearch.setOnClickListener(this)
        binding.buttonDrag.setOnClickListener(this)
        binding.buttonDel.setOnClickListener(this)
        binding.buttonAddNewLayer.setOnClickListener(this)
        mViewModel.liveDataDragMode.observe(viewLifecycleOwner) { isDragMode ->
            mLayersAdapter.currentList.forEachIndexed { adapterPosition, item ->
                val holder = binding.layersRecyclerView.findViewHolderForAdapterPosition(adapterPosition)
                        as? LayersAdapter.LayerItemViewHolder
                holder?.setDragMode(isDragMode)
            }
            binding.buttonDrag.isActivated = isDragMode
            binding.mainSwitch.visibility = if (isDragMode) View.GONE else View.VISIBLE
        }

        //search mode on/off
        mViewModel.liveDataSearchMode.observe(viewLifecycleOwner) { isSearchMode ->
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
            if (!mViewModel.liveDataSearchMode.value!!) return@doOnTextChanged
            mViewModel.onSearchTextChange(text)
            val notFoundText = "\"$text\" not found"
            nothingFoundToast = if (text.isNotEmpty())
                Toast.makeText(
                    requireActivity().applicationContext,
                    notFoundText,
                    Toast.LENGTH_SHORT
                ) else null
        }

        mViewModel.liveDataSearch.observe(viewLifecycleOwner) {
            it ?: return@observe
            if (!mViewModel.liveDataSearchMode.value!!) return@observe
            if (it.isEmpty()) {
                setRVAdapter(binding.layersRecyclerView, mLayersAdapter)
                nothingFoundToast?.show()
            } else {
                setRVAdapter(binding.layersRecyclerView, mSearchAdapter)
                mSearchAdapter.submitList(it)
            }
        }

        //background buttons click
        mViewModel.liveDataBackgroundBtn.observe(viewLifecycleOwner) {
            it ?: return@observe
            mToast.setText(it)
            mToast.show()
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
        mViewModel.liveDataLayers.value?.forEachIndexed { adapterPosition, item ->
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
                } else mViewModel.removeLayers()
            }
            R.id.button_search -> {
                mViewModel.liveDataSearchMode.value = mViewModel.liveDataSearchMode.value?.not()
            }
            R.id.button_add_new_layer -> {
                mViewModel.addNewLayer()
            }
            R.id.button_drag -> {
                mViewModel.liveDataDragMode.value = mViewModel.liveDataDragMode.value?.not()
            }
        }
    }

}
