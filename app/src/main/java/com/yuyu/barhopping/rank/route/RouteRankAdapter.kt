package com.yuyu.barhopping.rank.route

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yuyu.barhopping.R
import com.yuyu.barhopping.data.NewRouteStore
import com.yuyu.barhopping.databinding.ItemRouteRankBinding
import com.yuyu.barhopping.rank.route.RouteRankAdapter.*

class RouteRankAdapter(private val onClickListener: OnClickListener): ListAdapter<NewRouteStore, RouteRankViewHolder>(DiffCallback) {

    class RouteRankViewHolder(private var binding: ItemRouteRankBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(routeStore: NewRouteStore, viewModel: RouteRankViewModel, position: Int) {
            binding.routeStore = routeStore
            binding.starImg.apply {
                if(routeStore.userLike == false) {
                    setImageResource(R.drawable.ic_baseline_star_border_24)
                } else {
                    setImageResource(R.drawable.ic_baseline_star_24)
                }
            }
            binding.starImg.setOnClickListener {
                if(routeStore.userLike == false) { // 還沒收藏
                    binding.starImg.setImageResource(R.drawable.ic_baseline_star_24)
                    viewModel.routeCollection(routeStore.id, false)
                    routeStore.userLike = true
                } else if (routeStore.userLike == true){
                    binding.starImg.setImageResource(R.drawable.ic_baseline_star_border_24)
                    viewModel.routeCollection(routeStore.id, true)
                    routeStore.userLike = false
                }
            }
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
            return oldItem.id === newItem.id
        }

        override fun areContentsTheSame(oldItem: NewRouteStore, newItem: NewRouteStore): Boolean {
            return oldItem.userLike == newItem.userLike
        }
    }

    class OnClickListener(val clickListener: (newRouteStore: NewRouteStore) -> Unit) {
        fun click(routeStore: NewRouteStore) = clickListener(routeStore)
    }
}