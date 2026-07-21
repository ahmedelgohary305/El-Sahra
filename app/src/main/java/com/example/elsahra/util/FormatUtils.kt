package com.example.elsahra.util

import java.util.Locale

object FormatUtils {
    fun formatVoteCount(count: Int): String {
        return when {
            count >= 1000000 -> String.format(Locale.US, "%.1fM", count / 1000000.0)
            count >= 1000 -> String.format(Locale.US, "%.1fK", count / 1000.0)
            else -> count.toString()
        }
    }

    fun formatCurrency(amount: Long): String {
        if (amount <= 0) return "N/A"
        return java.text.NumberFormat.getCurrencyInstance(Locale.US).apply {
            maximumFractionDigits = 0
        }.format(amount)
    }
}
