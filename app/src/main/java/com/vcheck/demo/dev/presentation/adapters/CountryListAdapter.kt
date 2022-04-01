package com.vcheck.demo.dev.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.vcheck.demo.dev.databinding.CountryRowBinding
import com.vcheck.demo.dev.models.Country


class CountryListAdapter(private val countryList: ArrayList<Country>) :
    RecyclerView.Adapter<CountryListAdapter.ViewHolder>() {

    private lateinit var binding: CountryRowBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        binding = CountryRowBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val country = countryList[position]
        holder.bind(country)
    }

    override fun getItemCount(): Int = countryList.size


    class ViewHolder(private val binding: CountryRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(country: Country) {

            binding.countryName.text = country.name
            binding.flagImage.setImageResource(country.flag)

            itemView.setOnClickListener {
                Snackbar.make(itemView, country.name, Snackbar.LENGTH_LONG).show()
            }
        }
    }

}
