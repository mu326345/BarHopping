package com.yuyu.barhopping.explore

import android.animation.Animator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.yuyu.barhopping.Application
import com.yuyu.barhopping.MainActivity
import com.yuyu.barhopping.R
import com.yuyu.barhopping.databinding.FragmentExploreBinding
import com.yuyu.barhopping.factory.ViewModelFactory


class ExploreFragment : Fragment() {

    private lateinit var binding: FragmentExploreBinding
    private lateinit var adapter: ExploreAdapter
    private val viewModel by viewModels<ExploreViewModel> {
        ViewModelFactory((context?.applicationContext as Application).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentExploreBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        adapter = ExploreAdapter()

        val recyclerLayout = binding.recyclerLayout
        binding.recyclerLayout.adapter = adapter
        recyclerLayout.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerLayout.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var canHide = true
            var canShow = true
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && canHide) {
                    // scroll down
                    (activity as MainActivity).animateHideBottomNav(
                        object : Animator.AnimatorListener {
                            override fun onAnimationStart(p0: Animator?) {
//                            TODO("Not yet implemented")
                            }

                            override fun onAnimationEnd(p0: Animator?) {
                                canShow = true
                            }

                            override fun onAnimationCancel(p0: Animator?) {
//                            TODO("Not yet implemented")
                            }

                            override fun onAnimationRepeat(p0: Animator?) {
//                            TODO("Not yet implemented")
                            }
                        })
                    canHide = false

                } else if (dy < 0 && canShow) {
                    (activity as MainActivity).animateShowBottomNav(
                        object : Animator.AnimatorListener {
                            override fun onAnimationStart(p0: Animator?) {
//                            TODO("Not yet implemented")
                            }

                            override fun onAnimationEnd(p0: Animator?) {
                                canHide = true
                            }

                            override fun onAnimationCancel(p0: Animator?) {
//                            TODO("Not yet implemented")
                            }

                            override fun onAnimationRepeat(p0: Animator?) {
//                            TODO("Not yet implemented")
                            }
                        })
                    canShow = false
                }
            }
        })

        binding.marqueeTv.isSelected = true

        binding.clusterMapBtn.setOnClickListener {
            findNavController().navigate(ExploreFragmentDirections.navigateToClusterFragment())
        }

//        viewModel.barItem.observe(viewLifecycleOwner) {
//            adapter.submitList(it)
//        }

        viewModel.newBarItem.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        return binding.root
    }
}