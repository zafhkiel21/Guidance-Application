package com.example.myguidance

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

data class Schedules(
    val reportId: String = "",
    val status: String = "",
    val category: String = "",
    val targetUserId: String = "",
    val scheduleId: String = "",
    val title: String = "",
    val userId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val name: String = "",
    val date: String = "",
    val time: String = "",
    val purpose: String = "",
    val selectablePeopleList: List<Map<String, String>> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val startTimeMillis: Long = 0L,
    val endTimeInMillis: Long = 0L,
    val expiresAt: Long = 0L       // Expire timestamp used by fetch queries
)

class ScheduleCreation : ViewModel() {
    private val db = FirebaseFirestore.getInstance("guidance")
    private val auth = FirebaseAuth.getInstance()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _schedulesList = MutableStateFlow<List<Schedules>>(emptyList())
    val schedulesList: StateFlow<List<Schedules>> = _schedulesList.asStateFlow()

    init {
        fetchSchedules()
    }

    fun createSchedule(
        status: String,
        reportId: String,
        targetUserId: String,
        title: String,
        authorName: String,
        date: String,
        time: String,
        purpose: String,
        selectablePeopleList: List<Map<String, String>> = emptyList(),
        selectedDateInMillis: Long,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError("User not authenticated.")
            return
        }

        val resolvedName = authorName.ifBlank {
            currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Anonymous"
        }

        _isUploading.value = true

        // Calculate start and end epoch timestamps in milliseconds
        val startTimeMillis = calculateTimestamp(selectedDateInMillis, startHour, startMinute)
        val endTimeInMillis = calculateTimestamp(selectedDateInMillis, endHour, endMinute)

        saveScheduleToFirestore(
            status = status,
            reportId = reportId,
            targetUserId = targetUserId,
            title = title,
            userId = currentUser.uid,
            authorName = resolvedName,
            date = date,
            time = time,
            purpose = purpose,
            selectedPeople = selectablePeopleList,
            startTimeMillis = startTimeMillis,
            endTimeInMillis = endTimeInMillis,
            expiresAt = endTimeInMillis, // Expires after end time passes
            onSuccess = {
                _isUploading.value = false
                onSuccess()
            },
            onError = { err ->
                _isUploading.value = false
                onError(err)
            }
        )
    }

    fun fetchSchedules() {
        val currentTime = System.currentTimeMillis()
        db.collection("schedules")
            .whereGreaterThan("expiresAt", currentTime)
            .orderBy("expiresAt", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val schedules = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Schedules::class.java)
                }
                _schedulesList.value = schedules
            }
    }

    private fun saveScheduleToFirestore(
        status: String,
        category: String = "Appointment",
        reportId: String,
        targetUserId: String,
        title: String,
        userId: String,
        authorName: String,
        date: String,
        time: String,
        purpose: String,
        selectedPeople: List<Map<String, String>> = emptyList(),
        startTimeMillis: Long,
        endTimeInMillis: Long,
        expiresAt: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val newScheduleRef = db.collection("schedules").document()
        val currentTime = System.currentTimeMillis()

        val scheduleMap = mapOf(
            "status" to status,
            "category" to category,
            "reportId" to reportId,
            "scheduleId" to newScheduleRef.id,
            "targetUserId" to targetUserId,
            "userId" to userId,
            "authorId" to userId,
            "title" to title,
            "authorName" to authorName,
            "date" to date,
            "time" to time,
            "purpose" to purpose,
            "selectablePeople" to selectedPeople,
            "timestamp" to currentTime,
            "startTimeMillis" to startTimeMillis,
            "endTimeInMillis" to endTimeInMillis,
            "expiresAt" to expiresAt
        )

        newScheduleRef.set(scheduleMap)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.localizedMessage ?: "Failed to save schedule.") }
    }

    private fun calculateTimestamp(dateInMillis: Long, hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateInMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun markReportAsScheduled(
        reportId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (reportId.isBlank()) return

        // Reference the specific document in your "reports" (or "posts") collection
        db.collection("reports").document(reportId)
            .update("status", "Pending") // Updates only the "status" field
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Failed to update report status.")
            }
    }

    fun markReportAsResolved(
        reportId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (reportId.isBlank()) return

        // Reference the specific document in your "reports" (or "posts") collection
        db.collection("reports").document(reportId)
            .update("status", "Resolved") // Updates only the "status" field
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Failed to update report status.")
            }
    }

}


