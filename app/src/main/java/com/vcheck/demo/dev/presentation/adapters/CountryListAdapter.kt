package com.vcheck.demo.dev.presentation.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.databinding.CountryRowBinding
import com.vcheck.demo.dev.domain.Country
import com.vcheck.demo.dev.domain.CountryTO
import com.vcheck.demo.dev.presentation.country_stage.ChooseCountryFragment
import java.util.*
import kotlin.collections.ArrayList

class CountryListAdapter(
    private val countryList: ArrayList<CountryTO>,
    private val onCountryItemClick: OnCountryItemClick,
    private val searchCountryCallback: SearchCountryCallback
) :
    RecyclerView.Adapter<CountryListAdapter.ViewHolder>(), Filterable {

    private lateinit var binding: CountryRowBinding
    private val mainCountryList = ArrayList<CountryTO>(countryList)
    private val searchCountryList = ArrayList<CountryTO>(countryList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        binding = CountryRowBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, onCountryItemClick)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val country = mainCountryList[position]
        holder.bind(country)
    }

    override fun getItemCount(): Int = mainCountryList.size


    class ViewHolder(
        private val binding: CountryRowBinding,
        private val onCountryItemClick: OnCountryItemClick,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(country: CountryTO) {
            binding.countryName.text = country.name
            binding.flagEmoji.text = country.flag
            binding.countryItem.setOnClickListener {
                onCountryItemClick.onClick(country.code)
            }
        }
    }

    interface OnCountryItemClick {
        fun onClick(country: String)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = ArrayList<CountryTO>()
                if (constraint.isNullOrBlank() or constraint.isNullOrEmpty()) {
                    filteredList.addAll(searchCountryList)
                } else {
                    searchCountryList.forEach {
                        if (it.name.lowercase().contains(constraint.toString().lowercase())) {
                            filteredList.add(it)
                        }
                    }
                }

                val result = FilterResults()
                result.values = filteredList
                return result
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                mainCountryList.clear()
                val resultList = results?.values as ArrayList<CountryTO>
                if (resultList.isNotEmpty()) {
                    mainCountryList.addAll(resultList)
                } else {
                    searchCountryCallback.onEmptySearchResult()
                }
                notifyDataSetChanged()
            }
        }
    }
}
