package com.vcheck.demo.dev.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.models.Country
import kotlinx.android.synthetic.main.country_row.view.*

class CountryListAdapter(private val countryList: ArrayList<Country>) :
    RecyclerView.Adapter<CountryListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.country_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val country = countryList[position]
        holder.bind(country)
    }

    override fun getItemCount(): Int = countryList.size


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(country: Country) {
            itemView.country_name.text = country.name
            itemView.flag_image.setImageResource(country.flag)

            itemView.setOnClickListener {
                Snackbar.make(itemView, country.name, Snackbar.LENGTH_LONG).show()
            }
        }
    }

}
