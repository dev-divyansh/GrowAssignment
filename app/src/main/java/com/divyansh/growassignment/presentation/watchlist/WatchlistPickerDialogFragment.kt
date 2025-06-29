package com.divyansh.growassignment.presentation.watchlist

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.divyansh.growassignment.R
import com.divyansh.growassignment.databinding.DialogWatchlistPickerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.util.Log
import com.divyansh.growassignment.data.models.WatchlistEntity

@AndroidEntryPoint
class WatchlistPickerDialogFragment(
    private val onWatchlistPicked: (Long) -> Unit
) : DialogFragment() {
    private var _binding: DialogWatchlistPickerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WatchlistViewModel by viewModels()
    private lateinit var adapter: WatchlistDialogAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogWatchlistPickerBinding.inflate(LayoutInflater.from(context))
        
        // Initialize adapter
        adapter = WatchlistDialogAdapter(requireContext(), emptyList())
        binding.lvWatchlists.adapter = adapter
        
        val builder = AlertDialog.Builder(requireContext(), R.style.Theme_GrowAssignment)
            .setView(binding.root)
            .setNegativeButton("Cancel") { _, _ -> dismiss() }
            .setPositiveButton("Add") { _, _ ->
                handleAddAction()
            }
        
        // Observe watchlists
        lifecycleScope.launch {
            viewModel.watchlists.collectLatest { lists ->
                Log.d("WatchlistDialog", "Watchlists updated: ${lists.size} items")
                // Convert WatchlistWithStocks to WatchlistEntity
                val watchlistEntities = lists.map { WatchlistEntity(it.id, it.name, it.createdAt) }
                adapter.updateData(watchlistEntities)
                
                // Show/hide no watchlists message
                if (lists.isEmpty()) {
                    binding.tvNoWatchlists.visibility = View.VISIBLE
                    binding.lvWatchlists.visibility = View.GONE
                } else {
                    binding.tvNoWatchlists.visibility = View.GONE
                    binding.lvWatchlists.visibility = View.VISIBLE
                }
            }
        }
        
        return builder.create()
    }
    
    private fun handleAddAction() {
        val newName = binding.etNewWatchlistName.text.toString().trim()
        
        if (newName.isNotEmpty()) {
            // Create new watchlist
            lifecycleScope.launch {
                val watchlistId = viewModel.createWatchlist(newName)
                Log.d("WatchlistDialog", "Created new watchlist: $newName with ID: $watchlistId")
                onWatchlistPicked(watchlistId)
                dismiss()
            }
        } else {
            // Add to selected existing watchlist
            val selectedWatchlistId = adapter.getSelectedWatchlistId()
            if (selectedWatchlistId != null) {
                Log.d("WatchlistDialog", "Adding to existing watchlist: $selectedWatchlistId")
                onWatchlistPicked(selectedWatchlistId)
                dismiss()
            } else {
                Log.w("WatchlistDialog", "No watchlist selected and no new name provided")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 