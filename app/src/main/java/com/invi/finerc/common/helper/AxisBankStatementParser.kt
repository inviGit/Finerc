package com.invi.finerc.common.helper

import com.invi.finerc.common.AppUtils
import com.invi.finerc.data.entity.CreditCardEntity
import com.invi.finerc.data.entity.TransactionEntity
import com.invi.finerc.domain.models.Category
import com.invi.finerc.domain.models.CurrencyType
import com.invi.finerc.domain.models.PaymentSummary
import com.invi.finerc.domain.models.TransactionSource
import com.invi.finerc.domain.models.TransactionStatus
import com.invi.finerc.domain.models.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale

class AxisBankStatementParser {

    private val dateFormat = SimpleDateFormat("dd MMM ''yy", Locale.ENGLISH)
    private val paymentDateFormat = SimpleDateFormat("dd MMM ''yy", Locale.ENGLISH)
    private val statementMonthFormat = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
    private val cardNumberRegex = Regex("(\\d{4}[\\*X]{4,}\\d{4})")
    private val cardHolderRegex =
        Regex("""(?m)^(.*?)\nADDRESS""") // Assuming cardholder is first line before ADDRESS
    private val creditLimitRegex = Regex("""Credit Limit\s*₹\s*([\d,]+\.\d{2})""")
    private val paymentDueDateRegex = Regex("""Payment Due Date\s*(\d{2} \w{3} '\d{2})""")
    private val totalPaymentDueRegex = Regex("""Total Payment Due\s*₹\s*([\d,]+\.\d{2})""")
    private val minimumPaymentDueRegex = Regex("""Minimum Payment Due\s*₹\s*([\d,]+\.\d{2})""")
    private val selectedStatementMonthRegex =
        Regex("""Selected Statement Month\s*₹\s*([\d,]+\.\d{2})""")
    private val transactionLineRegex =
        Regex("""^(\d{2} \w{3} '\d{2})\s+(.*?)\s+₹\s*([\d,]+\.\d{2})\s+(Debit|Credit)$""")
    private val TRANSACTION_SUMMARY_HEADER =
        Regex("""^\s*[\[\w]*\s*Transaction\s+Summary[\[\w]*\s*$""", RegexOption.IGNORE_CASE)
    private val dateRegex =
        Regex("""^\d{1,2} (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) '\d{2}$""")
    private val debitCreditRegex = Regex("""^(Debit|Credit)$""", RegexOption.IGNORE_CASE)

    /**
     * Parses CreditCardEntity from full statement text
     */
    fun parseCreditCardEntity(
        statementText: String,
        lines: List<String>,
        paymentSummary: PaymentSummary
    ): CreditCardEntity {
        val cardNumberMasked = extractCardNumber(statementText)
        val cardHolderName = lines[1]
        val creditLimit = extractCreditLimit(statementText)

        return CreditCardEntity(
            bankName = "AXIS BANK",
            cardNumberMasked = cardNumberMasked,
            cardType = "CREDIT",
            cardHolderName = cardHolderName,
            creditLimit = creditLimit,
        )
    }

    /**
     * Extract masked card number
     */
    fun extractCardNumber(text: String): String {
        return cardNumberRegex.find(text)?.groupValues?.get(1) ?: "****XXXX"
    }

    /**
     * Extract credit limit as Double
     */
    fun extractCreditLimit(text: String): Double {
        val raw = creditLimitRegex.find(text)?.groupValues?.get(1)?.replace(",", "") ?: "0.0"
        return raw.toDoubleOrNull() ?: 0.0
    }

    /**
     * Extract payment summary data
     */
    fun extractPaymentSummary(text: String): PaymentSummary {
        val totalDueRaw =
            totalPaymentDueRegex.find(text)?.groupValues?.get(1)?.replace(",", "") ?: "0.0"
        val minDueRaw =
            minimumPaymentDueRegex.find(text)?.groupValues?.get(1)?.replace(",", "") ?: "0.0"
        val dueDateStr = paymentDueDateRegex.find(text)?.groupValues?.get(1) ?: ""
        val selectedMonth = selectedStatementMonthRegex.find(text)?.groupValues?.get(1) ?: ""

        val dueDateMillis = try {
            paymentDateFormat.parse(dueDateStr)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }

        val statementMonthMillis = try {
            statementMonthFormat.parse(selectedMonth)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
        return PaymentSummary(
            totalPaymentDue = totalDueRaw.toDoubleOrNull() ?: 0.0,
            minimumPaymentDue = minDueRaw.toDoubleOrNull() ?: 0.0,
            paymentDueDate = dueDateStr,
            paymentDueMillis = dueDateMillis,
            statementMonth = statementMonthMillis
        )
    }

    /**
     * Parses transactions and supports multiline details
     */
    fun parseTransactions(statementText: String): List<TransactionEntity> {
        val lines = statementText.lines()
        val transactions = mutableListOf<TransactionEntity>()

        val startPattern =
            Regex("""^\d{1,2} (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) '\d{2}""")
        val endPattern = Regex("""(Debit|Credit)$""", RegexOption.IGNORE_CASE)

        var i = lines.indexOfFirst { TRANSACTION_SUMMARY_HEADER.containsMatchIn(it) }
        if (i == -1) i = 0

        while (i < lines.size) {
            val line = lines[i].trim()

            if (startPattern.containsMatchIn(line)) {
                // Accumulate full transaction lines until one end with Debit or Credit
                val builder = StringBuilder(line)
                var j = i + 1
                var ended = endPattern.containsMatchIn(line)

                while (j < lines.size && !ended) {
                    val nextLine = lines[j].trim()
                    builder.append(" ").append(nextLine)
                    ended = endPattern.containsMatchIn(nextLine)
                    j++
                }

                val fullTransaction = builder.toString().replace("\\s+".toRegex(), " ").trim()

                // Process fullTransaction line with transactionLineRegex
                val match = transactionLineRegex.matchEntire(fullTransaction)

                if (match != null) {
                    val (dateStr, detailsStart, amountStr, dc) = match.destructured
                    val txnDate = try {
                        dateFormat.parse(dateStr)
                    } catch (e: Exception) {
                        null
                    }
                    val amount = amountStr.replace(",", "").toDoubleOrNull() ?: 0.0
                    val txnType = if (dc.equals(
                            "Credit",
                            ignoreCase = true
                        )
                    ) TransactionType.CREDIT else TransactionType.DEBIT

                    val dateMillis = txnDate?.time ?: 0L
                    val transactionId = AppUtils.generateTransactionId(
                        dateMillis, "AXIS", amount, txnType, detailsStart
                    )

                    val transaction = TransactionEntity(
                        id = 0L,
                        transactionId = transactionId,
                        source = TransactionSource.CC_STATEMENT,
                        txnType = txnType,
                        amount = amount,
                        cashback = 0.0,
                        txnDate = dateMillis,
                        bankName = "AXIS BANK",
                        category = Category.fromKeyword(detailsStart),
                        place = AppUtils.extractPlace(detailsStart) ?: "",
                        description = detailsStart,
                        currencyCode = CurrencyType.INR,
                        note = "",
                        status = TransactionStatus.ACTIVE
                    )
                    transactions.add(transaction)
                }

                i = j
            } else {
                i++
            }
        }

        return transactions
    }
}