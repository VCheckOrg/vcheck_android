package com.vcheck.demo.dev.presentation

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vcheck.demo.dev.R

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val langSpinner = findViewById<Spinner>(R.id.lang_spinner)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.languages, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        langSpinner.adapter = adapter
        langSpinner.onItemSelectedListener = this
    }

    override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, l: Long) {
        val text = adapterView.getItemAtPosition(position).toString()
        //Toast.makeText(adapterView.context, text, Toast.LENGTH_SHORT).show()
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {}

}