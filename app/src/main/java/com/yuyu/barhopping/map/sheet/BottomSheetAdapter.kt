package com.yuyu.barhopping.map.sheet

import android.content.Context
import android.graphics.Color.parseColor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yuyu.barhopping.R
import com.yuyu.barhopping.UserManager
import com.yuyu.barhopping.data.SheetItem
import com.yuyu.barhopping.databinding.ItemSheetRouteDetailBinding
import com.yuyu.barhopping.databinding.ItemSheetRouteFriendItemBinding
import com.yuyu.barhopping.map.MapViewModel
import com.yuyu.barhopping.util.getMarketType

class BottomSheetAdapter :
    ListAdapter<SheetItem, BottomSheetAdapter.RouteDetailViewHolder>(DiffCallback) {

    class RouteDetailViewHolder(private var binding: ItemSheetRouteDetailBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SheetItem, position: Int) {
            binding.imgList.removeAllViews()
            binding.item = item
            if (item.count == position) {
                binding.imageScroll.visibility = View.VISIBLE
                val itemBinding = ItemSheetRouteFriendItemBinding.inflate(LayoutInflater.from(context))
                Glide.with(context).load(UserManager.user?.icon).into(itemBinding.friendsIcon)
                binding.imgList.addView(itemBinding.root)
            }
            binding.itemCardView.apply {
                if(item.done) {
                    setCardBackgroundColor(parseColor("#F2C867")) // e_huang
                } else {
                    setCardBackgroundColor(parseColor("#FFFFFFFF")) // white
                }
            }
            item.partners.forEach {
                binding.imageScroll.visibility = View.VISIBLE
                val itemBinding = ItemSheetRouteFriendItemBinding.inflate(LayoutInflater.from(context))
                Glide.with(context).load(it.imageUrl).into(itemBinding.friendsIcon)
                binding.imgList.addView(itemBinding.root)
            }

            val type = item.name?.getMarketType()
            val resId = when(type) {
                1 -> R.drawable.seven_logo_2
                2 -> R.drawable.family_logo_2
                3 -> R.drawable.hi_life_logo_2
                4 -> R.drawable.ok_logo_2
                else -> {
                    R.drawable.seven_logo_2
                    Log.v("yy", "market unknown type")
                }
            }
            binding.marketIcon.setImageDrawable(context.resources.getDrawable(resId))
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
        holder.bind(getItem(position), position)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SheetItem>() {
        override fun areItemsTheSame(oldItem: SheetItem, newItem: SheetItem): Boolean {
            return oldItem.name === newItem.name
        }

        override fun areContentsTheSame(oldItem: SheetItem, newItem: SheetItem): Boolean {
            return oldItem.done == newItem.done
        }
    }
}