package com.example.myguidance

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
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
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

class ScheduleInvitation : ViewModel() {
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

        scheduleListener?.remove()

        scheduleListener = db.collection("schedules")
            .addSnapshotListener { snapshot, error ->
                _isRefreshing.value = false

                if (error != null || snapshot == null) {
                    _schedules.value = emptyList()
                    return@addSnapshotListener
                }

                val validSchedules = snapshot.documents
                    .mapNotNull { doc -> doc.toObject(Schedules::class.java) }
                    .filter { schedule -> schedule.expiresAt > currentTime }
                    .sortedBy { it.expiresAt }

                _schedules.value = validSchedules
            }
    }

    fun updateScheduleStatus(scheduleId: String, reportId: String? = null, newStatus: String) {
        if (scheduleId.isNotBlank()) {
            db.collection("schedules").document(scheduleId)
                .update("status", newStatus)

            if (!reportId.isNullOrBlank()) {
                db.collection("reports").document(reportId)
                    .update("status", newStatus)
            }
        }
    }

    fun refreshSchedules() {
        _isRefreshing.value = true
        fetchRealSchedules()
    }

    @SuppressLint("EmptySuperCall")
    override fun onCleared() {
        super.onCleared()
        scheduleListener?.remove()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleInvitationDesignScreen(
    authViewModel: AuthViewModel,
    scheduleViewModel: ScheduleInvitation = viewModel(),
    onBackClick: () -> Unit = {}
) {

    val userIdNumber by authViewModel.userIdNumber.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val schedulesList by scheduleViewModel.schedules.collectAsState()
    val isViewModelRefreshing by scheduleViewModel.isRefreshing.collectAsState()

    val mine = schedulesList.filter { it.isFor(userIdNumber) }
    android.util.Log.d("INVITE_DEBUG", "myId='$userIdNumber' total=${schedulesList.size} mine=${mine.size} ids=${schedulesList.map { s -> s.selectablePeopleList.map { it["idNumber"] } }}")
    val pending = mine.filter { it.status.trim().equals("Pending", ignoreCase = true) }
    val history = mine.filter { !it.status.trim().equals("Pending", ignoreCase = true) }
    val shown = if (selectedTab == 0) pending else history

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Schedule Invitations", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            SecondaryTabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surface) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Pending (${pending.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("History (${history.size})") }
                )
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing || isViewModelRefreshing,
                onRefresh = {
                    scope.launch {
                        isRefreshing = true
                        scheduleViewModel.refreshSchedules()
                        delay(800.milliseconds)
                        delay(1000.milliseconds)
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                val listAlpha by animateFloatAsState(
                    targetValue = if (isRefreshing || isViewModelRefreshing) 0.4f else 1.0f,
                    animationSpec = tween(durationMillis = 300),
                    label = "ScheduleListFadeAnimation"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(alpha = listAlpha)
                ) {
                    if (shown.isEmpty()) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize()) {
                                    EmptyInvitesState(
                                        message = if (selectedTab == 0) "You're all caught up." else "No past invitations."
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(shown, key = { it.scheduleId }) { schedule ->
                                InviteDesignCard(
                                    schedule = schedule,
                                    onAccept = { scheduleViewModel.updateScheduleStatus(schedule.scheduleId, schedule.reportId, "Scheduled") },
                                    onDecline = { scheduleViewModel.updateScheduleStatus(schedule.scheduleId, schedule.reportId,"Pending") },

                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyInvitesState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
    }
}

@Composable
private fun InviteDesignCard(
    schedule: Schedules,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val isPending = schedule.status.equals("Pending", ignoreCase = true)

    val (chipBg, chipColor, chipText) = when (schedule.status.uppercase()) {
        "ACCEPTED" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Accepted")
        "DECLINED" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Declined")
        "EXPIRED" -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "Expired")
        else -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary, "New")
    }

    val (accentBg, accentColor) = when (schedule.category.lowercase()) {
        "appointment" -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        "event" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "workshop" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        else -> Color(0xFFF3E5F5) to Color(0xFF7B1FA2)
    }

    val (monthStr, dayStr) = remember(schedule.date) {
        try {
            val parsedDate = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).parse(schedule.date)
            if (parsedDate != null) {
                val month = SimpleDateFormat("MMM", Locale.getDefault()).format(parsedDate).uppercase()
                val day = SimpleDateFormat("dd", Locale.getDefault()).format(parsedDate)
                month to day
            } else {
                "DATE" to "--"
            }
        } catch (_: Exception) {
            "DATE" to "--"
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {

                // Date block
                Column(
                    modifier = Modifier
                        .width(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentBg)
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = monthStr,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Text(
                        text = dayStr,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = schedule.title.ifBlank { "Guidance Session" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(chipBg)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = chipText,
                                color = chipColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "From ${schedule.authorName.ifBlank { "Guidance Office" }} • ${schedule.category}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Time",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = schedule.time.ifBlank { "TBD" },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (schedule.purpose.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = schedule.purpose, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (isPending) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Decline")
                    }
                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Accept")
                    }
                }
            }
        }
    }
}

fun Schedules.isFor(idNumber: String): Boolean {
    return idNumber.isNotBlank() && (selectablePeopleList + selectablePeople).any { personMap ->
        val personId = personMap["idNumber"]?.trim() ?: ""
        personId.equals(idNumber.trim(), ignoreCase = true)
    }
}
