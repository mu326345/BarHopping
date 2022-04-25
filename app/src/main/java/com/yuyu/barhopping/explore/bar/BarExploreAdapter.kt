package com.yuyu.barhopping.explore.bar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yuyu.barhopping.data.Bar
import com.yuyu.barhopping.data.RouteCommend
import com.yuyu.barhopping.databinding.ItemBarExploreBinding

class BarExploreAdapter: ListAdapter<Bar, BarExploreAdapter.BarExploreViewHolder>(DiffCallback) {

    class BarExploreViewHolder(private var binding: ItemBarExploreBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(bar: Bar) {
            binding.bar = bar
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarExploreViewHolder {
        return BarExploreViewHolder(ItemBarExploreBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BarExploreViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Bar>() {

        override fun areItemsTheSame(oldItem: Bar, newItem: Bar): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: Bar, newItem: Bar): Boolean {
            return oldItem == newItem
        }
    }
}