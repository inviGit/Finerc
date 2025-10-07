package com.invi.finerc.common

import com.invi.finerc.domain.models.Category
import com.invi.finerc.domain.models.CurrencyType
import com.invi.finerc.domain.models.TransactionType
import com.invi.finerc.domain.models.TransactionUiModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.MessageDigest
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import java.util.Currency
import java.util.Date
import java.util.Locale

object AppUtils {

    fun dateParser(
        formattedDate: String,
        zone: ZoneId = ZoneId.systemDefault()
    ): Long {
        val text = formattedDate.trim()

        // 1) ISO instant (UTC, ends with 'Z'), e.g., 2025-09-28T08:15:30Z
        runCatching {
            return Instant.from(DateTimeFormatter.ISO_INSTANT.parse(text)).toEpochMilli()
        }

        // 2) ISO zoned/offset (keeps offset/zone), e.g., 2025-09-28T13:45:00+05:30 or with zone ID
        runCatching {
            return ZonedDateTime.parse(text, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                .toInstant().toEpochMilli()
        }
        runCatching {
            return OffsetDateTime.parse(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .toInstant().toEpochMilli()
        }

        // 3) Local patterns (assume system zone when no zone/offset in text)
        val patterns = listOf(
            "dd MMM uuuu, h:mm a",   // e.g., 28 Sep 2025, 1:20 PM
            "dd MMM uuuu HH:mm",     // e.g., 28 Sep 2025 13:20
            "dd MMM uuuu",           // e.g., 28 Sep 2025
            "dd/MM/uuuu HH:mm:ss",   // e.g., 28/09/2025 13:20:30
            "dd/MM/uuuu HH:mm",      // e.g., 28/09/2025 13:20
            "dd/MM/uuuu",            // e.g., 28/09/2025
            "uuuu-MM-dd HH:mm:ss",   // e.g., 2025-09-28 13:20:30
            "uuuu-MM-dd'T'HH:mm:ss", // e.g., 2025-09-28T13:20:30
            "uuuu-MM-dd"             // e.g., 2025-09-28
        )

        for (p in patterns) {
            val fmt = DateTimeFormatter.ofPattern(p, Locale.ENGLISH)
            runCatching {
                if (p.contains('H') || p.contains('h')) {
                    return LocalDateTime.parse(text, fmt).atZone(zone).toInstant().toEpochMilli()
                } else {
                    return LocalDate.parse(text, fmt).atStartOfDay(zone).toInstant().toEpochMilli()
                }
            }
        }

        throw IllegalArgumentException("Unsupported date format: $formattedDate")
    }

    fun parseCurrencySmart(input: String): Pair<BigDecimal, String> {
        val s = input.trim()
        val code = when {
            s.contains("₹") -> "INR"
            s.contains("$") -> "USD"
            else -> "INR" // default; adjust if needed
        }
        val locale = when (code) {
            "INR" -> Locale.ENGLISH
            "USD" -> Locale.US
            else -> Locale.getDefault()
        }
        val fmt = NumberFormat.getCurrencyInstance(locale)
        fmt.currency = Currency.getInstance(code)
        // Let the parser accept up to 2 decimals; actual rounding applied below
        fmt.maximumFractionDigits = 2

        val number = fmt.parse(s) ?: 0
        val bd = BigDecimal.valueOf(number.toDouble()).setScale(2, RoundingMode.HALF_UP)
        return bd to code
    }

    fun formatCurrency(
        amount: BigDecimal,
        currencyCode: CurrencyType?,
        locale: Locale? = null,
        roundingMode: RoundingMode = RoundingMode.HALF_UP
    ): String {
        val cur = Currency.getInstance(currencyCode.toString())
        val fmt = NumberFormat.getCurrencyInstance(
            locale ?: defaultLocaleForCurrency(currencyCode.toString())
        )
        fmt.currency = cur

        // Up to two decimals: show 0, 1, or 2 as needed, capped at 2
        fmt.minimumFractionDigits = 0
        fmt.maximumFractionDigits = 2

        // Round to 2 decimals, then trim trailing zeros (e.g., 100.00 -> 100, 19.50 -> 19.5)
        val rounded = amount.setScale(2, roundingMode).stripTrailingZeros()

        return fmt.format(rounded)
    }

    private fun defaultLocaleForCurrency(code: String): Locale =
        when (code) {
            "INR" -> Locale.ENGLISH   // Indian grouping and ₹
            "USD" -> Locale.US            // US grouping and $
            else -> Locale.getDefault()
        }

    fun formatDate(txnDate: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(txnDate))
    }

    sealed interface CutoffKind {
        data object WeekFromMonday : CutoffKind
        data class MonthFromDay(
            val dayOfMonth: Int,
            // If true and today is before dayOfMonth, roll to previous month’s `dayOfMonth`
            val rollIfBeforeStart: Boolean = false
        ) : CutoffKind

        data object YearFromJan1 : CutoffKind
    }

    fun cutoffMillis(
        kind: CutoffKind,
        nowMillis: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault()
    ): Long {
        val nowZdt = Instant.ofEpochMilli(nowMillis).atZone(zone)
        return when (kind) {
            CutoffKind.WeekFromMonday -> {
                val monday = nowZdt.toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .atStartOfDay(zone)
                monday.toInstant().toEpochMilli()
            }

            is CutoffKind.MonthFromDay -> {
                val base = if (kind.rollIfBeforeStart && nowZdt.dayOfMonth < kind.dayOfMonth)
                    nowZdt.minusMonths(1) else nowZdt
                val length = base.toLocalDate().lengthOfMonth()
                val safeDay = kind.dayOfMonth.coerceIn(1, length)
                val start = base.withDayOfMonth(safeDay).toLocalDate().atStartOfDay(zone)
                start.toInstant().toEpochMilli()
            }

            CutoffKind.YearFromJan1 -> {
                val start = nowZdt.toLocalDate().withDayOfYear(1).atStartOfDay(zone)
                start.toInstant().toEpochMilli()
            }
        }
    }

    fun groupSmsMessagesByDate(
        smsList: List<TransactionUiModel>,
        dateFormat: String = "MMM dd",
        locale: Locale = Locale.getDefault(),
        sortDescending: Boolean = false,
        selectedYear: Int? = null,
        selectedMonth: Int? = null // 1-12 for Jan-Dec
    ): List<Pair<String, Double>> {
        val calendar = Calendar.getInstance()

        val filteredList = smsList.filter { sms ->
            calendar.timeInMillis = sms.txnDate
            val messageYear = calendar.get(Calendar.YEAR)
            val messageMonth = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based

            val yearMatches = selectedYear?.let { it == messageYear } ?: true
            val monthMatches = selectedMonth?.let { it == messageMonth } ?: true

            yearMatches && monthMatches
        }

        val grouped = filteredList
            .groupBy {
                val date = Date(it.txnDate)
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
        smsList: List<TransactionUiModel>, selectedYear: Int? = null,
        selectedMonth: Int? = null
    ): List<Pair<String, Double>> =
        groupSmsMessagesByDate(
            smsList,
            "dd",
            selectedYear = selectedYear,
            selectedMonth = selectedMonth
        )

    fun byWeek(
        smsList: List<TransactionUiModel>, selectedYear: Int? = null,
        selectedMonth: Int? = null
    ): List<Pair<String, Double>> =
        groupSmsMessagesByDate(
            smsList,
            "w",
            selectedYear = selectedYear,
            selectedMonth = selectedMonth
        )

    fun byMonth(
        smsList: List<TransactionUiModel>, selectedYear: Int? = null,
        selectedMonth: Int? = null
    ): List<Pair<String, Double>> =
        groupSmsMessagesByDate(
            smsList,
            "MMM",
            selectedYear = selectedYear,
            selectedMonth = selectedMonth
        )

    fun byYear(
        smsList: List<TransactionUiModel>,
        selectedYear: Int? = null
    ): List<Pair<String, Double>> =
        groupSmsMessagesByDate(smsList, "yyyy", selectedYear = selectedYear)

    fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
            else -> "Jan"
        }
    }

    fun generateTransactionId(
        txnDate: Long,
        bankName: String?,
        amount: Double,
        txnType: TransactionType,
        transactionId: String?
    ): String {
        val raw =
            "${bankName.orEmpty()}_${txnDate}_${"%.2f".format(amount)}_${txnType}_${transactionId.orEmpty()}}"
        return raw.toSha256()
    }

    fun generateItemUniqueId(
        orderId: String,
        orderDate: Long,
        unitPrice: Double,
        quantity: Int,
        productName: String
    ): String {
        val raw =
            "${orderId.orEmpty()}_${orderDate}_${"%.2f".format(unitPrice)}_${quantity}_${productName.orEmpty()}}"
        return raw.toSha256()
    }

    fun String.toSha256(): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private val dateFormats = listOf(
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    )

    /**
     * Parse date string to milliseconds
     * Example: "14/08/2025" -> 1723584600000
     */
    fun parseDateToMillis(dateString: String): Long {
        val cleanDate = dateString.trim()

        // Try predefined formats
        for (format in dateFormats) {
            try {
                val date = format.parse(cleanDate)
                if (date != null) {
                    return date.time
                }
            } catch (e: Exception) {
                // Try next format
            }
        }

        // Fallback: Manual parsing
        try {
            val parts = cleanDate.split("/", "-", ".")
            if (parts.size == 3) {
                val day = parts[0].toInt()
                val month = parts[1].toInt() - 1 // Calendar months are 0-based
                val year = parts[2].toInt()

                val calendar = Calendar.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                return calendar.timeInMillis
            }
        } catch (e: Exception) {
            println("❌ Failed to parse date: $dateString - ${e.message}")
        }

        // Last resort: current time
        return System.currentTimeMillis()
    }

    /**
     * Extract amount from string like "190.39 Dr" or "7.00 Cr"
     * Example: "190.39 Dr" -> 190.39
     */
    fun extractAmount(amountString: String): Double {
        if (amountString.isEmpty()) return 0.0

        try {
            val cleanAmount = amountString
                .replace("D", "", ignoreCase = true)
                .replace("C", "", ignoreCase = true)
                .replace("₹", "")
                .replace("Rs", "", ignoreCase = true)
                .replace(",", "")
                .replace("O", "0") // OCR error correction
                .replace("l", "1") // OCR error correction
                .replace("S", "5") // OCR error correction
                .trim()

            return cleanAmount.toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            println("❌ Failed to extract amount from: $amountString - ${e.message}")
            return 0.0
        }
    }

    fun extractCashbackAmount(amountString: String): Double {
        if (amountString.isEmpty()) return 0.0

        if (amountString.contains("D", ignoreCase = true)) return 0.0

        return extractAmount(amountString)
    }

    /**
     * Extract transaction type from amount string
     * "Dr" = DEBIT, "Cr" = CREDIT
     * Example: "190.39 Dr" -> DEBIT
     */
    fun extractTransactionType(amountString: String): TransactionType {
        return if (amountString.contains("C", ignoreCase = true)) {
            TransactionType.CREDIT
        } else {
            TransactionType.DEBIT
        }
    }

    fun extractPlace(message: String): String? {
        val lowerMessage = message.lowercase()

        // 1. Check against category keywords (your list of places)
        for (category in Category.entries) {
            for (keyword in category.keywords) {
                val keywordLower = keyword.lowercase()
                if (lowerMessage.contains(keywordLower)) {
                    return keyword
                }
            }
        }

        // 2. Fallback to regex patterns (most accurate)
        val fallbackRegex = Regex(
            """(?:at|to|on|via|by|pos|txn|thru at|paid to|spent at|purchase at|swiped at)\s+([a-zA-Z0-9&@.\- ]{2,30})""",
            RegexOption.IGNORE_CASE
        )
        val match = fallbackRegex.find(message)
        val place = match?.groupValues?.getOrNull(1)?.trim()

        return place?.takeIf { it.isNotBlank() }
    }

    val months = listOf(
        1 to "January",
        2 to "February",
        3 to "March",
        4 to "April",
        5 to "May",
        6 to "June",
        7 to "July",
        8 to "August",
        9 to "September",
        10 to "October",
        11 to "November",
        12 to "December"
    )
}


// Recover bank/card from "Bank • Card" (bullet separator)
//    val parts = transactionUiModel.bankName.split("•").map { it.trim() }
//    val bankName = parts.getOrNull(0)?.takeIf { it.isNotEmpty() } ?: ""
//    val cardType = parts.getOrNull(1)?.takeIf { it.isNotEmpty() } ?: ""


