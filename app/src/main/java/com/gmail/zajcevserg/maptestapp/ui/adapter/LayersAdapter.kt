package com.gmail.zajcevserg.maptestapp.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.doOnLayout
import androidx.core.view.forEach
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gmail.zajcevserg.maptestapp.R
import com.gmail.zajcevserg.maptestapp.databinding.ItemLayerLayoutBinding
import com.gmail.zajcevserg.maptestapp.model.database.LayerItem
import com.gmail.zajcevserg.maptestapp.ui.custom.OnItemMoveListener
import com.gmail.zajcevserg.maptestapp.ui.custom.OnStartDragListener
import com.gmail.zajcevserg.maptestapp.viewmodel.LayersVM
import com.google.android.material.slider.Slider
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class LayersAdapter(val mViewModel: LayersVM,
                    val context: Context,
                    val startDragListener: OnStartDragListener
) : ListAdapter<LayerItem, LayersAdapter.LayerItemViewHolder>(LayersDiff),
    OnItemMoveListener {

    companion object LayersDiff : DiffUtil.ItemCallback<LayerItem>() {

        override fun areContentsTheSame(
            oldItem: LayerItem,
            newItem: LayerItem
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(
            oldItem: LayerItem,
            newItem: LayerItem
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    inner class LayerItemViewHolder(
        val binding: ItemLayerLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {


        private fun convertPixelToDip(px: Int): Int {
            return run {
                val dMetrics = context.resources.displayMetrics
                px / (dMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
            }
        }
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

                val currentDate = Date()
                val dateFormat: DateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val dateText = dateFormat.format(currentDate)
                syncDateTextView.text = context.getString(R.string.sync_date,dateText)

                binding.transparencyTextView.doOnLayout {
                    it as TextView
                    it.text = context.getString(R.string.transparency,
                        currentList[adapterPosition].transparency)
                }

                transparencySlider.addOnChangeListener { slider, value, fromUser ->
                    val currentTransparencySliderValue = context.getString(R.string.transparency, value.toInt())
                    transparencyTextView.text = currentTransparencySliderValue
                }

                switchActivate.setOnCheckedChangeListener { view, checked ->
                    val idToUpdate = try {
                        currentList[adapterPosition].id
                    } catch(exception: IndexOutOfBoundsException) {
                        return@setOnCheckedChangeListener
                    }
                    mViewModel.turnOn(idToUpdate, checked)
                }

                val mListener = View.OnClickListener {
                    val idToUpdate = try {
                        currentList[adapterPosition].id
                    } catch(exception: IndexOutOfBoundsException) {
                        return@OnClickListener
                    }

                    val itemToCollapse = currentList.find {
                        it.id != idToUpdate && it.expanded
                    }

                    val indexToCollapse = currentList.indexOf(itemToCollapse)
                    notifyItemChanged(indexToCollapse)
                    notifyItemChanged(adapterPosition)
                    mViewModel.expandLayer(idToUpdate)

                }

                layerTitle.setOnClickListener(mListener)
                expandImage.setOnClickListener(mListener)

                goToLayerCenterButton.setOnClickListener {
                    mViewModel.liveDataMapInteraction.value = adapterPosition
                    mViewModel.liveDataMapInteraction.value = null
                }
            }
        }

        fun bindView(position: Int) {

            with (binding) {
                val layerItemModel = currentList[position]
                motionLayer.translationX = 0f
                backgroundButtonsLayer.scaleX = 0f
                backgroundButtonsLayer.scaleY = 0f

                //icon
                setLayerIcon(layerItemModel, layerIcon)

                binding.bgTop.isSelected = layerItemModel.selectedToRemove

                //long click to select
                binding.layerIcon.setOnLongClickListener {
                    setSelectionToRemove(layerItemModel, layerIcon)
                    true
                }

                binding.layerIcon.setOnClickListener {

                    val isSelectionMode =
                        mViewModel.liveDataLayers.value?.any {
                        it.selectedToRemove
                    }!!

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
                motionLayout.forEach {
                    it.isEnabled = layerItemModel.enabled
                }

                //switch
                switchActivate.isChecked = layerItemModel.turnedOn

                //slider
                transparencySlider.value = layerItemModel.transparency.toFloat()

                setDragMode(mViewModel.liveDataDragMode.value!!)

                dragImageView.setOnTouchListener { view, motionEvent ->
                    if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN
                        && mViewModel.liveDataDragMode.value!!) {
                        startDragListener.onStartDrag(this@LayerItemViewHolder)
                    }
                    false
                }


            }
        }

        private fun setSelectionToRemove(itemModel: LayerItem,
                                         imageView: AppCompatImageView) {
            //itemModel.selectedToRemove = !itemModel.selectedToRemove
            mViewModel.liveDataLayers.value?.forEach {
                if (it.id == itemModel.id) it.selectedToRemove = !it.selectedToRemove
            }
            setLayerIcon(itemModel, imageView)
            binding.bgTop.isSelected = !binding.bgTop.isSelected
        }

        private fun setLayerIcon(itemModel: LayerItem, imageView: AppCompatImageView) {
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
            return currentList[adapterPosition].enabled
        }

        fun setDragMode(isDragMode: Boolean) {
            //log("pos $adapterPosition $isDragMode")
            if (isDragMode) {
                binding.switchActivate.visibility = View.INVISIBLE
                binding.dragImageView.visibility = View.VISIBLE
            } else {
                binding.switchActivate.visibility = View.VISIBLE
                binding.dragImageView.visibility = View.INVISIBLE
            }
        }
    }

    override fun onViewAttachedToWindow(holder: LayerItemViewHolder) {
        holder.setDragMode(mViewModel.liveDataDragMode.value!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LayerItemViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemLayerLayoutBinding =
            ItemLayerLayoutBinding.inflate(inflater, parent, false)
        return LayerItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LayerItemViewHolder, position: Int) {
        //log("onBindViewHolder $position")
        holder.bindView(position)

    }

    override fun getItemCount(): Int {
        return currentList.size
    }



    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val layers = mViewModel.liveDataLayers.value!!
        Collections.swap(layers, fromPosition, toPosition)
        mViewModel.liveDataLayers.value = layers
        notifyItemMoved(fromPosition, toPosition)
        return true
    }


}