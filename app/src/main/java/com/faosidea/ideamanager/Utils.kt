package com.faosidea.ideamanager

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
}