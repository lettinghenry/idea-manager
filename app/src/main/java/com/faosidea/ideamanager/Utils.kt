package com.faosidea.ideamanager

import android.graphics.Paint
import android.widget.EditText
import android.widget.TextView

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
}