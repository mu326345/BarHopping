package com.yuyu.barhopping.rank.person

import android.animation.Animator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yuyu.barhopping.MainActivity
import com.yuyu.barhopping.databinding.FragmentPersonRankBinding

class PersonRankFragment : Fragment() {

    private lateinit var binding: FragmentPersonRankBinding
    private lateinit var viewModel: PersonRankViewModel
    private lateinit var adapter: PersonRankAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPersonRankBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel = ViewModelProvider(this).get(PersonRankViewModel::class.java)
        adapter = PersonRankAdapter()

        val recyclerLayout = binding.recyclerLayout
        binding.recyclerLayout.adapter = adapter
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

        viewModel.userItem.observe(
            viewLifecycleOwner, Observer {
                adapter.submitList(it)
            }
        )

        return binding.root
    }
}