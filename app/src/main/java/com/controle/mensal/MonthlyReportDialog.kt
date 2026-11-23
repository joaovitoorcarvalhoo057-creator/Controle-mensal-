
package com.controle.mensal

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import java.util.Calendar

object MonthlyReportDialog {

    fun show(
        context: Context,
        reportManager: MonthlyReportManager
    ) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, year, month, _ ->

                val report = reportManager.getReport(
                    month = month,
                    year = year
                )

                val reportText =
                    MonthlyReportFormatter.format(report)

                AlertDialog.Builder(context)
                    .setTitle("Relatório mensal")
                    .setMessage(reportText)
                    .setNeutralButton("Compartilhar") { _, _ ->

                        val shareIntent = Intent(
                            Intent.ACTION_SEND
                        ).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_SUBJECT,
                                "Relatório mensal de pamonhas"
                            )
                            putExtra(
                                Intent.EXTRA_TEXT,
                                reportText
                            )
                        }

                        context.startActivity(
                            Intent.createChooser(
                                shareIntent,
                                "Compartilhar relatório"
                            )
                        )
                    }
                    .setPositiveButton("Fechar", null)
                    .show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setTitle("Escolha o mês do relatório")
            show()
        }
    }
}
