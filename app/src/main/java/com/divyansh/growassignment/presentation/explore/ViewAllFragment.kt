package com.divyansh.growassignment.presentation.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.divyansh.growassignment.databinding.FragmentViewAllBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ViewAllFragment : Fragment() {
    private var _binding: FragmentViewAllBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExploreViewModel by viewModels()
    private lateinit var adapter: TopMoverAdapter
    private var type: String = "gainer"
    private var currentPage = 0
    private val pageSize = 20
    private var fullList: List<com.divyansh.growassignment.data.models.TopMoverDto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getString("type") ?: "gainer"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentViewAllBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = TopMoverAdapter { _ -> /* handle navigation if needed */ }
        binding.recyclerAll.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerAll.adapter = adapter

        lifecycleScope.launch {
            viewModel.exploreState.collectLatest { state ->
                if (state is ExploreUiState.Success) {
                    fullList = if (type == "gainer") state.gainers else state.losers
                    showPage(0)
                }
            }
        }

        binding.btnLoadMore.setOnClickListener {
            showPage(currentPage + 1)
        }
    }

    private fun showPage(page: Int) {
        val from = page * pageSize
        val to = minOf(from + pageSize, fullList.size)
        if (from < fullList.size) {
            adapter.submitList(fullList.subList(0, to))
            currentPage = page
            binding.btnLoadMore.visibility = if (to < fullList.size) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 