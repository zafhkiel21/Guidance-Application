package com.example.myguidance

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

// ViewModel for Realtime Firestore Schedule Data
class ScheduleViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance("guidance")
    private val _schedules = MutableStateFlow<List<Schedules>>(emptyList())
    val schedules: StateFlow<List<Schedules>> = _schedules.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var scheduleListener: ListenerRegistration? = null

    init {
        fetchRealSchedules()
    }

    private fun fetchRealSchedules() {
        val currentTime = System.currentTimeMillis()

        // 1. Remove previous listener if active
        scheduleListener?.remove()

        // 2. Fetch schedules and filter out expired items client-side
        scheduleListener = db.collection("schedules")
            .addSnapshotListener { snapshot, error ->
                _isRefreshing.value = false

                if (error != null || snapshot == null) {
                    _schedules.value = emptyList()
                    return@addSnapshotListener
                }

                val validSchedules = snapshot.documents
                    .mapNotNull { doc -> doc.toObject(Schedules::class.java) }
                    .filter { it.status.equals("Scheduled", ignoreCase = true) }
                    .filter { schedule -> schedule.expiresAt > currentTime }
                    .sortedBy { it.expiresAt } // Sort chronologically

                _schedules.value = validSchedules
            }
    }

    fun refreshSchedules() {
        _isRefreshing.value = true
        fetchRealSchedules()
    }

    @SuppressLint("EmptySuperCall")
    override fun onCleared(){
        super.onCleared()
        scheduleListener?.remove()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    authViewModel: AuthViewModel = viewModel(),
    scheduleViewModel: ScheduleViewModel = viewModel(),
    onScheduleClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onScheduleInvitationClick: () -> Unit = {},
    onSettingClick: () -> Unit = {},
    onReportClick: () -> Unit = {}
) {



    var isFabExpanded by remember { mutableStateOf(false) }
    val isViewModelRefreshing by scheduleViewModel.isRefreshing.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val userIdNumber by authViewModel.userIdNumber.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()
    val scheduleList by scheduleViewModel.schedules.collectAsState()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedFilterTab by remember { mutableIntStateOf(0) }

    val categories = listOf("All", "Appointment", "Seminar", "Other")




    val filteredSchedules = remember(scheduleList, selectedDate, selectedFilterTab, userIdNumber, userRole) {
        scheduleList.filter { item ->
            // 1. If it's still loading the user ID, don't show student-specific items yet (or allow if admin)
            val isAllowed = when {
                userRole.isCounselorOrAdmin -> true
                userIdNumber.isBlank() -> false // Prevents flashing/hiding everything before ID loads
                else -> item.isFor(userIdNumber)
            }

            val itemDate = parseDateToLocalDate(item.date)
            val dateMatches = itemDate == selectedDate
            val categoryMatches = when (selectedFilterTab) {
                0 -> true
                else -> {
                    val selectedCategoryName = categories[selectedFilterTab]
                    item.category.equals(selectedCategoryName, ignoreCase = true)
                }
            }
            isAllowed && dateMatches && categoryMatches
        }
    }


    if (userRole != UserRole.UNKNOWN && userRole.canCreatePost) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Schedule",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },

        floatingActionButton = {
            if (userRole.canCreatePost) {
                ExpandableFloatingActionButton(
                    items = listOf(
                        FabSubItem(
                            label = "Schedule",
                            icon = Icons.Outlined.CalendarMonth,
                            onClick = onScheduleInvitationClick
                        )
                    ),
                    isExpanded = isFabExpanded,
                    onExpandedChange = { isFabExpanded = it },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        },

        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {

                NavigationBarItem(
                    selected = true,
                    onClick = onScheduleClick,
                    icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Schedule") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onHomeClick,
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") }
                )
                if (userRole.isCounselorOrAdmin) {
                    NavigationBarItem(
                        selected = false,
                        onClick = onReportClick,
                        icon = { Icon(Icons.Outlined.Report, contentDescription = "Reports") }
                    )
                }
                NavigationBarItem(
                    selected = false,
                    onClick = onSettingClick,
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") }
                )
            }
        }
    ) { innerPadding ->
        val scrimAlpha by animateFloatAsState(
            targetValue = if (isFabExpanded) 0.6f else 0f,
            animationSpec = tween(durationMillis = 300),
            label = "ScrimAnimation"
        )
        PullToRefreshBox(
            isRefreshing = isRefreshing || isViewModelRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    scheduleViewModel.refreshSchedules()
                    delay(800.milliseconds)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val listAlpha by animateFloatAsState(
                targetValue = if (isRefreshing || isViewModelRefreshing) 0.4f else 1.0f,
                animationSpec = tween(durationMillis = 300),
                label = "ScheduleListFadeAnimation"
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .graphicsLayer { alpha = listAlpha }
            ) {
                // Horizontal Date Strip Calendar
                DateStripHeader(
                    selectedDate = selectedDate,
                    schedules = scheduleList,
                    onDateSelected = { selectedDate = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEachIndexed { index, label ->
                        FilterChip(
                            selected = selectedFilterTab == index,
                            onClick = { selectedFilterTab = index },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Schedule Items List
                if (filteredSchedules.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()), // Enables pull gesture when list is empty
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = "No Seminar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No schedules or Seminar for this date.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredSchedules, key = { it.scheduleId }) { item ->
                            ScheduleCard(item = item)
                        }
                    }
                }
            }

        }
        if (scrimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = scrimAlpha }
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { isFabExpanded = false }
            )
        }
    }
}
}

@Composable
fun DateStripHeader(
    selectedDate: LocalDate,
    schedules: List<Schedules>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    var isMonthMenuExpanded by remember { mutableStateOf(false) }

    val scheduledDateSet = remember(schedules) {
        schedules.mapNotNull { parseDateToLocalDate(it.date) }.toSet()
    }

    val yearMonth = remember(selectedDate) { YearMonth.from(selectedDate) }
    val daysInMonth = remember(yearMonth) {
        (1..yearMonth.lengthOfMonth()).map { yearMonth.atDay(it) }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(selectedDate) {
        val selectedIndex = daysInMonth.indexOf(selectedDate)
        if (selectedIndex >= 0) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    val monthYearText = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp)
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isMonthMenuExpanded = true }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            ) {
                Text(
                    text = monthYearText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Month",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = isMonthMenuExpanded,
                onDismissRequest = { isMonthMenuExpanded = false }
            ) {
                Month.entries.forEach { month ->
                    val isTodayMonth = month == today.month
                    val isSelectedMonth = month == selectedDate.month

                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = month.name.lowercase().replaceFirstChar { it.titlecase() },
                                    fontWeight = when {
                                        isSelectedMonth -> FontWeight.ExtraBold
                                        isTodayMonth -> FontWeight.Bold
                                        else -> FontWeight.Normal
                                    },
                                    color = when {
                                        isSelectedMonth -> MaterialTheme.colorScheme.onSurface
                                        isTodayMonth -> MaterialTheme.colorScheme.primary
                                        else -> Color.Unspecified
                                    }
                                )

                                if (isTodayMonth) {
                                    Text(
                                        text = "Today",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            val newYearMonth = YearMonth.of(selectedDate.year, month)
                            val targetDay = selectedDate.dayOfMonth.coerceAtMost(newYearMonth.lengthOfMonth())
                            onDateSelected(newYearMonth.atDay(targetDay))
                            isMonthMenuExpanded = false
                        },
                        modifier = Modifier.background(
                            if (isTodayMonth) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = daysInMonth,
                key = { it.toString() }
            ) { date ->
                val isSelected = date == selectedDate
                val isToday = date == today
                val hasSchedule = scheduledDateSet.contains(date)
                val isDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f


                val dayName = date.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)).uppercase()
                val dayNum = date.dayOfMonth.toString()

                Column(
                    modifier = Modifier
                        .width(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                hasSchedule -> if (isDarkTheme) Color(0xFF1F3D27) else Color(0xFFE8F5E9)
                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .clickable { onDateSelected(date) }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayName,
                        fontSize = 12.sp,
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            hasSchedule -> if (isDarkTheme) Color(0xFF81C784) else Color(0xFF2E7D32)
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dayNum,
                        fontSize = 16.sp,
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            hasSchedule -> if (isDarkTheme) Color(0xFFC8E6C9) else Color(0xFF1B5E20)
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = FontWeight.Bold
                    )
                    if (hasSchedule) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else if (isDarkTheme) Color(0xFF81C784) else Color(0xFF2E7D32))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleCard(item: Schedules) {
    val displayCategory = item.category.ifBlank { "Appointment" }

    val (categoryBg, categoryColor) = when (displayCategory.lowercase()) {
        "appointment" -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        "seminar"       -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "workshop"    -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        else          -> Color(0xFFF3E5F5) to Color(0xFF7B1FA2)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(categoryBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = displayCategory,
                        color = categoryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )


                }

                Text(
                    text = formatTimestamp(item.timestamp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
Column(verticalArrangement = Arrangement.SpaceBetween){



    Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
        imageVector = Icons.Default.Schedule,
        contentDescription = "Time",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.width(16.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
        text = item.time,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )}

}}



            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.title.ifBlank { "Guidance Session" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (item.purpose.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.purpose,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


        }
    }
}

private fun parseDateToLocalDate(dateStr: String): LocalDate? {
    return try {
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val date: Date = sdf.parse(dateStr) ?: return null
        date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    } catch (_: Exception) {
        null
    }
}