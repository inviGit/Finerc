package com.invi.finerc.common

import com.invi.finerc.models.Category
import com.invi.finerc.models.SMSType
import com.invi.finerc.models.SmsMessageModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AppUtils {

    fun groupSmsMessagesByDate(
        smsList: List<SmsMessageModel>,
        dateFormat: String = "MMM dd",
        locale: Locale = Locale.getDefault(),
        sortDescending: Boolean = false,
        selectedYear: Int? = null,
        selectedMonth: Int? = null // 1-12 for Jan-Dec
    ): List<Pair<String, Double>> {
        val calendar = Calendar.getInstance()

        val filteredList = smsList.filter { sms ->
            calendar.timeInMillis = sms.date
            val messageYear = calendar.get(Calendar.YEAR)
            val messageMonth = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based

            val yearMatches = selectedYear?.let { it == messageYear } ?: true
            val monthMatches = selectedMonth?.let { it == messageMonth } ?: true

            yearMatches && monthMatches
        }

        val grouped = filteredList
            .groupBy {
                val date = Date(it.date)
                SimpleDateFormat(dateFormat, locale).format(date)
            }
            .map { (date, messages) ->
                val totalAmount = messages.fold(0.0) { acc, txn ->
                    acc + txn.amount
                }
                date to totalAmount
            }

        return if (sortDescending) {
            grouped.sortedByDescending { it.second }
        } else {
            grouped.sortedBy { it.first }
        }
    }

    fun groupSmsMessagesByDate1(
        smsList: List<SmsMessageModel>,
        dateFormat: String = "MMM dd",
        locale: Locale = Locale.getDefault(),
        sortDescending: Boolean = false
    ): List<Pair<String, Double>> {
        val grouped = smsList
            .groupBy {
                val date = Date(it.date)
                SimpleDateFormat(dateFormat, locale).format(date)
            }
            .map { (date, messages) ->
                val totalAmount = messages.fold(0.0) { acc, txn ->
                    acc + txn.amount
                }
                date to totalAmount
            }

        return if (sortDescending) {
            grouped.sortedByDescending { it.second }
        } else {
            grouped.sortedBy { it.first }
        }
    }

    fun byDay(
        smsList: List<SmsMessageModel>, selectedYear: Int? = null,
        selectedMonth: Int? = null
    ): List<Pair<String, Double>> =
        groupSmsMessagesByDate(
            smsList,
            "dd",
            selectedYear = selectedYear,
            selectedMonth = selectedMonth
        )

    fun byWeek(
        smsList: List<SmsMessageModel>, selectedYear: Int? = null,
        selectedMonth: Int? = null
    ): List<Pair<String, Double>> =
        groupSmsMessagesByDate(
            smsList,
            "w",
            selectedYear = selectedYear,
            selectedMonth = selectedMonth
        )

    fun byMonth(
        smsList: List<SmsMessageModel>, selectedYear: Int? = null,
        selectedMonth: Int? = null
    ): List<Pair<String, Double>> =
        groupSmsMessagesByDate(
            smsList,
            "MMM",
            selectedYear = selectedYear,
            selectedMonth = selectedMonth
        )

    fun byYear(
        smsList: List<SmsMessageModel>,
        selectedYear: Int? = null
    ): List<Pair<String, Double>> =
        groupSmsMessagesByDate(smsList, "yyyy", selectedYear = selectedYear)
}


