object TipEngine {
    data class Signals(
        val pctUsed: Double,
        val pctSaved: Double,
        val categoryOverspend: Map<String,Double>,
        val momPctChange: Double?,
        val dailyBudget: Double?,
        val nOverspentCats: Int
    )

    fun generateTips(sig: Signals): List<String> {
        val tips = mutableListOf<String>()

        // 1) Overall budget tips
        if (sig.pctUsed > 100) {
            tips += "You’re over budget by ${"%.0f".format(sig.pctUsed-100)}% — try a no-spend weekend."
        } else if (sig.pctUsed > 90) {
            tips += "You’ve used ${"%.0f".format(sig.pctUsed)}% of your income — slow down spending now."
        }

        // 2) Savings tips
        if (sig.pctSaved < 10) {
            tips += "Your savings rate is only ${"%.0f".format(sig.pctSaved)}% — auto-save 5% each paycheck?"
        } else {
            tips += "Nice, you're saving ${"%.0f".format(sig.pctSaved)}% of your income!"
        }

        // 3) Category overspend
        sig.categoryOverspend.forEach { (cat,pct) ->
            if (pct > 110)
                tips += "Cut back on $cat — it’s ${"%.0f".format(pct-100)}% over budget."
            else if (pct > 90)
                tips += "$cat is at ${"%.0f".format(pct)}% — keep an eye on it."
        }

        // 4) Month-over-month trend
        sig.momPctChange?.let { delta ->
            if (delta > 10)
                tips += "Your spending increased ${"%.0f".format(delta)}% since last month."
            else if (delta < -10)
                tips += "Great — you cut spending by ${"%.0f".format(-delta)}% from last month."
        }

        // 5) Daily budget pacing
        sig.dailyBudget?.let { dbud ->
            tips += "You can only spend R${"%.2f".format(dbud)}/day to stay on track."
        }

        // 6) Too many bad categories
        if (sig.nOverspentCats >= 2) {
            tips += "Multiple categories overspent—consider a weekly review of your budgets."
        }

        // 7) Fallback
        if (tips.isEmpty()) {
            tips += "All clear—keep up the good work!"
        }

        return tips
    }
}
