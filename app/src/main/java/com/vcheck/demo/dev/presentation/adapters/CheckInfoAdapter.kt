package com.vcheck.demo.dev.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.DocInfoRowBinding
import com.vcheck.demo.dev.domain.DocFieldWitOptPreFilledData
import com.vcheck.demo.dev.presentation.check_doc_info_stage.CheckDocInfoFragment

class CheckInfoAdapter(private val documentInfoList: ArrayList<DocFieldWitOptPreFilledData>,
                       private val docInfoEditCallback: DocInfoEditCallback) :
    RecyclerView.Adapter<CheckInfoAdapter.ViewHolder>() {

    private lateinit var binding: DocInfoRowBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        binding = DocInfoRowBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, docInfoEditCallback)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val documentInfo = documentInfoList[position]
        holder.bind(documentInfo)
    }

    override fun getItemCount(): Int = documentInfoList.size

    class ViewHolder(private val binding: DocInfoRowBinding, private val docInfoEditCallback: DocInfoEditCallback) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(documentInfo: DocFieldWitOptPreFilledData) {
            //TODO: check for locale: if device locale corresponds to one of localized titles,
            // set appropriate title (until we have more locales)
            // Else, set title.en:
            val title = documentInfo.title.ru
            binding.docFieldTitle.text = title
            binding.infoField.setText(documentInfo.autoParsedValue)

            binding.infoField.doOnTextChanged { text, start, before, count ->
                if (text != null && text.isNotEmpty() && documentInfo.regex != null
                    && !text.matches(Regex(documentInfo.regex))) {
                        //TODO test
                    binding.infoField.error = (docInfoEditCallback as CheckDocInfoFragment).getString(
                        R.string.check_doc_fields_validation_error)
                }
                docInfoEditCallback.onFieldInfoEdited(documentInfo.name, text.toString())
            }
        }
    }
}