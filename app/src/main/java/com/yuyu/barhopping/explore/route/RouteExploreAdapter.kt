package com.yuyu.barhopping.explore.route

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yuyu.barhopping.data.RouteCommend
import com.yuyu.barhopping.databinding.ItemRouteExploreBinding

class RouteExploreAdapter: ListAdapter<RouteCommend, RouteExploreAdapter.RouteExploreViewHolder>(DiffCallback) {

    class RouteExploreViewHolder(private var binding: ItemRouteExploreBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(routeCommend: RouteCommend) {
            binding.routeCommend = routeCommend
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteExploreViewHolder {
        return RouteExploreViewHolder(ItemRouteExploreBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RouteExploreViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    companion object DiffCallback : DiffUtil.ItemCallback<RouteCommend>() {
        override fun areItemsTheSame(oldItem: RouteCommend, newItem: RouteCommend): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: RouteCommend, newItem: RouteCommend): Boolean {
            return oldItem == newItem
        }
    }
}