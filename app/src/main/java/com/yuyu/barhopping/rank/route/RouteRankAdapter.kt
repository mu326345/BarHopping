package com.yuyu.barhopping.rank.route

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yuyu.barhopping.data.NewRouteStore
import com.yuyu.barhopping.data.RouteStore
import com.yuyu.barhopping.databinding.ItemRouteRankBinding
import com.yuyu.barhopping.rank.route.RouteRankAdapter.*

class RouteRankAdapter(val onClickListener: OnClickListener): ListAdapter<NewRouteStore, RouteRankViewHolder>(DiffCallback) {

    class RouteRankViewHolder(private var binding: ItemRouteRankBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(routeStore: NewRouteStore, viewModel: RouteRankViewModel, position: Int) {
            binding.itemPosition = position
            binding.routeStore = routeStore
            binding.viewModel = viewModel
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
        holder.bind(item, RouteRankViewModel(), position)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<NewRouteStore>() {
        override fun areItemsTheSame(oldItem: NewRouteStore, newItem: NewRouteStore): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: NewRouteStore, newItem: NewRouteStore): Boolean {
            return oldItem == newItem
        }
    }

    class OnClickListener(val clickListener: (newRouteStore: NewRouteStore) -> Unit) {
        fun click(routeStore: NewRouteStore) = clickListener(routeStore)
    }
}