package com.yuyu.barhopping.rank.person

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yuyu.barhopping.data.User
import com.yuyu.barhopping.databinding.ItemPersonRankBinding

class PersonRankAdapter: ListAdapter<User, PersonRankAdapter.PersonRankViewHolder>(DiffCallback) {

    class PersonRankViewHolder(private var binding: ItemPersonRankBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.user = user
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonRankViewHolder {
        return PersonRankViewHolder(ItemPersonRankBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: PersonRankViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}