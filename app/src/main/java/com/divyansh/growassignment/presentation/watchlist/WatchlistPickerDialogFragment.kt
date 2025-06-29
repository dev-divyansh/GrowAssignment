package com.divyansh.growassignment.presentation.watchlist

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.divyansh.growassignment.databinding.DialogWatchlistPickerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WatchlistPickerDialogFragment(
    private val onWatchlistPicked: (Long) -> Unit
) : DialogFragment() {
    private var _binding: DialogWatchlistPickerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WatchlistViewModel by viewModels()
    private var selectedWatchlistId: Long? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogWatchlistPickerBinding.inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Add to Watchlist")
            .setView(binding.root)
            .setNegativeButton("Cancel") { _, _ -> dismiss() }
            .setPositiveButton("Add") { _, _ ->
                val newName = binding.etNewWatchlistName.text.toString().trim()
                if (newName.isNotEmpty()) {
                    viewModel.createWatchlist(newName)
                    lifecycleScope.launch {
                        viewModel.watchlists.collectLatest { lists ->
                            val created = lists.find { it.name == newName }
                            if (created != null) {
                                onWatchlistPicked(created.id)
                                dismiss()
                                return@collectLatest
                            }
                        }
                    }
                } else if (selectedWatchlistId != null) {
                    onWatchlistPicked(selectedWatchlistId!!)
                    dismiss()
                }
            }
        lifecycleScope.launch {
            viewModel.watchlists.collectLatest { lists ->
                val names = lists.map { it.name }
                val ids = lists.map { it.id }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_single_choice, names)
                binding.lvWatchlists.adapter = adapter
                binding.lvWatchlists.setOnItemClickListener { _, _, pos, _ ->
                    selectedWatchlistId = ids[pos]
                }
            }
        }
        return builder.create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 