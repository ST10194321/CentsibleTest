// src/main/java/com/st10194321/centsibletest/reports.kt
package com.st10194321.centsibletest

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.st10194321.centsibletest.databinding.ActivityReportsBinding
import kotlinx.coroutines.CoroutineScope
import com.github.mikephil.charting.components.LimitLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class reports : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private val cal  = Calendar.getInstance()

    // ─── user‐selected range ───────────────────────────────────
    private var rangeStartMillis: Long? = null
    private var rangeEndMillis:   Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // Back button closes Activity
        binding.btnBack.setOnClickListener {
            val up = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(up)
            finish()
        }

        // Month navigation
        updateMonthText()
        binding.btnPrevMonth.setOnClickListener {
            cal.add(Calendar.MONTH, -1)
            clearRange()
            refreshAllCharts()
        }
        binding.btnNextMonth.setOnClickListener {
            cal.add(Calendar.MONTH, +1)
            clearRange()
            refreshAllCharts()
        }

        // ─── Date-range picker on the month label ────────────────
        binding.tvCurrentMonth.setOnClickListener {
            val picker = MaterialDatePicker.Builder
                .dateRangePicker()
                .setTitleText("Select start & end dates")
                .build()

            picker.show(supportFragmentManager, "DATE_RANGE_PICKER")

            picker.addOnPositiveButtonClickListener { range: Pair<Long, Long> ->
                rangeStartMillis = range.first
                rangeEndMillis   = range.second

                val fmt   = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val start = fmt.format(Date(range.first))
                val end   = fmt.format(Date(range.second))
                binding.tvCurrentMonth.text = "$start – $end"

                refreshAllCharts()
            }
        }

        // Initial load
        refreshAllCharts()
    }

    private fun clearRange() {
        rangeStartMillis = null
        rangeEndMillis   = null
    }

    private fun updateMonthText() {
        val monthName = DateFormatSymbols().months[cal.get(Calendar.MONTH)]
        val year      = cal.get(Calendar.YEAR)
        binding.tvCurrentMonth.text = "$monthName $year"
    }

    private fun refreshAllCharts() {
        val start = rangeStartMillis
        val end   = rangeEndMillis
        if (start != null && end != null) {
            loadPieChartDataForRange(start, end)
            loadBarChartDataForRange(start, end)

        } else {
            updateMonthText()
            loadPieChartDataForCurrentMonth()
            loadBarChartDataForRecentMonths()

        }
    }

    // ──────────────────────────────────────────────────────────
    //  DATE‐RANGE PIE CHART
    // ──────────────────────────────────────────────────────────
    private fun loadPieChartDataForRange(start: Long, end: Long) {
        val uid = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show()
            return
        }
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        db.collection("users").document(uid)
            .collection("categories")
            .get()
            .addOnSuccessListener { catSnap ->
                val names = catSnap.documents.mapNotNull { it.getString("name") }
                if (names.isEmpty()) {
                    clearPieChart()
                    return@addOnSuccessListener
                }
                val totals = mutableMapOf<String, Double>()
                var done = 0

                names.forEach { cat ->
                    db.collection("users").document(uid)
                        .collection("transactions")
                        .whereEqualTo("category", cat)
                        .get()
                        .addOnSuccessListener { txSnap ->
                            var sum = 0.0
                            for (tx in txSnap.documents) {
                                val ds = tx.getString("date") ?: continue
                                val d  = sdf.parse(ds) ?: continue
                                if (d.time in start..end) {
                                    sum += tx.getDouble("amount") ?: 0.0
                                }
                            }
                            totals[cat] = sum
                            done++
                            if (done == names.size) {
                                drawPieChart(totals)
                                buildPieLegend(totals)
                            }
                        }
                        .addOnFailureListener {
                            done++
                            if (done == names.size) {
                                drawPieChart(totals)
                                buildPieLegend(totals)
                            }
                        }
                }
            }
            .addOnFailureListener { clearPieChart() }
    }

    // ──────────────────────────────────────────────────────────
    //  DATE‐RANGE BAR CHART
    // ──────────────────────────────────────────────────────────
    private fun loadBarChartDataForRange(start: Long, end: Long) {
        val uid = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show()
            clearBarChart()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Prepare month‐labels and sums
                val monthPairs = getMonthYearPairs(start, end)
                val labels     = monthPairs.map { DateFormatSymbols().shortMonths[it.first] }
                val budgets    = mutableListOf<Float>()
                val expenses   = mutableListOf<Float>()

                // 2. Fetch all categories & transactions once
                val catSnap = db.collection("users").document(uid)
                    .collection("categories").get().await()
                val txSnap  = db.collection("users").document(uid)
                    .collection("transactions").get().await()
                val sdf     = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                // 3. Accumulate per‐month sums
                monthPairs.forEach { pair ->
                    val m = pair.first
                    val y = pair.second

                    var bSum = 0.0
                    var eSum = 0.0

                    // budget limits
                    for (doc in catSnap.documents) {
                        val occ   = doc.getString("occurrence") ?: continue
                        val parts = occ.split("/")
                        if (parts.size == 3) {
                            val catM = parts[1].toIntOrNull()?.minus(1) ?: continue
                            val catY = parts[2].toIntOrNull() ?: continue
                            if (catM == m && catY == y) {
                                bSum += doc.getLong("amount")?.toDouble() ?: 0.0
                            }
                        }
                    }
                    budgets.add(bSum.toFloat())

                    // transaction sums
                    for (doc in txSnap.documents) {
                        val ds = doc.getString("date") ?: continue
                        val d  = sdf.parse(ds) ?: continue
                        val cal = Calendar.getInstance().apply { time = d }
                        val txM = cal.get(Calendar.MONTH)
                        val txY = cal.get(Calendar.YEAR)
                        if (d.time in start..end && txM == m && txY == y) {
                            eSum += doc.getDouble("amount") ?: 0.0
                        }
                    }
                    expenses.add(eSum.toFloat())
                }

                // 4. Fetch the goals for the first month in the range
                val firstMonthIdx = monthPairs.first().first
                val goalMonthName = DateFormatSymbols().months[firstMonthIdx]
                val goalSnap = db.collection("users").document(uid)
                    .collection("goals")
                    .whereEqualTo("month", goalMonthName)
                    .get()
                    .await()
                val minG = (goalSnap.documents.firstOrNull()?.getLong("minimumgoal") ?: 0L).toFloat()
                val maxG = (goalSnap.documents.firstOrNull()?.getLong("maximumgoal") ?: 0L).toFloat()

                // 5. Update UI on the main thread
                launch(Dispatchers.Main) {
                    if (budgets.all { it == 0f } && expenses.all { it == 0f }) {
                        clearBarChart()
                        binding.barChart.setNoDataText("No data in this range")
                        binding.barChart.invalidate()
                    } else {
                        drawBarChart(
                            binding.barChart,
                            labels,
                            budgets,
                            expenses,
                            minG,
                            maxG
                        )
                    }
                    val totalB = budgets.sum().toDouble()
                    val totalE = expenses.sum().toDouble()
                    binding.tvTotalBudgetValue.text  = formatInSelectedCurrency(totalB)
                    binding.tvTotalExpenseValue.text = formatInSelectedCurrency(totalE)
                }
            } catch (e: Exception) {
                Log.e("reports", "range bar error", e)
                launch(Dispatchers.Main) {
                    Toast.makeText(this@reports, "Error loading bar chart", Toast.LENGTH_SHORT).show()
                    clearBarChart()
                }
            }
        }
    }


    // ──────────────────────────────────────────────────────────
    //  Helper: months+years between two timestamps
    // ──────────────────────────────────────────────────────────
    private fun getMonthYearPairs(start: Long, end: Long): List<Pair<Int,Int>> {
        val startCal = Calendar.getInstance().apply {
            timeInMillis       = start
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val endCal   = Calendar.getInstance().apply {
            timeInMillis       = end
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val list     = mutableListOf<Pair<Int,Int>>()
        while (!startCal.after(endCal)) {
            list += Pair(startCal.get(Calendar.MONTH), startCal.get(Calendar.YEAR))
            startCal.add(Calendar.MONTH, 1)
        }
        return list
    }

    // ─────────── your original month‐only methods (unchanged) ───────────

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
                                    val year  = parts[2].toIntOrNull() ?: continue
                                    if (month == cal.get(Calendar.MONTH) &&
                                        year  == cal.get(Calendar.YEAR)
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
                            Log.e("reports", "Failed to load transactions for $catName", e)
                            completedQueries++
                            if (completedQueries == categoryNames.size) {
                                drawPieChart(totalsPerCategory)
                                buildPieLegend(totalsPerCategory)
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("reports", "Failed to load categories", e)
                Toast.makeText(this, "Unable to load categories", Toast.LENGTH_SHORT).show()
                clearPieChart()
            }
    }

    private fun clearPieChart() {
        binding.pieChart.clear()
        binding.cellFood.visibility    = View.GONE
        binding.cellCar.visibility     = View.GONE
        binding.cellMedical.visibility = View.GONE
        binding.cellSaving.visibility  = View.GONE

        binding.pieChart.setNoDataText("No data for this month")
        binding.pieChart.invalidate()
    }

    private fun drawPieChart(dataMap: Map<String, Double>) {
        binding.pieChart.clear()

        val entries = dataMap.map { PieEntry(it.value.toFloat(), it.key) }
            .filter { it.value > 0f }

        if (entries.isEmpty()) {
            binding.pieChart.setNoDataText("No spending in this month")
            binding.pieChart.invalidate()
            return
        }

        val dataSet = PieDataSet(entries, "").apply {
            sliceSpace      = 4f
            selectionShift  = 5f
            colors          = listOf(
                Color.parseColor("#A1DC7C"),
                Color.parseColor("#344BFD"),
                Color.parseColor("#F68D2B"),
                Color.parseColor("#D33232"),
                Color.parseColor("#AE6CFF")
            )
            setDrawValues(true)
            valueTextColor  = Color.WHITE
            valueTextSize   = 12f
            valueFormatter  = PercentFormatter(binding.pieChart)
        }

        binding.pieChart.apply {
            data                  = PieData(dataSet)
            setUsePercentValues(true)
            setExtraOffsets(0f, 0f, 0f, 0f)

            isDrawHoleEnabled      = true
            holeRadius             = 70f
            transparentCircleRadius= 73f
            setHoleColor(Color.parseColor("#1E1E1E"))

            setDrawCenterText(true)
            centerText            = formatInSelectedCurrency(dataMap.values.sum())
            setCenterTextSize(18f)
            setCenterTextColor(Color.WHITE)

            setEntryLabelColor(Color.TRANSPARENT)
            setEntryLabelTextSize(0f)

            legend.isEnabled      = false
            description.isEnabled = false

            setTouchEnabled(false)
            animateY(800)
            invalidate()
        }
    }

    private fun buildPieLegend(dataMap: Map<String, Double>) {
        binding.cellFood.visibility    = View.GONE
        binding.cellCar.visibility     = View.GONE
        binding.cellMedical.visibility = View.GONE
        binding.cellSaving.visibility  = View.GONE

        val nonZero = dataMap.entries.filter { it.value > 0.0 }.map { it.toPair() }
        val colors  = listOf(
            Color.parseColor("#A1DC7C"),
            Color.parseColor("#344BFD"),
            Color.parseColor("#F68D2B"),
            Color.parseColor("#D33232"),
            Color.parseColor("#AE6CFF")
        )

        nonZero.take(4).forEachIndexed { i, (catName, total) ->
            val formatted = formatInSelectedCurrency(total)
            when (i) {
                0 -> {
                    binding.cellFood.visibility = View.VISIBLE
                    binding.dotFood.setBackgroundColor(colors[i])
                    binding.tvLegendFood.text = "$catName   $formatted"
                }
                1 -> {
                    binding.cellCar.visibility = View.VISIBLE
                    binding.dotCar.setBackgroundColor(colors[i])
                    binding.tvLegendCar.text = "$catName   $formatted"
                }
                2 -> {
                    binding.cellMedical.visibility = View.VISIBLE
                    binding.dotMedical.setBackgroundColor(colors[i])
                    binding.tvLegendMedical.text = "$catName   $formatted"
                }
                3 -> {
                    binding.cellSaving.visibility = View.VISIBLE
                    binding.dotSaving.setBackgroundColor(colors[i])
                    binding.tvLegendSaving.text = "$catName   $formatted"
                }
            }
        }
    }

    private fun loadBarChartDataForRecentMonths() {
        val uid = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show()
            clearBarChart()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val monthYears = getFiveCenteredMonthYears()
                val labels     = getFiveCenteredMonthShortLabels()
                val budgets    = mutableListOf<Float>()
                val expenses   = mutableListOf<Float>()

                val catSnap = db.collection("users").document(uid)
                    .collection("categories").get().await()
                val txSnap  = db.collection("users").document(uid)
                    .collection("transactions").get().await()

                monthYears.forEach { pair ->
                    // 1. Extract month & year
                    val m = pair.first
                    val y = pair.second

                    // 2. Reset sums for THIS month
                    var bSum = 0.0
                    var eSum = 0.0

                    // 3. Sum up category budgets
                    for (doc in catSnap.documents) {
                        val occ = doc.getString("occurrence") ?: continue
                        val parts = occ.split("/")
                        if (parts.size == 3) {
                            val catM = parts[1].toIntOrNull()?.minus(1) ?: continue
                            val catY = parts[2].toIntOrNull() ?: continue
                            if (catM == m && catY == y) {
                                bSum += doc.getLong("amount")?.toDouble() ?: 0.0
                            }
                        }
                    }
                    budgets.add(bSum.toFloat())

                    // 4. Sum up transactions
                    for (doc in txSnap.documents) {
                        val ds = doc.getString("date") ?: continue
                        val parts = ds.split("/")
                        if (parts.size == 3) {
                            val txM = parts[1].toIntOrNull()?.minus(1) ?: continue
                            val txY = parts[2].toIntOrNull() ?: continue
                            if (txM == m && txY == y) {
                                eSum += doc.getDouble("amount") ?: 0.0
                            }
                        }
                    }
                    expenses.add(eSum.toFloat())
                }


                launch(Dispatchers.Main) {
                    if (budgets.all { it == 0f } && expenses.all { it == 0f }) {
                        clearBarChart()
                        binding.barChart.setNoDataText("No budget or expense data for recent months.")
                        binding.barChart.invalidate()
                        val zeroFmt = formatInSelectedCurrency(0.0)
                        binding.tvTotalBudgetValue.text = zeroFmt
                        binding.tvTotalExpenseValue.text = zeroFmt
                    } else {
                        val centerIdx = labels.size / 2
                        val monthYear = monthYears[centerIdx]
                        val goalMonthName = DateFormatSymbols().months[ monthYear.first ]
                        // assume one goal doc per month
                        db.collection("users").document(uid)
                      .collection("goals")
                        .whereEqualTo("month", goalMonthName)
                        .get()
                        .addOnSuccessListener { goalSnap ->
                        val minG = (goalSnap.documents.firstOrNull()?.getLong("minimumgoal") ?: 0L).toFloat()
                        val maxG = (goalSnap.documents.firstOrNull()?.getLong("maximumgoal") ?: 0L).toFloat()
                        drawBarChart(binding.barChart, labels, budgets, expenses, minG, maxG)
                        }
                        val center = 2
                        val cb = budgets[center].toDouble()
                        val ce = expenses[center].toDouble()
                        binding.tvTotalBudgetValue.text = formatInSelectedCurrency(cb)
                        binding.tvTotalExpenseValue.text = formatInSelectedCurrency(ce)
                    }
                }
            } catch (e: Exception) {
                Log.e("reports", "Error loading bar chart data", e)
                launch(Dispatchers.Main) {
                    Toast.makeText(this@reports, "Error loading bar chart data", Toast.LENGTH_SHORT).show()
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

    private fun getFiveCenteredMonthYears(): List<Pair<Int,Int>> {
        val base = cal.clone() as Calendar
        base.add(Calendar.MONTH, -2)
        return List(5) { i ->
            Pair(base.get(Calendar.MONTH), base.get(Calendar.YEAR)).also {
                base.add(Calendar.MONTH, 1)
            }
        }
    }

    private fun getFiveCenteredMonthShortLabels(): List<String> =
        getFiveCenteredMonthYears().map { DateFormatSymbols().shortMonths[it.first] }

    private fun drawBarChart(
        barChart: BarChart,
        monthLabels: List<String>,
        budgetValues: List<Float>,
        expenseValues: List<Float>,
        minGoal: Float,
        maxGoal: Float
    ) {
        if (monthLabels.isEmpty() || budgetValues.size != expenseValues.size) {
            clearBarChart()
            return
        }

        // 1) Build your bar entries
        val bEntries = budgetValues.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
        val eEntries = expenseValues.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }

        val bSet = BarDataSet(bEntries, "Total Budget").apply {
            color = Color.parseColor("#A1DC7C")
            setDrawValues(false)
        }
        val eSet = BarDataSet(eEntries, "Expense").apply {
            color = Color.parseColor("#D33232")
            setDrawValues(false)
        }

        // 2) Group the bars
        val data = BarData(bSet, eSet).apply { barWidth = 0.3f }
        val groupSpace = 0.2f
        val barSpace   = 0.05f
        val groupWidth = data.getGroupWidth(groupSpace, barSpace)
        data.groupBars(0f, groupSpace, barSpace)

        // 3) Configure X axis
        barChart.xAxis.apply {
            position            = XAxis.XAxisPosition.BOTTOM
            granularity         = 1f
            setDrawGridLines(false)
            textColor           = Color.WHITE
            setCenterAxisLabels(true)
            axisMinimum         = 0f
            axisMaximum         = groupWidth * monthLabels.size
            valueFormatter      = IndexAxisValueFormatter(monthLabels)
        }

        // 4) Add goal lines on the left Y axis
        barChart.axisLeft.apply {
            // clean up any previous lines
            removeAllLimitLines()

            // min‐goal line
            val minLine = LimitLine(minGoal, "Min Goal").apply {
                lineWidth      = 2f
                lineColor      = Color.parseColor("#344BFD")
                textColor      = Color.WHITE
                textSize       = 12f
                labelPosition  = LimitLine.LimitLabelPosition.RIGHT_TOP
            }
            // max‐goal line
            val maxLine = LimitLine(maxGoal, "Max Goal").apply {
                lineWidth      = 2f
                lineColor      = Color.parseColor("#A1DC7C")
                textColor      = Color.WHITE
                textSize       = 12f
                labelPosition  = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
            }

            addLimitLine(minLine)
            addLimitLine(maxLine)

            // draw lines behind the bars
            setDrawLimitLinesBehindData(true)
            val dataMax = (budgetValues + expenseValues).maxOrNull() ?: 0f
            val top     = maxOf(dataMax, maxGoal) * 1.1f   // add 10% padding
            axisMaximum = top

            // keep grid lines if you like
            setDrawGridLines(true)
            gridColor = Color.parseColor("#444444")
            textColor = Color.WHITE
            axisMinimum = 0f
        }

        barChart.apply {
            this.data                 = data
            description.isEnabled= false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setTouchEnabled(true)
            axisRight.isEnabled  = false
            legend.isEnabled     = false
            invalidate()
        }
    }






}
