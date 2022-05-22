package com.yuyu.barhopping.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yuyu.barhopping.data.BarPost
import com.yuyu.barhopping.databinding.ItemBarExploreBinding

class ExploreAdapter: ListAdapter<BarPost, ExploreAdapter.ExploreViewHolder>(DiffCallback) {

    class ExploreViewHolder(private var binding: ItemBarExploreBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(bar: BarPost) {
            binding.bar = bar
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreViewHolder {
        return ExploreViewHolder(ItemBarExploreBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ExploreViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<BarPost>() {

        override fun areItemsTheSame(oldItem: BarPost, newItem: BarPost): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: BarPost, newItem: BarPost): Boolean {
            return oldItem == newItem
        }
    }
}