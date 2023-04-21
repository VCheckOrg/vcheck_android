package com.vcheck.sdk.core.presentation.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.ProviderListItemBinding
import com.vcheck.sdk.core.domain.Provider

class ProvidersListAdapter(private val providersList: List<Provider>,
                           private val onProviderItemClick: OnProviderItemClick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var providerListItemBinding: ProviderListItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        providerListItemBinding = ProviderListItemBinding.inflate(layoutInflater, parent, false)
        return ProviderViewHolder(providerListItemBinding, onProviderItemClick)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val provider = providersList[position]
        (holder as ProviderViewHolder).bind(provider)
    }

    override fun getItemCount(): Int = providersList.size

    class ProviderViewHolder(
        private val binding: ProviderListItemBinding,
        private val onProviderItemClick: OnProviderItemClick,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(provider: Provider) {

            if (provider.protocol != "vcheck") {
                binding.verifMethodTitleVcheck.text = provider.name.replaceFirstChar { it.uppercaseChar() }
                binding.verifMethodSubtitleVcheck.text = ""
                // other fields are not customizable yet
            }

            VCheckSDK.backgroundTertiaryColorHex?.let {
                binding.cardVcheckBackground.setCardBackgroundColor(Color.parseColor(it))
            }
            VCheckSDK.primaryTextColorHex?.let {
                binding.verifMethodTitleVcheck.setTextColor(Color.parseColor(it))
            }
            VCheckSDK.secondaryTextColorHex?.let {
                binding.verifMethodSubtitleVcheck.setTextColor(Color.parseColor(it))
            }
            VCheckSDK.borderColorHex?.let {
                binding.methodCardVcheck.setCardBackgroundColor(Color.parseColor(it))
            }

            binding.cardVcheckBackground.setOnClickListener {
                onProviderItemClick.onClick(provider)
            }
        }
    }

    interface OnProviderItemClick {
        fun onClick(provider: Provider)
    }
}