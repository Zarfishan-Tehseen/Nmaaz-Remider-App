package com.example.nmaazreminder.ui.fragments.home.choosedate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.fragment.app.activityViewModels
import com.example.nmaazreminder.R
import com.example.nmaazreminder.ui.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar

class ChooseDateBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_choose_date_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendarView = view.findViewById<CalendarView>(R.id.calendarViewSelector)

        // This safely extracts the current Calendar instance out of the state container flow
        calendarView.date = viewModel.selectedDateState.value.timeInMillis

        // Update the state when the user selects a new date
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val targetCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }

            viewModel.updateSelectedDate(targetCalendar)
            dismiss()
        }
    }
}