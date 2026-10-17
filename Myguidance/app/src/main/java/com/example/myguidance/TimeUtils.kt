package com.example.myguidance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog as MaterialDatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Formats hour and minute into "hh:mm a" string (e.g., "09:00 AM")
 */
fun formatTime(hour: Int, minute: Int): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
}

/**
 * Formats epoch millis into readable date string (e.g., "October 08, 2026")
 */
fun formatDate(millis: Long?): String {
    return millis?.let {
        SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(it))
    } ?: ""
}

/**
 * Reusable Global Date Picker Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    state: DatePickerState,
    onDismiss: () -> Unit,
    onConfirm: (Long?) -> Unit
) {
    MaterialDatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(state.selectedDateMillis)
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = state)
    }
}

/**
 * Reusable Global Time Picker Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimePickerDialog(
    state: TimePickerState,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = state)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        onConfirm(state.hour, state.minute)
                        onDismiss()
                    }) { Text("OK") }
                }
            }
        }
    }
}

/**
 * Global DateTime Container for managing all dialog triggers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDateTimePickerContainer(
    showDatePicker: Boolean,
    onDismissDatePicker: () -> Unit,
    onDateSelected: (Long?) -> Unit,
    showStartTimePicker: Boolean,
    onDismissStartTimePicker: () -> Unit,
    startHour: Int,
    startMinute: Int,
    onStartTimeSelected: (hour: Int, minute: Int) -> Unit,
    showEndTimePicker: Boolean,
    onDismissEndTimePicker: () -> Unit,
    endHour: Int,
    endMinute: Int,
    onEndTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    // --- Date Picker Dialog ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        AppDatePickerDialog(
            state = datePickerState,
            onDismiss = onDismissDatePicker,
            onConfirm = onDateSelected
        )
    }

    // --- Start Time Picker Dialog ---
    if (showStartTimePicker) {
        val startTimePickerState = rememberTimePickerState(initialHour = startHour, initialMinute = startMinute)
        AppTimePickerDialog(
            state = startTimePickerState,
            onDismiss = onDismissStartTimePicker,
            onConfirm = onStartTimeSelected
        )
    }

    // --- End Time Picker Dialog ---
    if (showEndTimePicker) {
        val endTimePickerState = rememberTimePickerState(initialHour = endHour, initialMinute = endMinute)
        AppTimePickerDialog(
            state = endTimePickerState,
            onDismiss = onDismissEndTimePicker,
            onConfirm = onEndTimeSelected
        )
    }
}