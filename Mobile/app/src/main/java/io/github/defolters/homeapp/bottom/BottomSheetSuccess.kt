package io.github.defolters.homeapp.bottom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.github.defolters.homeapp.R
import kotlinx.android.synthetic.main.bottom_sheet.*

class BottomSheetSuccess : CustomRoundBottomSheet() {

    private var state:Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        datePicker.setIs24HourView(true)

        switchNotification.setOnCheckedChangeListener { buttonView, isChecked ->
            state = isChecked
        }

        buttonApply.setOnClickListener {
            Toast.makeText(this.activity, "${state} ${datePicker.hour} ${datePicker.minute}", Toast.LENGTH_SHORT).show()
            this.dismiss()
        }
    }

}