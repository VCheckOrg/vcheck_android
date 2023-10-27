package com.vcheck.sdk.core.presentation.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.CountryBlockedRowBinding
import com.vcheck.sdk.core.databinding.CountryRowBinding
import com.vcheck.sdk.core.domain.CountryTO
import kotlin.collections.ArrayList

class CountryListAdapter(
    private val countryList: List<CountryTO>,
    private val onCountryItemClick: OnCountryItemClick,
    private val searchCountryCallback: SearchCountryCallback
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private lateinit var availableCountryBinding: CountryRowBinding
    private lateinit var unavailableCountryRowBinding: CountryBlockedRowBinding
    private val mainCountryList = ArrayList<CountryTO>(countryList)
    private val searchCountryList = ArrayList<CountryTO>(countryList)

    override fun getItemViewType(position: Int): Int {
        return if (mainCountryList[position].isBlocked) {
            0
        } else {
            1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)

        return if (viewType == 0) {
            unavailableCountryRowBinding = CountryBlockedRowBinding.inflate(layoutInflater, parent, false)
            UnavailableCountryViewHolder(unavailableCountryRowBinding)
        } else {
            availableCountryBinding = CountryRowBinding.inflate(layoutInflater, parent, false)
            AvailableCountryViewHolder(availableCountryBinding, onCountryItemClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val country = mainCountryList[position]

        if (country.isBlocked) {
            (holder as UnavailableCountryViewHolder).bind(country)
        } else {
            (holder as AvailableCountryViewHolder).bind(country)
        }
    }

    override fun getItemCount(): Int = mainCountryList.size

    class UnavailableCountryViewHolder(
        private val binding: CountryBlockedRowBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(country: CountryTO) {
                binding.countryName.text = country.name
                binding.flagEmoji.text = country.flag
                VCheckSDK.designConfig!!.primaryTextColorHex?.let {
                    binding.countryName.setTextColor(Color.parseColor(it))
                }
            }
    }

    class AvailableCountryViewHolder(
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
            VCheckSDK.designConfig!!.primaryTextColorHex?.let {
                binding.countryName.setTextColor(Color.parseColor(it))
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
