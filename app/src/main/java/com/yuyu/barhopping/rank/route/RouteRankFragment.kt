package com.yuyu.barhopping.rank.route

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yuyu.barhopping.MainActivity
import com.yuyu.barhopping.databinding.FragmentRouteRankBinding
import com.yuyu.barhopping.map.MapFragmentDirections

class RouteRankFragment : Fragment() {

    private lateinit var binding: FragmentRouteRankBinding
    private val viewModel by viewModels<RouteRankViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRouteRankBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        val recyclerLayout = binding.recyclerLayout
        recyclerLayout.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
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

        viewModel.navigateToProgress()

        binding.recyclerLayout.adapter = RouteRankAdapter(
            RouteRankAdapter.OnClickListener {
                viewModel.navigateToDetail(it)
            })

        viewModel.newRouteItem.observe(viewLifecycleOwner) {
            (binding.recyclerLayout.adapter as RouteRankAdapter).submitList(it)
            viewModel.onNavigateProgress()
        }

        viewModel.navigateToDetail.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(
                    RouteRankFragmentDirections.navigateToRouteDetailFragment(
                        it
                    )
                )
                viewModel.onDetailNavigated()
            }
        }

        viewModel.navigateToProgress.observe(viewLifecycleOwner) { isProgressBar ->
            if (isProgressBar) {
                findNavController().navigate(MapFragmentDirections.navigateToProgressBarDialogFragment())

            } else {
                findNavController().popBackStack()
            }
        }

        return binding.root
    }
}