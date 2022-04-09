package com.vcheck.demo.dev.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vcheck.demo.dev.databinding.DocInfoRowBinding
import com.vcheck.demo.dev.domain.DocField

class CheckInfoAdapter(private val documentInfoList: ArrayList<DocField>) :
    RecyclerView.Adapter<CheckInfoAdapter.ViewHolder>() {

    private lateinit var binding: DocInfoRowBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        binding = DocInfoRowBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val documentInfo = documentInfoList[position]
        holder.bind(documentInfo)
    }

    override fun getItemCount(): Int = documentInfoList.size

    class ViewHolder(private val binding: DocInfoRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(documentInfo: DocField) {
            binding.title.text = documentInfo.name
            binding.infoField.setText(documentInfo.type)
        }
    }
}