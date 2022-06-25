package com.gmail.zajcevserg.maptestapp.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.forEach
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import java.text.DateFormat
import java.text.SimpleDateFormat


import com.gmail.zajcevserg.maptestapp.R
import com.gmail.zajcevserg.maptestapp.databinding.DividerSharedLayersBinding
import com.gmail.zajcevserg.maptestapp.databinding.ItemLayerLayoutBinding
import com.gmail.zajcevserg.maptestapp.model.application.anyExceptHeader
import com.gmail.zajcevserg.maptestapp.model.application.findExceptHeader
import com.gmail.zajcevserg.maptestapp.model.database.DataItem
import com.gmail.zajcevserg.maptestapp.model.database.DataItem.LayerItem
import com.gmail.zajcevserg.maptestapp.ui.custom.OnItemMoveListener
import com.gmail.zajcevserg.maptestapp.ui.custom.OnStartDragListener
import com.gmail.zajcevserg.maptestapp.ui.custom.Switch3Way
import com.gmail.zajcevserg.maptestapp.viewmodel.LayersVM
import com.google.android.material.slider.Slider
import java.util.*


class LayersAdapter(val mViewModel: LayersVM,
                    val context: Context,
                    val startDragListener: OnStartDragListener
) : ListAdapter<DataItem, RecyclerView.ViewHolder>(LayersDiff),
    OnItemMoveListener {

    companion object LayersDiff : DiffUtil.ItemCallback<DataItem>() {

        override fun areContentsTheSame(
            oldItem: DataItem,
            newItem: DataItem
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(
            oldItem: DataItem,
            newItem: DataItem
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }

    inner class DividerViewHolder(
        binding: DividerSharedLayersBinding
    ) : RecyclerView.ViewHolder(binding.root
    ) {
        init {
            val currentDate = Date()
            val dateFormat: DateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val dateText = dateFormat.format(currentDate)
            binding.refreshDateTv.text = dateText
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    inner class LayerItemViewHolder(
        val binding: ItemLayerLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            with(binding) {

                transparencySlider.setOnTouchListener { view, motionEvent ->
                    view.parent.requestDisallowInterceptTouchEvent(true)
                    view.performClick()
                }

                transparencySlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                    @SuppressLint("RestrictedApi")
                    override fun onStartTrackingTouch(slider: Slider) {
                        // Responds to when slider's touch event is being started
                    }
                    @SuppressLint("RestrictedApi")
                    override fun onStopTrackingTouch(slider: Slider) {
                        val idToUpdate = try {
                            currentList[adapterPosition].id
                        } catch(exception: IndexOutOfBoundsException) {
                            return
                        }
                        mViewModel.transparencyChange(idToUpdate, slider.value.toInt())
                        // Responds to when slider's touch event is being stopped
                    }
                })

                transparencySlider.addOnChangeListener { slider, value, fromUser ->
                    setColoredFormatText(value.toInt(), transparencyTextView)
                }

                layerSwitch.setOnPositionChangeByClickListener { switchPosition ->

                    val mChecked = when (switchPosition) {
                        Switch3Way.SwitchPositions.START -> false
                        Switch3Way.SwitchPositions.MIDDLE -> false
                        Switch3Way.SwitchPositions.END -> true
                    }

                    val idToUpdate = try {
                        currentList[adapterPosition].id
                    } catch(exception: IndexOutOfBoundsException) {
                        return@setOnPositionChangeByClickListener
                    }

                    mViewModel.onLayerSwitchClicked(idToUpdate, mChecked)

                }

                val mListener = View.OnClickListener {
                    val idToUpdate = try {
                        currentList[adapterPosition].id
                    } catch(exception: IndexOutOfBoundsException) {
                        return@OnClickListener
                    }

                    val itemToCollapse = currentList.findExceptHeader {
                        it.id != idToUpdate && it.expanded
                    }

                    val indexToCollapse = currentList.indexOf(itemToCollapse)
                    notifyItemChanged(indexToCollapse)
                    notifyItemChanged(adapterPosition)
                    mViewModel.expandLayer(idToUpdate)
                }

                layerTitle.setOnClickListener(mListener)
                expandImage.setOnClickListener(mListener)

                zoomToFitButton.setOnClickListener {
                    mViewModel.liveDataMapInteraction.value = currentList[adapterPosition].id
                    mViewModel.liveDataMapInteraction.value = null
                }
            }
        }

        fun bindView(position: Int
        ) {

            val layerItemModel = currentList[position] as LayerItem
            with (binding) {

                motionLayer.translationX = 0f
                backgroundButtonsLayer.scaleX = 0f
                backgroundButtonsLayer.scaleY = 0f

                //transparency value
                setColoredFormatText(layerItemModel.transparency, transparencyTextView)

                // number of views
                numberOfViews.text = layerItemModel.numberOfViews.toString()

                //element count
                val elementCountText = context.getString(R.string.elements_count)
                val viewsSpannable = SpannableStringBuilder(elementCountText)
                viewsSpannable.setSpan(
                    ForegroundColorSpan(Color.WHITE),
                    7,
                    viewsSpannable.length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                viewsSpannable.insert(7, layerItemModel.elementCount.toString())
                binding.elementCountTextView.setText(viewsSpannable, TextView.BufferType.SPANNABLE)

                //icon
                setLayerIcon(layerItemModel, layerIcon)

                binding.bgTop.isSelected = layerItemModel.selectedToRemove

                //long click to select
                binding.layerIcon.setOnLongClickListener {
                    setSelectionToRemove(layerItemModel, layerIcon)
                    true
                }

                binding.layerIcon.setOnClickListener {
                    val isSelectionMode = currentList.anyExceptHeader {
                        it.selectedToRemove
                    }
                    if (isSelectionMode) setSelectionToRemove(layerItemModel, layerIcon)
                }

                //title
                layerTitle.text = layerItemModel.title
                layerTitle.isActivated = layerItemModel.expanded
                layerTitle.typeface =
                    if (layerItemModel.expanded) Typeface.DEFAULT_BOLD else Typeface.DEFAULT

                //sleep icon
                binding.sleepImage.visibility = if (!layerItemModel.enabled) View.VISIBLE else View.GONE


                //expanded state
                bottomPanelGroup.visibility =
                    if (layerItemModel.expanded) View.VISIBLE else View.GONE
                expandImage.isActivated = layerItemModel.expanded

                //disabled
                mainItemVg.forEach {
                    it.isEnabled = layerItemModel.enabled
                }

                //switch
                layerSwitch.switchPosition =
                    if (layerItemModel.turnedOn) Switch3Way.SwitchPositions.END
                    else Switch3Way.SwitchPositions.START

                //slider
                transparencySlider.value = layerItemModel.transparency.toFloat()

                setDragMode(mViewModel.liveDataDragMode.value!!)

                dragImageView.setOnTouchListener { view, motionEvent ->

                    when {
                        motionEvent.actionMasked == MotionEvent.ACTION_DOWN
                                && mViewModel.liveDataDragMode.value!! ->
                            startDragListener.onStartDrag(this@LayerItemViewHolder)
                    }
                    false
                }

                //zoom
                zoom.text = "${layerItemModel.zoomMin}-${layerItemModel.zoomMax}"
            }
        }

        private fun setColoredFormatText(formatValue: Int,
                                         textView: TextView
        ) {
            SpannableString(
                context.getString(R.string.visibility_format_string, formatValue)
            ).apply {
                setSpan(
                    ForegroundColorSpan(Color.WHITE),11, length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                textView.setText(
                    this, TextView.BufferType.SPANNABLE
                )
            }
        }

        private fun setSelectionToRemove(itemModel: LayerItem,
                                         imageView: AppCompatImageView
        ) {

            itemModel.selectedToRemove = !itemModel.selectedToRemove
            setLayerIcon(itemModel, imageView)
            binding.bgTop.isSelected = !binding.bgTop.isSelected
        }

        private fun setLayerIcon(itemModel: LayerItem,
                                 imageView: AppCompatImageView
        ) {
            val mainIconId =
                if (!itemModel.selectedToRemove) {
                    context.resources.getIdentifier(
                        itemModel.layerIconResName,
                        "drawable",
                        context.packageName
                    )
                } else R.drawable.check_circle
            imageView.setImageResource(mainIconId)
            imageView.isActivated = itemModel.expanded
        }

        fun isLayerEnabled(): Boolean {
            return (currentList[adapterPosition] as LayerItem).enabled
        }

        fun setDragMode(isDragMode: Boolean
        ) {
            if (isDragMode) {
                binding.layerSwitch.visibility = View.GONE
                binding.dragImageView.visibility = View.VISIBLE

            } else {
                binding.layerSwitch.visibility = View.VISIBLE
                binding.dragImageView.visibility = View.GONE
            }
        }
    }

    override fun onViewAttachedToWindow(
        holder: RecyclerView.ViewHolder
    ) {
        if (holder is LayerItemViewHolder) {
            holder.setDragMode(mViewModel.liveDataDragMode.value!!)
            holder.binding.layerSwitch.switchPosition =
                if ((currentList[holder.adapterPosition] as LayerItem).turnedOn)
                    Switch3Way.SwitchPositions.END
                else Switch3Way.SwitchPositions.START
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            val binding: ItemLayerLayoutBinding =
                ItemLayerLayoutBinding.inflate(inflater, parent, false)
            LayerItemViewHolder(binding)
        } else {
            val binding: DividerSharedLayersBinding =
                DividerSharedLayersBinding.inflate(inflater, parent, false)
            DividerViewHolder(binding)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder,
                                  position: Int
    ) {
        if (holder is LayerItemViewHolder) holder.bindView(position)
    }

    override fun getItemViewType(position: Int
    ): Int {
        return if (currentList[position] is LayerItem) 0 else 1
    }

    override fun onItemMove(fromPosition: Int,
                            toPosition: Int
    ): Boolean {

        val itemFrom = currentList[fromPosition]
        val itemTo = currentList[toPosition]
        if (itemFrom is LayerItem && itemTo is LayerItem) {
            val idFrom = itemFrom.id
            val idTo = itemTo.id
            val checkedStateFrom = itemFrom.turnedOn
            val checkedStateTo = itemTo.turnedOn
            mViewModel.mSavedCheckedStates[itemFrom.id] = checkedStateTo
            mViewModel.mSavedCheckedStates[itemTo.id] = checkedStateFrom
            currentList[fromPosition].id = idTo
            currentList[toPosition].id = idFrom
            Collections.swap(mViewModel.liveDataLayers.value!!, fromPosition, toPosition)
            mViewModel.updateLayersOrderInDB()

        } else {
            val movedLayer = currentList[fromPosition] as LayerItem
            movedLayer.isSharedLayer = toPosition > fromPosition
            Collections.swap(mViewModel.liveDataLayers.value!!, fromPosition, toPosition)
            mViewModel.updateIsSharedLayer(movedLayer)
        }

        notifyItemMoved(fromPosition, toPosition)
        return true
    }

}