package com.faosidea.ideamanager

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Date
import java.util.Locale

object Utils {

    /**
     * An elaborate check for string nullness and emptiness
     */
    fun isEmpty(string: String?): Boolean {
        return string.isNullOrEmpty() ||
                string.isEmpty() ||
                string.trim() == "" ||
                string.equals("null", true)
    }


    fun TextView.setStrikeThrough(strikeThrough: Boolean) {
        if (strikeThrough) {
            // Add the strike-through flag while keeping other flags
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            // Remove the strike-through flag while keeping other flags
            paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    fun EditText.setStrikeThrough(strikeThrough: Boolean) {
        if (strikeThrough) {
            // Add the strike-through flag while keeping other flags
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            // Remove the strike-through flag while keeping other flags
            paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    /**
     *  date selection from calendar
     */
    fun selectDate(view: View, activity: FragmentActivity): Long {

        var selectedDate = 0L
        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Due date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now())
                    .build()
            )

        val picker = builder.build()

        picker.addOnPositiveButtonClickListener { millis ->
            Log.d("DatePicker", "Selected millis :: $millis")
            selectedDate = millis

            //update UI
            (view as TextView).text = formatDate(millis)
        }

        picker.show(activity.supportFragmentManager, "DATE_PICKER")
        return selectedDate
    }

    // Helper function to format milliseconds into a readable date string
     fun formatDate(milliseconds: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val formated = sdf.format(Date(milliseconds))
        Log.d("DatePicker", "Formated date :: $formated")
        return formated
    }

    /**
     * helper to delete task item
     */
    fun showDeleteConfirmationDialog(context: Context, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.confirm_deletion))
            .setMessage(context.getString(R.string.are_you_sure_you_want_to_delete))
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton(context.getString(R.string.delete)) { dialog, which ->
                onConfirm()
                dialog.dismiss()
            }
            .show()
    }

    /**
     * validate input
     */
    fun validateText(textView: TextView): Boolean {
        val text = (textView.text.toString() + "")
        val isValid = !Utils.isEmpty(text)

        if (!isValid) {
            textView.error = "invalid input length!"
        }
        return isValid
    }

}