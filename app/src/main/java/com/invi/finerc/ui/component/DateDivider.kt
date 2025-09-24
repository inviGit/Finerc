package com.invi.finerc.ui.component

import androidx.compose.runtime.Composable
import java.util.Calendar
import java.util.Locale

@Composable
fun DateDivider(date: Long) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = date

    val today = Calendar.getInstance()
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)

    val yesterday = Calendar.getInstance()
    yesterday.add(Calendar.DAY_OF_YEAR, -1)
    yesterday.set(Calendar.HOUR_OF_DAY, 0)
    yesterday.set(Calendar.MINUTE, 0)
    yesterday.set(Calendar.SECOND, 0)
    yesterday.set(Calendar.MILLISECOND, 0)

    val dateText = when {
        calendar.timeInMillis == today.timeInMillis -> "Today"
        calendar.timeInMillis == yesterday.timeInMillis -> "Yesterday"
        else -> {
            val formatter = java.text.SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            formatter.format(calendar.time)
        }
    }

    TextDivider(
        dateText
    )

}