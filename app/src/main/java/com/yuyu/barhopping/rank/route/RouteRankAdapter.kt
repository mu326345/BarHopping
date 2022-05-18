package com.yuyu.barhopping.rank.route

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yuyu.barhopping.data.RouteStore
import com.yuyu.barhopping.databinding.ItemRouteRankBinding
import com.yuyu.barhopping.rank.route.RouteRankAdapter.*

class RouteRankAdapter(val onClickListener: OnClickListener): ListAdapter<RouteStore, RouteRankViewHolder>(DiffCallback) {

    class RouteRankViewHolder(private var binding: ItemRouteRankBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(routeStore: RouteStore) {
            binding.routeStore = routeStore
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteRankViewHolder {
        return RouteRankViewHolder(ItemRouteRankBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RouteRankViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.click(item)
        }
        holder.bind(item)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<RouteStore>() {
        override fun areItemsTheSame(oldItem: RouteStore, newItem: RouteStore): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: RouteStore, newItem: RouteStore): Boolean {
            return oldItem == newItem
        }
    }

    class OnClickListener(val clickListener: (routeStore: RouteStore) -> Unit) {
        fun click(routeStore: RouteStore) = clickListener(routeStore)
    }
}