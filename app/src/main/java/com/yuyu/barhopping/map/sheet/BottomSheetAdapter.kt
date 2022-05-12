package com.yuyu.barhopping.map.sheet

import android.content.Context
import android.graphics.Color.parseColor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yuyu.barhopping.data.SheetItem
import com.yuyu.barhopping.databinding.ItemSheetRouteDetailBinding
import com.yuyu.barhopping.databinding.ItemSheetRouteFriendItemBinding
import com.yuyu.barhopping.map.MapViewModel

class BottomSheetAdapter :
    ListAdapter<SheetItem, BottomSheetAdapter.RouteDetailViewHolder>(DiffCallback) {

    class RouteDetailViewHolder(private var binding: ItemSheetRouteDetailBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SheetItem) {
            binding.item = item
            if(item.done) {
                binding.itemCardView.setCardBackgroundColor(parseColor("#808080"))
            }

            item.users.forEach {
                binding.imageScroll.visibility = View.VISIBLE
                val itemBinding = ItemSheetRouteFriendItemBinding.inflate(LayoutInflater.from(context))
                Glide.with(context).load(it.icon).into(itemBinding.friendsIcon)
                binding.imgList.addView(itemBinding.root)
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteDetailViewHolder {
        return RouteDetailViewHolder(
            ItemSheetRouteDetailBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), parent.context
        )
    }

    override fun onBindViewHolder(holder: RouteDetailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SheetItem>() {
        override fun areItemsTheSame(oldItem: SheetItem, newItem: SheetItem): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: SheetItem, newItem: SheetItem): Boolean {
            return oldItem == newItem
        }
    }

}