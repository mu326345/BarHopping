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

class BottomSheetAdapter :
    ListAdapter<SheetItem, RecyclerView.ViewHolder>(DiffCallback) {

    class RouteDetailViewHolder(private var binding: ItemSheetRouteDetailBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SheetItem.MarketNameAndState) {
            binding.item = item
            if(item.done) {
                binding.itemCardView.setCardBackgroundColor(parseColor("#808080"))
            }

            item.friends?.forEach {
                binding.imageScroll.visibility = View.VISIBLE
                val itemBinding = ItemSheetRouteFriendItemBinding.inflate(LayoutInflater.from(context))
                Glide.with(context).load(it.friendsIcon).into(itemBinding.friendsIcon)
                binding.imgList.addView(itemBinding.root)
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            viewTypeUserProgress -> RouteDetailViewHolder(
                ItemSheetRouteDetailBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), parent.context
            )

            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RouteDetailViewHolder -> {
                holder.bind((getItem(position) as SheetItem.MarketNameAndState))
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SheetItem>() {
        override fun areItemsTheSame(oldItem: SheetItem, newItem: SheetItem): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: SheetItem, newItem: SheetItem): Boolean {
            return oldItem == newItem
        }

        private const val viewTypeUserProgress = 0
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SheetItem.MarketNameAndState -> viewTypeUserProgress
        }
    }
}