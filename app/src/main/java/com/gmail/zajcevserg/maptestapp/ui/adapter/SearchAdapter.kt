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
import com.gmail.zajcevserg.maptestapp.databinding.ItemSearchLayoutBinding
import com.gmail.zajcevserg.maptestapp.model.database.LayerItem
import com.gmail.zajcevserg.maptestapp.ui.activity.log
import com.gmail.zajcevserg.maptestapp.ui.custom.OnItemMoveListener
import com.gmail.zajcevserg.maptestapp.ui.custom.OnStartDragListener
import com.gmail.zajcevserg.maptestapp.ui.custom.Switch3Way
import com.gmail.zajcevserg.maptestapp.viewmodel.LayersVM
import com.google.android.material.slider.Slider
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class SearchAdapter(val mViewModel: LayersVM,
                    val context: Context
) : ListAdapter<LayerItem, SearchAdapter.ItemSearchViewHolder>(LayersDiff) {

    private var mOnSearchItemClickListener: ((id: Int) -> Unit)? = null

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
    fun setOnSearchItemClickListener(listener: (id: Int) -> Unit) {
        this.mOnSearchItemClickListener = listener
    }


    inner class ItemSearchViewHolder(
        val binding: ItemSearchLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                mOnSearchItemClickListener?.invoke(currentList[adapterPosition].id)
            }
        }

        fun bindView(position: Int) {
            binding.itemTitle.text = currentList[position].title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemSearchViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemSearchLayoutBinding =
            ItemSearchLayoutBinding.inflate(inflater, parent, false)
        return ItemSearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemSearchViewHolder, position: Int) {
        holder.bindView(position)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }



}