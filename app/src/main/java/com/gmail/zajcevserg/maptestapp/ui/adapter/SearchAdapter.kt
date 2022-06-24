package com.gmail.zajcevserg.maptestapp.ui.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.gmail.zajcevserg.maptestapp.databinding.ItemSearchLayoutBinding

import com.gmail.zajcevserg.maptestapp.model.database.LayerObject



class SearchAdapter(val context: Context
) : ListAdapter<LayerObject, SearchAdapter.ItemSearchViewHolder>(LayersDiff) {

    private var mOnSearchItemClickListener: ((id: Int) -> Unit)? = null

    companion object LayersDiff : DiffUtil.ItemCallback<LayerObject>() {

        override fun areContentsTheSame(
            oldItem: LayerObject,
            newItem: LayerObject
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(
            oldItem: LayerObject,
            newItem: LayerObject
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
                mOnSearchItemClickListener?.invoke(adapterPosition)
            }
        }

        fun bindView(position: Int) {
            binding.itemTitle.text = currentList[position].objectName
            setIcon(currentList[adapterPosition], binding.searchResultIcon)
        }

        private fun setIcon(itemModel: LayerObject, imageView: AppCompatImageView) {
            val mainIconId = context.resources.getIdentifier(
                itemModel.iconResName,
                "drawable",
                context.packageName
            )
            imageView.setImageResource(mainIconId)
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