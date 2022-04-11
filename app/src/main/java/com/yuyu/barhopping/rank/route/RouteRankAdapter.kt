package com.yuyu.barhopping.rank.route

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yuyu.barhopping.data.Route
import com.yuyu.barhopping.databinding.ItemRouteRankBinding
import com.yuyu.barhopping.rank.route.RouteRankAdapter.*

class RouteRankAdapter: ListAdapter<Route, RouteRankViewHolder>(DiffCallback) {

    class RouteRankViewHolder(private var binding: ItemRouteRankBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(route: Route) {
            binding.route = route
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteRankViewHolder {
        return RouteRankViewHolder(ItemRouteRankBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RouteRankViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Route>() {
        override fun areItemsTheSame(oldItem: Route, newItem: Route): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Route, newItem: Route): Boolean {
            return oldItem == newItem
        }
    }
}