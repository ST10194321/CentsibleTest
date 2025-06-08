package com.st10194321.centsibletest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.st10194321.centsibletest.databinding.ItemTipBinding

class TipAdapter(private val tips: List<Tip>) :
    RecyclerView.Adapter<TipAdapter.VH>() {

    inner class VH(private val binding: ItemTipBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(tip: Tip) {
            binding.ivTipIcon.setImageResource(tip.iconRes)
            binding.tvTipText.text = tip.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTipBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(tips[position])

    override fun getItemCount() = tips.size
}
