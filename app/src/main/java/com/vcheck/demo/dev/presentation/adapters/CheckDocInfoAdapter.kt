package com.vcheck.demo.dev.presentation.adapters

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.DocInfoRowBinding
import com.vcheck.demo.dev.domain.DocFieldWitOptPreFilledData
import com.vcheck.demo.dev.presentation.check_doc_info_stage.CheckDocInfoFragment
import com.vcheck.demo.dev.util.isValidDocRelatedDate
import java.util.*


class CheckDocInfoAdapter(
    private val documentInfoList: ArrayList<DocFieldWitOptPreFilledData>,
    private val docInfoEditCallback: DocInfoEditCallback,
    private val currentLocaleCode: String
) :
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

    class ViewHolder(
        private val binding: DocInfoRowBinding,
        private val docInfoEditCallback: DocInfoEditCallback,
        private val localeCode: String
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(documentInfo: DocFieldWitOptPreFilledData) {

            val title = when (localeCode) {
                "uk" -> documentInfo.title.ua ?: documentInfo.title.ua
                "ru" -> documentInfo.title.ru ?: documentInfo.title.ru
                else -> documentInfo.title.en
            }
            binding.docFieldTitle.text = title
            binding.infoField.setText(documentInfo.autoParsedValue)

            if ((documentInfo.name == "date_of_birth" || documentInfo.name == "date_of_expiry")) {

                val hint = when (localeCode) {
                    "uk" -> "РРРР-ММ-ДД"
                    "ru" -> "ГГГГ-ММ-ДД"
                    else -> "YYYY-MM-DD"
                }

                binding.infoField.hint = hint
                binding.infoField.inputType = InputType.TYPE_CLASS_NUMBER

            } else {
                binding.infoField.hint = ""
            }


            binding.infoField.addTextChangedListener(object : TextWatcher {

                private var current = ""
                private val yyyymmdd = (docInfoEditCallback as CheckDocInfoFragment).getString(
                    R.string.yyyymmdd
                )
                private val cal = Calendar.getInstance()

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(
                    text: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    if (documentInfo.name == "date_of_birth" || documentInfo.name == "date_of_expiry") {
                        try {

                            if (text.toString() != current) {
                                var cleanString =
                                    text.toString().replace("[^\\d.]|\\.".toRegex(), "")
                                val cleanCurrent = current.replace("[^\\d.]|\\.".toRegex(), "")
                                var selection = cleanString.length
                                if (cleanString.length == 4) {
                                    selection++
                                }
                                if (cleanString.length == 5) {
                                    selection++
                                }
                                if (cleanString.length == 6) {
                                    selection += 2
                                }
                                if (cleanString.length == 7) {
                                    selection += 2
                                }
                                if (cleanString.length >= 8) {
                                    selection += 2
                                }

                                if (cleanString == cleanCurrent) {
                                    selection--
                                }

                                if (cleanString.length < 8) {
                                    cleanString += yyyymmdd.substring(cleanString.length)
                                } else {

                                    var day = cleanString.substring(6, 8).toInt()
                                    var mon = cleanString.substring(4, 6).toInt()
                                    var year = cleanString.substring(0, 4).toInt()
                                    mon = if (mon < 1) 1 else if (mon > 12) 12 else mon
                                    cal[Calendar.MONTH] = mon - 1
                                    year =
                                        if (year < 1900) 1900 else if (year > 2100) 2100 else year
                                    cal[Calendar.YEAR] = year

                                    day =
                                        if (day > cal.getActualMaximum(Calendar.DATE)) cal.getActualMaximum(
                                            Calendar.DATE
                                        ) else day
                                    cleanString = String.format("%02d%02d%02d", year, mon, day)
                                }
                                cleanString = String.format(
                                    "%s-%s-%s",
                                    cleanString.substring(0, 4),
                                    cleanString.substring(4, 6),
                                    cleanString.substring(6, 8)
                                )
                                selection = if (selection < 0) 0 else selection
                                current = cleanString
                                binding.infoField.setText(current)
                                binding.infoField.setSelection(if (selection >= 10) 10 else selection)
                            }

                        } catch (e: Exception) {

                        }
                    }

                    if (text.isNotEmpty()) {
                        if (documentInfo.regex != null
                            && !text.matches(Regex(documentInfo.regex))
                        ) {
                            binding.infoField.error =
                                (docInfoEditCallback as CheckDocInfoFragment).getString(
                                    R.string.number_of_document_validation_error
                                )
                        } else {
                            if (documentInfo.name == "date_of_birth" && !isValidDocRelatedDate(
                                    current
                                )
                            ) {
                                binding.infoField.error =
                                    (docInfoEditCallback as CheckDocInfoFragment).getString(
                                        R.string.date_of_birth_validation_error
                                    )
                            } else if (documentInfo.name == "date_of_expiry" && !isValidDocRelatedDate(
                                    current
                                )
                            ) {
                                binding.infoField.error =
                                    (docInfoEditCallback as CheckDocInfoFragment).getString(
                                        R.string.date_of_expiry_validation_error
                                    )
                            } else {
                                if (text.length < 3) {
                                    when (documentInfo.title.en) {
                                        "Surname (cyrillic)" -> {
                                            binding.infoField.error =
                                                (docInfoEditCallback as CheckDocInfoFragment).getString(
                                                    R.string.surname_cyrillic_validation_error
                                                )
                                        }
                                        "Surname (latin)" -> {
                                            binding.infoField.error =
                                                (docInfoEditCallback as CheckDocInfoFragment).getString(
                                                    R.string.surname_latin_validation_error
                                                )
                                        }
                                        "Name (cyrillic)" -> {
                                            binding.infoField.error =
                                                (docInfoEditCallback as CheckDocInfoFragment).getString(
                                                    R.string.name_cyrillic_validation_error
                                                )
                                        }
                                        "Name (latin)" -> {
                                            binding.infoField.error =
                                                (docInfoEditCallback as CheckDocInfoFragment).getString(
                                                    R.string.name_latin_validation_error
                                                )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (documentInfo.name == "date_of_birth" || documentInfo.name == "date_of_expiry") {
                        docInfoEditCallback.onFieldInfoEdited(documentInfo.name, current)
                    } else {
                        docInfoEditCallback.onFieldInfoEdited(documentInfo.name, text.toString())
                    }

                }

                override fun afterTextChanged(p0: Editable?) {}
            })
        }
    }
}