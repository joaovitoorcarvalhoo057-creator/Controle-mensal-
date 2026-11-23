
package com.controle.mensal

import android.content.Context
import androidx.appcompat.app.AlertDialog

object MonthlyReportDialog {

    fun show(
        context: Context,
        reportManager: MonthlyReportManager
    ) {
        val report = reportManager.getCurrentMonthReport()
        val reportText = MonthlyReportFormatter.format(report)

        AlertDialog.Builder(context)
            .setTitle("Relatório do mês")
            .setMessage(reportText)
            .setPositiveButton("Fechar", null)
            .show()
    }
}
