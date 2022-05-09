package com.gmail.zajcevserg.maptestapp.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.DisplayMetrics
import android.view.LayoutInflater

import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.core.view.doOnLayout
import androidx.core.view.forEach
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gmail.zajcevserg.maptestapp.R
import com.gmail.zajcevserg.maptestapp.databinding.ItemLayerLayoutBinding
import com.gmail.zajcevserg.maptestapp.model.database.LayerItem
import com.gmail.zajcevserg.maptestapp.ui.activity.log
import com.gmail.zajcevserg.maptestapp.viewmodel.LayersVM

import com.google.android.material.slider.Slider
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class LayersAdapter(val mViewModel: LayersVM, val context: Context
) : ListAdapter<LayerItem, LayersAdapter.LayerItemViewHolder>(LayersDiff) {

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
                    mViewModel.activateLayer(idToUpdate, checked)
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
                val mainIconId =
                    context.resources.getIdentifier(
                        layerItemModel.layerIconResName, "drawable", context.packageName)
                layerIcon.setImageResource(mainIconId)
                layerIcon.isActivated = layerItemModel.expanded


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

            }
        }

        fun isLayerEnabled(): Boolean {
            return currentList[adapterPosition].enabled
        }


        fun setDragMode(isDragMode: Boolean) {

            if (isDragMode) {
                binding.switchActivate.visibility = View.INVISIBLE
                binding.dragImageView.visibility = View.VISIBLE
            } else {
                binding.switchActivate.visibility = View.VISIBLE
                binding.dragImageView.visibility = View.INVISIBLE
            }
        }


        /*private fun setExpanded(binding: ItemLayerLayoutBinding, expanded: Boolean) {

            with (binding) {

                if (expanded) {
                    expandImage.isSelected = true
                    layerIcon.isSelected = true
                    bottomPanelGroup.visibility = View.VISIBLE
                    layerTitle.typeface = Typeface.DEFAULT_BOLD
                    val color = ResourcesCompat.getColor(context.resources, R.color.teal_200, context.theme)
                    layerTitle.setTextColor(color)
                } else {
                    expandImage.isSelected = false
                    layerIcon.isSelected = false
                    bottomPanelGroup.visibility = View.GONE
                    layerTitle.typeface = Typeface.DEFAULT
                    layerTitle.setTextColor(Color.LTGRAY)
                }
            }
        }*/
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LayerItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemLayerLayoutBinding =
            ItemLayerLayoutBinding.inflate(inflater, parent, false)
        return LayerItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LayerItemViewHolder, position: Int) {
        log("onBindViewHolder $position")
        holder.bindView(position)

    }

    override fun getItemCount(): Int {
        return currentList.size
    }
}