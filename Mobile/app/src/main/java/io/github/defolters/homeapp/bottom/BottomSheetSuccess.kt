package io.github.defolters.homeapp.bottom

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.defolters.homeapp.R
import io.paperdb.Paper
import kotlinx.android.synthetic.main.bottom_sheet.*

class BottomSheetSuccess : CustomRoundBottomSheet() {

    var onApply: (Int, Boolean) -> Unit = { _, _ -> }
    private var state: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        datePicker.setIs24HourView(true)

        //restore data
        val isNotificationOn = Paper.book().read<Boolean>("NOTIFICATION", false)
        val period = Paper.book().read("PERIOD", 60)

        switchNotification.isChecked = isNotificationOn
        val hour = period / 60
        val minute = period.rem(60)
        datePicker.hour = hour
        datePicker.minute = minute

        switchNotification.setOnCheckedChangeListener { buttonView, isChecked ->
            state = isChecked
        }

        buttonApply.setOnClickListener {
            val seconds = datePicker.minute + (datePicker.hour * 60)
            Paper.book().write("PERIOD", seconds)
            Paper.book().write("NOTIFICATION", state)
            Log.d("TAG", "${state} ${datePicker.hour} ${datePicker.minute} ${seconds}")
            this.dismiss()

            onApply(seconds, state)
        }
    }

}