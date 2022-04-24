package com.yuyu.barhopping.map.sheet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yuyu.barhopping.databinding.ItemSheetRouteDetailBinding

class BottomSheetAdapter :
    ListAdapter<String, BottomSheetAdapter.RouteDetailViewHolder>(DiffCallback) {

    class RouteDetailViewHolder(private var binding: ItemSheetRouteDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(marketName: String) {
            binding.marketName = marketName
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteDetailViewHolder {
        return RouteDetailViewHolder(ItemSheetRouteDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RouteDetailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}