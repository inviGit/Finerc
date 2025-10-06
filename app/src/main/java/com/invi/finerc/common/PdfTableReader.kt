package com.invi.finerc.common

import android.content.Context
import com.invi.finerc.common.helper.AxisBankStatementParser
import com.invi.finerc.common.helper.IciciBankStatementParser
import com.invi.finerc.data.entity.BillCycleEntity
import com.invi.finerc.data.entity.BillCycleWithTransactions
import com.invi.finerc.data.entity.CreditCardWithCyclesAndTransactions
import com.invi.finerc.domain.models.BillCycleStatus
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class PdfTableReader(private val context: Context) {

    init {
        try {
            PDFBoxResourceLoader.init(context)
            println("✅ PDFBoxResourceLoader initialized")
        } catch (e: Exception) {
            println("❌ PDFBoxResourceLoader init failed: ${e.message}")
        }
    }

    /**
     * Reads tables/text content from PDF pages asynchronously
     * Assumes PDF is not password protected or password is provided
     */
    suspend fun readPdfTables(
        pdfInputStream: InputStream,
        password: String? = null,
        onProgress: ((String) -> Unit)? = null
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val tempFile = createTempFile(pdfInputStream)

            val document = if (!password.isNullOrBlank()) {
                PDDocument.load(tempFile, password)
            } else {
                PDDocument.load(tempFile)
            }

            val numberOfPages = document.numberOfPages
            onProgress?.invoke("PDF loaded: $numberOfPages pages found")

            val pageTexts = mutableListOf<String>()

            for (pageIndex in 0 until numberOfPages) {
                onProgress?.invoke("Extracting text from page ${pageIndex + 1}...")

                val page = document.getPage(pageIndex)

                // Use PDFTextStripper to extract text from page (optionally configure for table hints)
                val textStripper = com.tom_roush.pdfbox.text.PDFTextStripper()
                textStripper.startPage = pageIndex + 1
                textStripper.endPage = pageIndex + 1

                val pageText = textStripper.getText(document)

                pageTexts.add(pageText)

                onProgress?.invoke("Extracted ${pageText.length} chars from page ${pageIndex + 1}")
            }

            document.close()
            tempFile.delete()

            Result.success(pageTexts)

        } catch (e: Exception) {
            println("❌ PDF table extraction failed: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun createTempFile(inputStream: InputStream): File =
        withContext(Dispatchers.IO) {
            val tempFile = File.createTempFile("pdf_temp", ".pdf", context.cacheDir)
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            tempFile
        }

    fun parseAxisBankStatement(
        combinedText: String
    ): CreditCardWithCyclesAndTransactions {
        println("🏦 Parsing Axis Bank statement...")
        val parser = AxisBankStatementParser()
        val lines = combinedText.lines()

        val paymentSummary = parser.extractPaymentSummary(combinedText)
        val dates = listOf(
            "01/09/2025", "30/09/2025"
        ) // Replace with actual bill cycle dates extracted from statement if available

        val creditCard = parser.parseCreditCardEntity(combinedText, lines, paymentSummary)

        val transactions = parser.parseTransactions(combinedText)

        val billCycle = BillCycleEntity(
            cardId = 0L,
            startDate = AppUtils.parseDateToMillis(dates[0]),
            endDate = AppUtils.parseDateToMillis(dates[1]),
            dueDate = paymentSummary.paymentDueMillis,
            statementAmount = paymentSummary.totalPaymentDue,
            statementMonth = paymentSummary.statementMonth,
            minDueAmount = paymentSummary.minimumPaymentDue,
            paidAmount = 0.0,
            status = BillCycleStatus.OPEN
        )
        val cyclesWithTransactions = listOf(
            BillCycleWithTransactions(
                billCycle = billCycle, transactions = transactions
            )
        )

        return CreditCardWithCyclesAndTransactions(
            creditCard = creditCard, cyclesWithTransactions = cyclesWithTransactions
        )
    }

    fun parseHdfcBankStatement(
        combinedText: String
    ): CreditCardWithCyclesAndTransactions {
        throw NotImplementedError("HDFC Bank parsing not yet implemented")
    }

    fun parseIciciBankStatement(
        combinedText: String
    ): CreditCardWithCyclesAndTransactions {
        val parser = IciciBankStatementParser()
        val lines = combinedText.lines()

        val (creditCard, cyclesWithTransactions) = parser.parseStatement(combinedText, lines)

        return CreditCardWithCyclesAndTransactions(
            creditCard = creditCard, cyclesWithTransactions = cyclesWithTransactions
        )
    }

    fun parseSbiBankStatement(
        combinedText: String
    ): CreditCardWithCyclesAndTransactions {
        throw NotImplementedError("SBI Bank parsing not yet implemented")
    }

}
