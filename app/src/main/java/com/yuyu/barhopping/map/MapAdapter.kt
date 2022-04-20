package com.yuyu.barhopping.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yuyu.barhopping.databinding.ItemStep1Binding
import com.yuyu.barhopping.databinding.ItemStep2Binding
import com.yuyu.barhopping.databinding.ItemStep3Binding

class MapAdapter(
    val viewModel: MapViewModel,
    private val onClickListener: View.OnClickListener,
    val onFocusChangeListener: View.OnFocusChangeListener
) : ListAdapter<StepTypeFilter, RecyclerView.ViewHolder>(DiffCallback) {

    class Step1ViewHolder(private var binding: ItemStep1Binding) :
        RecyclerView.ViewHolder(binding.root), LifecycleOwner {

        fun bind(
            step1: String,
            viewModel: MapViewModel,
            onClickListener: View.OnClickListener,
            onFocusChangeListener: View.OnFocusChangeListener
        ) {
            binding.lifecycleOwner = this
            binding.step1 = step1
            binding.viewModel = viewModel
            binding.nextStepBtn.setOnClickListener(onClickListener)
            binding.destinationEdit.onFocusChangeListener = onFocusChangeListener
            binding.executePendingBindings()
        }

        private val lifecycleRegistry = LifecycleRegistry(this)
        init {
            lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        }
        fun onAttach() {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
        }
        fun onDetach() {
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }
        override fun getLifecycle(): Lifecycle {
            return lifecycleRegistry
        }
    }

    class Step2ViewHolder(private var binding: ItemStep2Binding) :
        RecyclerView.ViewHolder(binding.root), LifecycleOwner {

        fun bind(
            step2: String,
            viewModel: MapViewModel,
            onClickListener: View.OnClickListener,
            onFocusChangeListener: View.OnFocusChangeListener
        ) {
            binding.lifecycleOwner = this
            binding.step2 = step2
            binding.viewModel = viewModel
            binding.nextStepBtn2.setOnClickListener(onClickListener)
            binding.previousStepBtn2.setOnClickListener(onClickListener)
            binding.locationEdit.onFocusChangeListener = onFocusChangeListener
            binding.step2DestinationEdit.onFocusChangeListener = onFocusChangeListener
            binding.executePendingBindings()
        }

        private val lifecycleRegistry = LifecycleRegistry(this)
        init {
            lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        }
        fun onAttach() {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
        }
        fun onDetach() {
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }
        override fun getLifecycle(): Lifecycle {
            return lifecycleRegistry
        }
    }

    class Step3ViewHolder(private var binding: ItemStep3Binding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            step3: String,
            onClickListener: View.OnClickListener
        ) {
            binding.step3 = step3
            binding.startGameBtn.setOnClickListener(onClickListener)
            binding.previousStepBtn3.setOnClickListener(onClickListener)
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            StepTypeFilter.STEP1.index -> Step1ViewHolder(
                ItemStep1Binding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            StepTypeFilter.STEP2.index -> Step2ViewHolder(
                ItemStep2Binding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            StepTypeFilter.STEP3.index -> Step3ViewHolder(
                ItemStep3Binding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is Step1ViewHolder -> {
                holder.bind(
                    getItem(position).value,
                    viewModel,
                    onClickListener,
                    onFocusChangeListener
                )
            }
            is Step2ViewHolder -> {
                holder.bind(
                    getItem(position).value,
                    viewModel,
                    onClickListener,
                    onFocusChangeListener
                )
            }
            is Step3ViewHolder -> {
                holder.bind(getItem(position).value, onClickListener)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<StepTypeFilter>() {
        override fun areItemsTheSame(oldItem: StepTypeFilter, newItem: StepTypeFilter): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: StepTypeFilter, newItem: StepTypeFilter): Boolean {
            return oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        when (getItem(position)) {
            StepTypeFilter.STEP1 -> {
                return StepTypeFilter.STEP1.index
            }
            StepTypeFilter.STEP2 -> {
                return StepTypeFilter.STEP2.index
            }
            StepTypeFilter.STEP3 -> {
                return StepTypeFilter.STEP3.index
            }
        }
    }

    /**
     * It for [LifecycleRegistry] change [onViewAttachedToWindow]
     */
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        when (holder) {
            is Step1ViewHolder -> holder.onAttach()
            is Step2ViewHolder -> holder.onAttach()
        }
    }

    /**
     * It for [LifecycleRegistry] change [onViewDetachedFromWindow]
     */
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        when (holder) {
            is Step1ViewHolder -> holder.onDetach()
            is Step2ViewHolder -> holder.onDetach()
        }
    }
}

