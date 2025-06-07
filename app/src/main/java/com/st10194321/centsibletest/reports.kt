package com.st10194321.centsibletest

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.st10194321.centsibletest.databinding.ActivityReportsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.DateFormatSymbols
import java.util.*
import com.st10194321.centsibletest.formatInSelectedCurrency

class reports : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private val cal  = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If you haven’t yet initialized CurrencyRepository in your Application subclass,
        // you can do it here (once) before any calls to formatInSelectedCurrency:
        // CurrencyRepository.initialize(applicationContext)

        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // Back button closes Activity
        binding.btnBack.setOnClickListener { finish() }

        // Initialize month label
        updateMonthText()

        // Prev / Next month listeners
        binding.btnPrevMonth.setOnClickListener {
            cal.add(Calendar.MONTH, -1)
            updateMonthText()
            loadPieChartDataForCurrentMonth()
            loadBarChartDataForRecentMonths()
            loadGoalsBarChart()
        }
        binding.btnNextMonth.setOnClickListener {
            cal.add(Calendar.MONTH, +1)
            updateMonthText()
            loadPieChartDataForCurrentMonth()
            loadBarChartDataForRecentMonths()
            loadGoalsBarChart()
        }

        // Initial data load
        loadPieChartDataForCurrentMonth()
        loadBarChartDataForRecentMonths()
        loadGoalsBarChart()
    }

    private fun updateMonthText() {
        val monthName = DateFormatSymbols().months[cal.get(Calendar.MONTH)]
        val year = cal.get(Calendar.YEAR)
        binding.tvCurrentMonth.text = "$monthName $year"
    }

    // ──────────────────────────────────────────────────────────
    //  PIE CHART + LEGEND
    // ──────────────────────────────────────────────────────────
    private fun loadPieChartDataForCurrentMonth() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(uid)
            .collection("categories")
            .get()
            .addOnSuccessListener { catSnap ->
                val categoryNames = catSnap.documents.mapNotNull { it.getString("name") }
                if (categoryNames.isEmpty()) {
                    Toast.makeText(this, "No categories found", Toast.LENGTH_SHORT).show()
                    clearPieChart()
                    return@addOnSuccessListener
                }

                val totalsPerCategory = mutableMapOf<String, Double>()
                var completedQueries = 0

                categoryNames.forEach { catName ->
                    db.collection("users").document(uid)
                        .collection("transactions")
                        .whereEqualTo("category", catName)
                        .get()
                        .addOnSuccessListener { txSnap ->
                            var sum = 0.0
                            for (tx in txSnap.documents) {
                                val dateStr = tx.getString("date") ?: continue
                                val parts = dateStr.split("/")
                                if (parts.size == 3) {
                                    val month = parts[1].toIntOrNull()?.minus(1) ?: continue
                                    val year = parts[2].toIntOrNull() ?: continue
                                    if (month == cal.get(Calendar.MONTH) &&
                                        year == cal.get(Calendar.YEAR)
                                    ) {
                                        sum += tx.getDouble("amount") ?: 0.0
                                    }
                                }
                            }
                            totalsPerCategory[catName] = sum
                            completedQueries++

                            if (completedQueries == categoryNames.size) {
                                drawPieChart(totalsPerCategory)
                                buildPieLegend(totalsPerCategory)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(
                                "Reports",
                                "Failed to load transactions for $catName",
                                e
                            )
                            completedQueries++
                            if (completedQueries == categoryNames.size) {
                                drawPieChart(totalsPerCategory)
                                buildPieLegend(totalsPerCategory)
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ReportsActivity", "Failed to load categories", e)
                Toast.makeText(this, "Unable to load categories", Toast.LENGTH_SHORT).show()
                clearPieChart()
            }
    }

    private fun clearPieChart() {
        binding.pieChart.clear()
        binding.cellFood.visibility = View.GONE
        binding.cellCar.visibility = View.GONE
        binding.cellMedical.visibility = View.GONE
        binding.cellSaving.visibility = View.GONE

        binding.pieChart.setNoDataText("No data for this month")
        binding.pieChart.invalidate()
    }

    private fun drawPieChart(dataMap: Map<String, Double>) {
        binding.pieChart.clear()

        val entries = dataMap.map { (catName, totalZar) ->
            PieEntry(totalZar.toFloat(), catName)
        }.filter { it.value > 0f }

        if (entries.isEmpty()) {
            binding.pieChart.setNoDataText("No spending in this month")
            binding.pieChart.invalidate()
            return
        }

        val dataSet = PieDataSet(entries, "").apply {
            sliceSpace = 4f
            selectionShift = 5f
            colors = listOf(
                Color.parseColor("#A1DC7C"), // Food
                Color.parseColor("#344BFD"), // Car
                Color.parseColor("#F68D2B"), // Medical
                Color.parseColor("#D33232"), // Saving
                Color.parseColor("#AE6CFF")  // Extra
            )
            setDrawValues(true)
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            valueFormatter = PercentFormatter(binding.pieChart)
        }

        val pieData = PieData(dataSet)

        binding.pieChart.apply {
            data = pieData
            setUsePercentValues(true)
            setExtraOffsets(0f, 0f, 0f, 0f)

            isDrawHoleEnabled = true
            holeRadius = 70f
            transparentCircleRadius = 73f
            setHoleColor(Color.parseColor("#1E1E1E"))

            setDrawCenterText(true)
            // ← REPLACED: Use formatInSelectedCurrency instead of ZAR hard‐coded NumberFormat
            val totalSpentZar = dataMap.values.sum()
            centerText = formatInSelectedCurrency(totalSpentZar)
            setCenterTextSize(18f)
            setCenterTextColor(Color.WHITE)

            setEntryLabelColor(Color.TRANSPARENT)
            setEntryLabelTextSize(0f)

            legend.isEnabled = false
            description.isEnabled = false

            setTouchEnabled(false)
            animateY(800)
            invalidate()
        }
    }

    private fun buildPieLegend(dataMap: Map<String, Double>) {
        binding.cellFood.visibility = View.GONE
        binding.cellCar.visibility = View.GONE
        binding.cellMedical.visibility = View.GONE
        binding.cellSaving.visibility = View.GONE

        val nonZeroList = dataMap.entries
            .filter { it.value > 0.0 }
            .map { Pair(it.key, it.value) }

        val colors = listOf(
            Color.parseColor("#A1DC7C"),
            Color.parseColor("#344BFD"),
            Color.parseColor("#F68D2B"),
            Color.parseColor("#D33232"),
            Color.parseColor("#AE6CFF")
        )

        for (i in nonZeroList.indices) {
            if (i >= 4) break
            val (catName, totalZar) = nonZeroList[i]
            val color = colors[i]

            // ← REPLACED: Convert totalZar → chosen currency instead of hard‐coded ZAR
            val formattedTotal = formatInSelectedCurrency(totalZar)

            when (i) {
                0 -> {
                    binding.cellFood.visibility = View.VISIBLE
                    binding.dotFood.setBackgroundColor(color)
                    binding.tvLegendFood.text = "$catName   $formattedTotal"
                }
                1 -> {
                    binding.cellCar.visibility = View.VISIBLE
                    binding.dotCar.setBackgroundColor(color)
                    binding.tvLegendCar.text = "$catName   $formattedTotal"
                }
                2 -> {
                    binding.cellMedical.visibility = View.VISIBLE
                    binding.dotMedical.setBackgroundColor(color)
                    binding.tvLegendMedical.text = "$catName   $formattedTotal"
                }
                3 -> {
                    binding.cellSaving.visibility = View.VISIBLE
                    binding.dotSaving.setBackgroundColor(color)
                    binding.tvLegendSaving.text = "$catName   $formattedTotal"
                }
            }
        }
    }

    // ──────────────────────────────────────────────────────────
    //  BAR CHART (Budget vs Expense)
    // ──────────────────────────────────────────────────────────
    private fun loadBarChartDataForRecentMonths() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show()
            clearBarChart()
            return
        }

        // Launch a coroutine on IO to fetch Firestore data
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val monthYearsToFetch = getFiveCenteredMonthYears()
                val monthLabels = getFiveCenteredMonthShortLabels()

                val budgetValues = mutableListOf<Float>()
                val expenseValues = mutableListOf<Float>()

                val allCategoriesSnapshot =
                    db.collection("users").document(uid).collection("categories").get().await()
                val allTransactionsSnapshot =
                    db.collection("users").document(uid).collection("transactions").get().await()

                for ((targetMonth, targetYear) in monthYearsToFetch) {
                    var totalBudgetLimit = 0.0
                    var totalExpense = 0.0

                    for (doc in allCategoriesSnapshot.documents) {
                        val occurrenceDateStr = doc.getString("occurrence")
                        val amountZar = doc.getLong("amount")?.toDouble() ?: 0.0
                        if (!occurrenceDateStr.isNullOrBlank()) {
                            val parts = occurrenceDateStr.split("/")
                            if (parts.size == 3) {
                                val catMonth = parts[1].toIntOrNull()?.minus(1) ?: continue
                                val catYear = parts[2].toIntOrNull() ?: continue
                                if (catMonth == targetMonth && catYear == targetYear) {
                                    totalBudgetLimit += amountZar
                                }
                            }
                        }
                    }
                    budgetValues.add(totalBudgetLimit.toFloat())

                    for (doc in allTransactionsSnapshot.documents) {
                        val dateStr = doc.getString("date")
                        val txAmtZar = doc.getDouble("amount") ?: 0.0
                        if (!dateStr.isNullOrBlank()) {
                            val parts = dateStr.split("/")
                            if (parts.size == 3) {
                                val txMonth = parts[1].toIntOrNull()?.minus(1) ?: continue
                                val txYear = parts[2].toIntOrNull() ?: continue
                                if (txMonth == targetMonth && txYear == targetYear) {
                                    totalExpense += txAmtZar
                                }
                            }
                        }
                    }
                    expenseValues.add(totalExpense.toFloat())
                }

                // Switch to Main thread for UI updates
                launch(Dispatchers.Main) {
                    if (budgetValues.all { it == 0f } && expenseValues.all { it == 0f }) {
                        binding.barChart.setNoDataText("No budget or expense data for recent months.")
                        binding.barChart.invalidate()

                        // ← REPLACED: Use formatInSelectedCurrency(0.0)
                        val zeroFormatted = formatInSelectedCurrency(0.0)
                        binding.tvTotalBudgetValue.text = zeroFormatted
                        binding.tvTotalExpenseValue.text = zeroFormatted
                    } else {
                        drawBarChart(binding.barChart, monthLabels, budgetValues, expenseValues)

                        // For the “center” month’s values:
                        val centerIndex = 2
                        val centralBudgetZar = budgetValues[centerIndex].toDouble()
                        val centralExpenseZar = expenseValues[centerIndex].toDouble()

                        // ← REPLACED: Convert each ZAR value to chosen currency
                        binding.tvTotalBudgetValue.text =
                            formatInSelectedCurrency(centralBudgetZar)
                        binding.tvTotalExpenseValue.text =
                            formatInSelectedCurrency(centralExpenseZar)
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportsActivity", "Error loading bar chart data: ${e.message}", e)
                launch(Dispatchers.Main) {
                    Toast.makeText(
                        this@reports,
                        "Error loading bar chart data",
                        Toast.LENGTH_SHORT
                    ).show()
                    clearBarChart()
                }
            }
        }
    }

    private fun clearBarChart() {
        binding.barChart.clear()
        binding.barChart.setNoDataText("No data available.")
        binding.barChart.invalidate()
    }

    private fun getFiveCenteredMonthYears(): List<Pair<Int, Int>> {
        val base = cal.clone() as Calendar
        base.add(Calendar.MONTH, -2)
        val result = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until 5) {
            result.add(Pair(base.get(Calendar.MONTH), base.get(Calendar.YEAR)))
            base.add(Calendar.MONTH, 1)
        }
        return result
    }

    private fun getFiveCenteredMonthShortLabels(): List<String> {
        val monthYears = getFiveCenteredMonthYears()
        return monthYears.map { (m, _) ->
            DateFormatSymbols().shortMonths[m]
        }
    }

    private fun drawBarChart(
        barChart: BarChart,
        monthLabels: List<String>,
        budgetValues: List<Float>,
        expenseValues: List<Float>
    ) {
        if (monthLabels.size != 5 ||
            budgetValues.size != 5 ||
            expenseValues.size != 5) {
            Log.e("reports", "drawBarChart: Expected exactly 5 entries.")
            clearBarChart()
            return
        }

        val budgetEntries = mutableListOf<BarEntry>()
        val expenseEntries = mutableListOf<BarEntry>()
        for (i in 0 until 5) {
            budgetEntries.add(BarEntry(i.toFloat(), budgetValues[i]))
            expenseEntries.add(BarEntry(i.toFloat(), expenseValues[i]))
        }

        val budgetDataSet = BarDataSet(budgetEntries, "Total Budget").apply {
            color = Color.parseColor("#A1DC7C")
            setDrawValues(false)
        }
        val expenseDataSet = BarDataSet(expenseEntries, "Expense").apply {
            color = Color.parseColor("#D33232")
            setDrawValues(false)
        }

        val barData = BarData(budgetDataSet, expenseDataSet).apply {
            barWidth = 0.3f
        }
        val groupSpace = 0.2f
        val barSpace = 0.05f
        val groupWidth = barData.getGroupWidth(groupSpace, barSpace)
        barData.groupBars(groupWidth, groupSpace, barSpace)

        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            textColor = Color.WHITE
            setCenterAxisLabels(true)
            axisMinimum = 0f
            axisMaximum = groupWidth * 5.5f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val idx = value.toInt()
                    return if (idx in 0..4) monthLabels[idx] else ""
                }
            }
        }

        barChart.apply {
            data = barData
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setTouchEnabled(true)
            axisLeft.apply {
                textColor = Color.WHITE
                setDrawGridLines(true)
                gridColor = Color.parseColor("#444444")
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
            legend.isEnabled = false
            invalidate()
        }
    }

    // ──────────────────────────────────────────────────────────
    //  NEW: Goals Bar Chart (Min vs Max)
    // ──────────────────────────────────────────────────────────
    private fun loadGoalsBarChart() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show()
            clearGoalsBarChart()
            return
        }

        db.collection("users").document(uid)
            .collection("goals")
            .get()
            .addOnSuccessListener { goalsSnap ->
                if (goalsSnap.isEmpty) {
                    clearGoalsBarChart()
                    return@addOnSuccessListener
                }

                val monthNameToIndex = DateFormatSymbols().months
                    .mapIndexed { idx, m -> m.lowercase(Locale.getDefault()) to idx }
                    .toMap()

                val entriesMin = mutableListOf<BarEntry>()
                val entriesMax = mutableListOf<BarEntry>()
                val labelsSet = mutableSetOf<Int>()

                for (gDoc in goalsSnap.documents) {
                    val monthStr = gDoc.getString("month")?.lowercase(Locale.getDefault()) ?: continue
                    val minGoalZar = (gDoc.getLong("minimumgoal") ?: 0L).toFloat()
                    val maxGoalZar = (gDoc.getLong("maximumgoal") ?: 0L).toFloat()
                    val monthIdx = monthNameToIndex[monthStr] ?: continue
                    labelsSet.add(monthIdx)
                    entriesMin.add(BarEntry(monthIdx.toFloat(), minGoalZar))
                    entriesMax.add(BarEntry(monthIdx.toFloat(), maxGoalZar))
                }

                if (entriesMin.isEmpty() && entriesMax.isEmpty()) {
                    clearGoalsBarChart()
                    return@addOnSuccessListener
                }

                val sortedIndices = labelsSet.toList().sorted()
                drawGoalsBarChart(entriesMin, entriesMax, sortedIndices)
            }
            .addOnFailureListener { e ->
                Log.e("Reports", "Failed to load goals for chart", e)
                clearGoalsBarChart()
            }
    }

    private fun clearGoalsBarChart() {
        binding.barChartGoals.clear()
        binding.barChartGoals.setNoDataText("No goals set")
        binding.barChartGoals.invalidate()
    }

    private fun drawGoalsBarChart(
        minEntries: List<BarEntry>,
        maxEntries: List<BarEntry>,
        monthIndicesSorted: List<Int>
    ) {
        val minDataSet = BarDataSet(minEntries, "Min Goal").apply {
            color = Color.parseColor("#344BFD")
            valueTextColor = Color.WHITE
            valueTextSize = 10f
        }
        val maxDataSet = BarDataSet(maxEntries, "Max Goal").apply {
            color = Color.parseColor("#A1DC7C")
            valueTextColor = Color.WHITE
            valueTextSize = 10f
        }

        val barData = BarData(minDataSet, maxDataSet).apply {
            barWidth = 0.4f
        }
        val groupSpace = 0.2f
        val barSpace = 0.05f
        barData.groupBars(0f, groupSpace, barSpace)

        binding.barChartGoals.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            textColor = Color.WHITE
            axisMinimum = 0f
            axisMaximum = 11f + 1f
            setCenterAxisLabels(false)

            val shortMonths = DateFormatSymbols().shortMonths
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val idx = value.toInt()
                    return if (idx in monthIndicesSorted) {
                        shortMonths.getOrNull(idx) ?: ""
                    } else {
                        ""
                    }
                }
            }
        }

        binding.barChartGoals.apply {
            data = barData
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setTouchEnabled(false)

            axisLeft.apply {
                textColor = Color.WHITE
                setDrawGridLines(true)
                gridColor = Color.parseColor("#444444")
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
            legend.apply {
                textColor = Color.WHITE
                isEnabled = true
            }

            invalidate()
        }
    }
}
