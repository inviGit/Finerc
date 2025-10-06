package com.invi.finerc.common

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.Telephony
import com.invi.finerc.domain.models.Category
import com.invi.finerc.domain.models.TransactionType
import com.invi.finerc.domain.models.TransactionUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

// SMS Scanner Class
class SmsScanner(private val context: Context) {

    private val bankPatterns = mapOf(
        "SBI" to listOf("SBI", "SBIPAY", "STBANK"),
        "HDFC" to listOf("HDFC", "HDFCBK"),
        "ICICI" to listOf("ICICI", "ICICIB"),
        "AXIS" to listOf("AXIS", "AXISBK"),
        "PNB" to listOf("PNB", "PNBSMS"),
        "BOI" to listOf("BOI", "BOISMS"),
        "KOTAK" to listOf("KOTAK", "KOTAKBK"),
        "YES" to listOf("YES", "YESBNK"),
        "PAYTM" to listOf("PAYTM", "PYTMSMS"),
        "GPAY" to listOf("GPAY", "GOOGLEPAY"),
        "PHONEPE" to listOf("PHONEPE", "PHNEPE")
    )

    suspend fun scanSmsMessages(): List<TransactionUiModel> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<TransactionUiModel>()

        try {
            val contentResolver: ContentResolver = context.contentResolver
            val cursor: Cursor? = contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE
                ),
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use { c ->
                val addressIndex = c.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = c.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = c.getColumnIndex(Telephony.Sms.DATE)

                while (c.moveToNext()) {
                    val address = c.getString(addressIndex) ?: ""
                    val body = c.getString(bodyIndex) ?: ""
                    val date = c.getLong(dateIndex)

                    // Check if this is a transaction SMS
                    if (isTransactionSms(body)) {

                        val transactionType: TransactionType = getTransactionType(body)
                        val amount: Double = extractAmount(body, transactionType) ?: 0.0
                        val bankName = getBankName(address, body)
                        val place = AppUtils.extractPlace(body) ?: ""
                        val category = categorizeTransaction(body)

//                        val transactionId = AppUtils.generateTransactionId(
//                            date,
//                            bankName,
//                            amount,
//                            transactionType)

//                        messages.add(
//                            TransactionUiModel(
//                                id = 0L,
//                                transactionId = transactionId,
//                                description = body,
//                                date = date,
//                                place = place,
//                                transactionType = transactionType,
//                                bankName = bankName,
//                                category = category,
//                                amount = amount,
//                                source = TransactionSource.SMS,
//                                currencyCode = CurrencyType.INR,
//                                isInvalid = false
//                            )
//                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        messages
    }

    private fun isTransactionSms(body: String): Boolean {
        val transactionKeywords = listOf(
            "spent", "credited", "debited"
        )
        return transactionKeywords.any { body.contains(it, ignoreCase = true) }
    }

    private fun getTransactionType(body: String): TransactionType {
        return when {
            body.contains("credited", ignoreCase = true) ||
                    body.contains("received", ignoreCase = true) ||
                    body.contains("deposited", ignoreCase = true) -> TransactionType.CREDIT

            body.contains("debited", ignoreCase = true) ||
                    body.contains("withdrawn", ignoreCase = true) ||
                    body.contains("spent", ignoreCase = true) ||
                    body.contains("paid", ignoreCase = true) -> TransactionType.DEBIT

            else -> TransactionType.DRAFT
        }
    }

    private fun extractAmount(body: String, transactionType: TransactionType): Double? {
        val amountPattern =
            Pattern.compile("(?:rs|inr|₹)\\s*([0-9,]+(?:\\.[0-9]{2})?)", Pattern.CASE_INSENSITIVE)
        val matcher = amountPattern.matcher(body)

        if (matcher.find()) {
            return matcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
        } else {
            return 0.0
        }
    }

    data class ParsedAmount(val amount: Double, val context: String)

    private fun extractDebitAmount(message: String): ParsedAmount? {
        val amountRegex =
            Regex("""(?:₹|Rs\.?|INR|\$)?\s?[\d,]+(?:\.\d{1,2})?""", RegexOption.IGNORE_CASE)
        val matches = amountRegex.findAll(message)

        val results = matches.mapNotNull { match ->
            val rawAmount = match.value
            val index = match.range.first

            // Clean up amount string
            val cleaned = rawAmount.replace(Regex("""[^\d.]"""), "")
            val amount = cleaned.toDoubleOrNull() ?: return@mapNotNull null

            // Extract context around amount (30 chars before and after)
            val start = (index - 30).coerceAtLeast(0)
            val end = (index + rawAmount.length + 30).coerceAtMost(message.length)
            val context = message.substring(start, end).lowercase()

            ParsedAmount(amount, context)
        }.toList()

        // Prefer context with keywords related to 'spent' or 'debited'
        val preferred = results.firstOrNull {
            it.context.contains("debited") || it.context.contains("spent") || it.context.contains("used") || it.context.contains(
                "paid"
            )
        }

        return preferred ?: results.firstOrNull()
    }

    private fun getBankName(address: String, body: String): String {
        // First check sender address
        for ((bankName, patterns) in bankPatterns) {
            if (patterns.any { address.contains(it, ignoreCase = true) }) {
                return bankName
            }
        }

        // Then check message body
        for ((bankName, patterns) in bankPatterns) {
            if (patterns.any { body.contains(it, ignoreCase = true) }) {
                return bankName
            }
        }

        return "Unknown"
    }

    fun categorizeTransaction(body: String): Category {
        return Category.fromKeyword(body)
//        return CategoryModel.fromCategory(category)
    }
}