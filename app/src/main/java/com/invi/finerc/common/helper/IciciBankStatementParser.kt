package com.invi.finerc.common.helper


import com.invi.finerc.common.AppUtils
import com.invi.finerc.data.entity.BillCycleEntity
import com.invi.finerc.data.entity.BillCycleWithTransactions
import com.invi.finerc.data.entity.CreditCardEntity
import com.invi.finerc.data.entity.TransactionEntity
import com.invi.finerc.domain.models.BillCycleStatus
import com.invi.finerc.domain.models.Category
import com.invi.finerc.domain.models.CurrencyType
import com.invi.finerc.domain.models.TransactionSource
import com.invi.finerc.domain.models.TransactionStatus
import com.invi.finerc.domain.models.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale

class IciciBankStatementParser {

    private val dateFormat = SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH)

    private val cardNumberRegex = Regex("""(\d{4}\s*XXXX\s*XXXX\s*\d{4})""")
    private val cardHolderRegex = Regex("""(?m)^Customer Name\s*\n([^\n]+)""")
    private val creditLimitRegex =
        Regex("""Credit Limit\s*₹\s*([\d,]+\.\d{2})""") // Adjust if needed
    private val transactionRegex = Regex(
        """
    ^                               # Start of line
    (                               # Group 1: Date
      \d{2}/\d{2}/\d{4}             # Format: dd/mm/yyyy
      |                             # OR
      \d{2}-[A-Z]{3}-\d{2}          # Format: dd-MMM-yy (3 letters month)
    )
    \s+                             # One or more spaces
    (\d+)                           # Group 2: long numeric ID (digits only)
    \s+                             # One or more spaces
    (.+?)                           # Group 3: Description (lazy match)
    \s+                             # One or more spaces
    (\d+(?:\.\d{2})?)               # Group 4: first decimal number (any digits before decimal, optional .xx)
    \s+                             # One or more spaces
    (\d+(?:\.\d{2})?)               # Group 5: last decimal number (same pattern)
    $                               # End of line
    """.trimIndent(),
        setOf(RegexOption.IGNORE_CASE, RegexOption.COMMENTS)
    )


    fun extractCardNumber(text: String): String =
        cardNumberRegex.find(text)?.groupValues?.get(1) ?: ""

    fun extractCreditLimit(text: String): Double {
        val raw = creditLimitRegex.find(text)?.groupValues?.get(1)?.replace(",", "") ?: "0.0"
        return raw.toDoubleOrNull() ?: 0.0
    }

    fun parseTransactions(lines: List<String>): List<TransactionEntity> {
        val transactions = mutableListOf<TransactionEntity>()
        val startPattern = Regex("""^\d{2}-[A-Z]{3}-\d{2}""")

        val startPattern1 = Regex("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}.*$")

        val endPattern =
            Regex("""[\d,]+\.\d{2}(?:\s+[A-Z]{1,2})?$""")  // line ending with amount like 51.75 or 1,026.99

        var i = lines.indexOfFirst { cardNumberRegex.containsMatchIn(it) }

        while (i < lines.size) {
            val line = lines[i].trim()

            if (startPattern.containsMatchIn(line) || startPattern1.containsMatchIn(line)) {
                val builder = StringBuilder(line)
                var j = i + 1
                var ended = endPattern.containsMatchIn(line)

                // Accumulate lines until a line ends with amount (endPattern)
                while (j < lines.size && !ended) {
                    val nextLine = lines[j].trim()
                    builder.append(" ").append(nextLine)
                    ended = endPattern.containsMatchIn(nextLine)
                    j++
                }

                var fullTransaction = builder.toString().replace("\\s+".toRegex(), " ").trim().replace(",", "")

                var txnType: TransactionType = TransactionType.UN_ASSIGNED
                if(fullTransaction.endsWith("CR"))
                {
                    txnType = TransactionType.CREDIT
                    fullTransaction = fullTransaction.split("CR")[0]
                }

                val match = transactionRegex.matchEntire(fullTransaction.trim())
                if (match != null) {
                    val (dateStr, refNum, details, intlAmt, amountStr) = match.destructured
                    var txnDate = try {
                        SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH).parse(dateStr)
                    } catch (e: Exception) {
                        null
                    }
                    if (txnDate == null) {
                        txnDate = try {
                            SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(dateStr)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    val amount = amountStr.replace(",", "").toDoubleOrNull() ?: 0.0
                    if (txnType != TransactionType.UN_ASSIGNED)
                    {
                        txnType =  if (amount >= 0) TransactionType.DEBIT else TransactionType.CREDIT
                    }

                    val transactionId = AppUtils.generateTransactionId(
                        txnDate?.time ?: 0L, "ICICI", amount, txnType, refNum
                    )

                    val transaction = TransactionEntity(
                        id = 0L,
                        transactionId = transactionId,
                        source = TransactionSource.CC_STATEMENT,
                        txnType = txnType,
                        amount = kotlin.math.abs(amount),
                        cashback = 0.0,
                        txnDate = txnDate?.time ?: 0L,
                        bankName = "ICICI BANK",
                        category = Category.fromKeyword(details),
                        place = AppUtils.extractPlace(details) ?: "",
                        description = "$refNum - $details",
                        currencyCode = CurrencyType.INR,
                        note = details,
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

    fun parseStatement(
        combinedText: String, lines: List<String>
    ): Pair<CreditCardEntity, List<BillCycleWithTransactions>> {
        val creditCard = CreditCardEntity(
            bankName = "ICICI BANK",
            cardNumberMasked = extractCardNumber(combinedText),
            cardType = "CREDIT",
            cardHolderName = lines[1],
            creditLimit = extractCreditLimit(combinedText)
        )

        val transactions = parseTransactions(lines)

        val billCycle = BillCycleEntity(
            cardId = 0L, startDate = 0L, endDate = 0L, dueDate = 0L, status = BillCycleStatus.OPEN
        )

        return creditCard to listOf(
            BillCycleWithTransactions(
                billCycle = billCycle, transactions = transactions
            )
        )
    }
}