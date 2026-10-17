package com.example.myguidance


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class StatMetric(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

data class BarChartData(
    val label: String,
    val value: Float
)

@Composable
fun StatisticsScreen(
    reports: List<Report>,
    authViewModel: AuthViewModel,
    postViewModel: PostViewModel
) {

    val metrics = remember(reports) {
        fun count(status: String) = reports.count { it.status.equals(status, ignoreCase = true) }
        listOf(
            StatMetric("Total Reports", reports.size.toString(), Icons.AutoMirrored.Outlined.Article, Color(0xFF4CAF50)),
            StatMetric("To Be Scheduled", count("TBS").toString(), Icons.Outlined.Report, Color(0xFFF44336)),
            StatMetric("Scheduled", count("Scheduled").toString(), Icons.Outlined.CalendarMonth, Color(0xFF2196F3)),
            StatMetric("Resolved", count("Resolved").toString(), Icons.Outlined.CheckCircle, Color(0xFFFF9800))
        )
    }

    val weeklyData = remember(reports) {
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dayMillis = 24L * 60 * 60 * 1000

        val counts = (6 downTo 0).map { offset ->
            val start = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -offset)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val from = start.timeInMillis
            dayFormat.format(start.time) to reports.count { it.timestamp in from until from + dayMillis }
        }

        val max = (counts.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
        counts.map { (label, count) -> BarChartData(label, count / max.toFloat()) }
    }

    val incidentData = remember(reports) {
        reports
            .groupingBy { it.incident.ifBlank { "Other" } }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
    }

    val statusData = remember(reports) {
        listOf("TBS", "Pending", "Scheduled", "Resolved").map { status ->
            status to reports.count { it.status.equals(status, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Overview",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Metrics Grid (2 Columns)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(metric = metrics[0], modifier = Modifier.weight(1f))
                MetricCard(metric = metrics[1], modifier = Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(metric = metrics[2], modifier = Modifier.weight(1f))
                MetricCard(metric = metrics[3], modifier = Modifier.weight(1f))
            }
        }

        Text(
            text = "Weekly Reports",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyData.forEach { data ->
                        BarItem(data = data)
                    }
                }
            }
        }

        Text(
            text = "Breakdown",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        PieChartCard(title = "Reports by incident", data = incidentData)
        PieChartCard(title = "Reports by status", data = statusData, donut = true)
    }
}

@Composable
private fun MetricCard(metric: StatMetric, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(metric.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = metric.icon,
                    contentDescription = metric.title,
                    tint = metric.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = metric.value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = metric.title,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BarItem(data: BarChartData) {
    val fraction by animateFloatAsState(
        targetValue = data.value.coerceIn(0.1f, 1f),
        animationSpec = tween(600),
        label = "bar"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        // The bar area takes the space above the label, so tall bars never push the label out
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .fillMaxHeight(fraction = fraction)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = data.label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}


private val PieColors = listOf(
    Color(0xFF2196F3), Color(0xFFF44336), Color(0xFF4CAF50),
    Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF00BCD4)
)

@Composable
fun PieChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    donut: Boolean = false
) {
    val total = data.sumOf { it.second }

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(800),
        label = "pie"
    )

    val trackColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier = modifier.size(150.dp)) {
        val stroke = size.minDimension * 0.22f
        val inset = if (donut) stroke / 2 else 0f
        val arcSize = Size(size.width - inset * 2, size.height - inset * 2)

        if (total == 0) {
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = !donut,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = if (donut) Stroke(stroke) else Fill
            )
            return@Canvas
        }

        var start = -90f
        data.forEachIndexed { index, (_, value) ->
            val sweep = 360f * value / total * progress
            drawArc(
                color = PieColors[index % PieColors.size],
                startAngle = start,
                sweepAngle = sweep,
                useCenter = !donut,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = if (donut) Stroke(stroke) else Fill
            )
            start += sweep
        }
    }
}

@Composable
private fun PieLegend(data: List<Pair<String, Int>>) {
    val total = data.sumOf { it.second }.coerceAtLeast(1)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        data.forEachIndexed { index, (label, value) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(PieColors[index % PieColors.size])
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$label: $value\n (${value * 100 / total}%)",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PieChartCard(
    title: String,
    data: List<Pair<String, Int>>,
    donut: Boolean = false
) {
    val shown = data.filter { it.second > 0 }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            if (shown.isEmpty()) {
                Text("No data yet", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        PieChart(data = shown, donut = donut)
                        if (donut) {
                            Text(
                                text = shown.sumOf { it.second }.toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    PieLegend(data = shown)
                }
            }
        }
    }
}