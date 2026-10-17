package com.example.myguidance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAppointmentDesign(
    reportId: String = "",
    targetUserId: String = "",
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel = viewModel(),
    scheduleViewModel: ScheduleCreation,
    onBackClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {}
) {
    var titleText by remember { mutableStateOf("") }
    var selectedDateInMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    var startHour by remember { mutableIntStateOf(9) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(10) }
    var endMinute by remember { mutableIntStateOf(0) }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var purposeText by remember { mutableStateOf("") }

    val profile by profileViewModel.userProfile.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()

    var errorMessage by remember { mutableStateOf("") }
    val isUploading by scheduleViewModel.isUploading.collectAsState()

    // Format Date and Time using TimeUtils helpers
    val formattedDate = formatDate(selectedDateInMillis)
    val formattedStartTime = formatTime(startHour, startMinute)
    val formattedEndTime = formatTime(endHour, endMinute)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Schedule", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Field
            Text(
                text = "Title",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                placeholder = { Text("e.g., Academic Guidance, Counseling Session") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Date Selector Input
            Text(
                text = "Select Date",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = formattedDate,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                placeholder = { Text("Tap to choose date") },
                trailingIcon = {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Select Date"
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            )

            // Start & End Time Pickers in a Row
            Text(
                text = "Time Range",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = formattedStartTime,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Start Time") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = "Start Time"
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showStartTimePicker = true }
                )

                OutlinedTextField(
                    value = formattedEndTime,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("End Time") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = "End Time"
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEndTimePicker = true }
                )
            }

            // Description / Purpose Field
            Text(
                text = "Description",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = purposeText,
                onValueChange = { purposeText = it },
                placeholder = { Text("e.g., Discussing career guidance, Academic concerns...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button
            Button(
                onClick = {
                    errorMessage = ""
                    val startTotalMinutes = startHour * 60 + startMinute
                    val endTotalMinutes = endHour * 60 + endMinute

                    when {
                        titleText.isBlank() -> errorMessage = "Please enter a title."
                        selectedDateInMillis == null -> errorMessage = "Please select a date."
                        endTotalMinutes <= startTotalMinutes -> errorMessage =
                            "End time must be after start time."

                        endsInThePast(selectedDateInMillis!!, endHour, endMinute) -> errorMessage =
                            "The session must end in the future, or the invitation will not show."

                        else -> {
                            val timeRange = "$formattedStartTime - $formattedEndTime"
                            val fullPurpose =
                                if (titleText.isNotBlank()) "$titleText: $purposeText" else purposeText

                            scheduleViewModel.createSchedule(
                                status = "Pending",
                                reportId = reportId,
                                targetUserId = targetUserId,
                                title = titleText,
                                authorName = profile.name,
                                date = formattedDate,
                                time = timeRange,
                                purpose = fullPurpose,
                                selectedDateInMillis = selectedDateInMillis!!,
                                startHour = startHour,
                                startMinute = startMinute,
                                endHour = endHour,
                                endMinute = endMinute,
                                onError = { errorMessage = it },
                                onSuccess = {
                                    scheduleViewModel.markReportAsScheduled(reportId)
                                    onConfirmClick()
                                },

                                )
                        }
                    }
                },
                enabled = !isUploading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Send Schedule", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    AppDateTimePickerContainer(
        showDatePicker = showDatePicker,
        onDismissDatePicker = { showDatePicker = false },
        onDateSelected = { selectedDateInMillis = it },
        showStartTimePicker = showStartTimePicker,
        onDismissStartTimePicker = { showStartTimePicker = false },
        startHour = startHour,
        startMinute = startMinute,
        onStartTimeSelected = { h, m -> startHour = h; startMinute = m },
        showEndTimePicker = showEndTimePicker,
        onDismissEndTimePicker = { showEndTimePicker = false },
        endHour = endHour,
        endMinute = endMinute,
        onEndTimeSelected = { h, m -> endHour = h; endMinute = m }
    )
}

/** True when the chosen date and end time are already behind us. */
private fun endsInThePast(dateMillis: Long, endHour: Int, endMinute: Int): Boolean {
    val picked = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        .apply { timeInMillis = dateMillis }
    val end = java.util.Calendar.getInstance().apply {
        set(
            picked.get(java.util.Calendar.YEAR),
            picked.get(java.util.Calendar.MONTH),
            picked.get(java.util.Calendar.DAY_OF_MONTH),
            endHour,
            endMinute,
            0
        )
        set(java.util.Calendar.MILLISECOND, 0)
    }
    return end.timeInMillis <= System.currentTimeMillis()
}