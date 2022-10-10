package com.yuyu.barhopping.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yuyu.barhopping.R
import com.yuyu.barhopping.data.NewBarPost
import com.yuyu.barhopping.databinding.ItemBarExploreBinding

class ExploreAdapter : ListAdapter<NewBarPost, ExploreAdapter.ExploreViewHolder>(DiffCallback) {

    class ExploreViewHolder(private var binding: ItemBarExploreBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(bar: NewBarPost, viewModel: ExploreViewModel) {
            binding.bar = bar
            binding.likeBtn.apply {
                if (bar.userLike == false) {
                    setImageResource(R.drawable.ic_baseline_favorite_border_24)
                } else {
                    setImageResource(R.drawable.ic_baseline_favorite_24)
                }
            }
            binding.likeBtn.setOnClickListener {
                if (bar.userLike == false) {
                    binding.likeBtn.setImageResource(R.drawable.ic_baseline_favorite_24)
                    viewModel.barCollection(bar.id, false)
                    bar.userLike = true
                } else if (bar.userLike == true) {
                    binding.likeBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                    viewModel.barCollection(bar.id, true)
                    bar.userLike = false
                }
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreViewHolder {
        return ExploreViewHolder(
            ItemBarExploreBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ExploreViewHolder, position: Int) {
        holder.bind(getItem(position), ExploreViewModel(null))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<NewBarPost>() {

        override fun areItemsTheSame(oldItem: NewBarPost, newItem: NewBarPost): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: NewBarPost, newItem: NewBarPost): Boolean {
            return oldItem == newItem
        }
    }
}