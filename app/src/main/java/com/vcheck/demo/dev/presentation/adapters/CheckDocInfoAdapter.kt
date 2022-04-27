package com.vcheck.demo.dev.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.DocInfoRowBinding
import com.vcheck.demo.dev.domain.DocFieldWitOptPreFilledData
import com.vcheck.demo.dev.domain.DocType
import com.vcheck.demo.dev.presentation.check_doc_info_stage.CheckDocInfoFragment
import com.vcheck.demo.dev.util.isValidDocRelatedDate
import kotlin.collections.ArrayList

class CheckDocInfoAdapter(private val documentInfoList: ArrayList<DocFieldWitOptPreFilledData>,
                          private val docInfoEditCallback: DocInfoEditCallback,
                            private val currentLocaleCode: String) :
    RecyclerView.Adapter<CheckDocInfoAdapter.ViewHolder>() {

    private lateinit var binding: DocInfoRowBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        binding = DocInfoRowBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, docInfoEditCallback, currentLocaleCode)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val documentInfo = documentInfoList[position]
        holder.bind(documentInfo)
    }

    override fun getItemCount(): Int = documentInfoList.size

    class ViewHolder(private val binding: DocInfoRowBinding, private val docInfoEditCallback: DocInfoEditCallback,
        private val localeCode: String) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(documentInfo: DocFieldWitOptPreFilledData) {

            val title = when(localeCode) {
                "uk" -> documentInfo.title.ua ?: documentInfo.title.ua
                "ru" -> documentInfo.title.ru ?: documentInfo.title.ru
                else -> documentInfo.title.en
            }
            binding.docFieldTitle.text = title
            binding.infoField.setText(documentInfo.autoParsedValue)

            if ((documentInfo.name == "date_of_birth" || documentInfo.name == "date_of_expiry")) {
                binding.infoField.hint = "YYYY-MM-DD"
            } else {
                binding.infoField.hint = ""
            }

            binding.infoField.doOnTextChanged { text, start, before, count ->
                if (text != null && text.isNotEmpty()) {
                    if (documentInfo.regex != null
                        && !text.matches(Regex(documentInfo.regex))) {
                        binding.infoField.error = (docInfoEditCallback as CheckDocInfoFragment).getString(
                            R.string.check_doc_fields_validation_error)
                    } else {
                        if ((documentInfo.name == "date_of_birth" || documentInfo.name == "date_of_expiry")
                            && !isValidDocRelatedDate(text.toString())) {
                                binding.infoField.error = (docInfoEditCallback as CheckDocInfoFragment).getString(
                                    R.string.check_doc_fields_validation_error)
                        } else {
                            if (text.length < 3) {
                                binding.infoField.error = (docInfoEditCallback as CheckDocInfoFragment).getString(
                                    R.string.check_doc_fields_validation_error)
                            }
                        }
                    }
                }
                docInfoEditCallback.onFieldInfoEdited(documentInfo.name, text.toString())
            }
        }
    }
}