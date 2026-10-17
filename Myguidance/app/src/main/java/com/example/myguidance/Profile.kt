package com.example.myguidance

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    postViewModel: PostViewModel,
    onRecordClick: (reportId: String) -> Unit = {},
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel = viewModel(),
) {
    val userRole by authViewModel.userRole.collectAsState()
    val profile by profileViewModel.userProfile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val reports by postViewModel.reportsList.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val savedIdNumber by authViewModel.userIdNumber.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
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
        },
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Profile Avatar Placeholder
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "User Avatar",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(70.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // User Name
                Text(
                    text = profile.name.ifBlank { "User Name" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Dynamic Role Text
                Text(
                    text = userRole.name.ifBlank { "Unknown" },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Details Card using Firestore profile data
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ProfileDetailRow(label = "Name:", value = profile.name)
                        if(userRole == UserRole.STUDENT){
                            ProfileDetailRow(label = "Student ID:", value = profile.studentId)
                            ProfileDetailRow(label = "Course & Section:", value = "${profile.course} ${profile.section}")
                            ProfileDetailRow(label = "Year level:", value = profile.year)}
                        ProfileDetailRow(label = "Email:", value = profile.email)
                        ProfileDetailRow(label = "Phone:", value = profile.phone)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ReportCardList(
                    reports = reports,
                    idNumber = savedIdNumber.ifBlank { profile.studentId },
                    userId = currentUser?.uid ?: "",
                    onRecordClick = onRecordClick
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value.ifBlank { "N/A" },
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class UserProfile(
    @field:PropertyName("role") @get:PropertyName("role") val role: String = "",
    @field:PropertyName("name") @get:PropertyName("name") val name: String = "",
    @field:PropertyName("idNumber") @get:PropertyName("idNumber") val studentId: String = "",
    @field:PropertyName("course") @get:PropertyName("course") val course: String = "",
    @field:PropertyName("section") @get:PropertyName("section") val section: String = "",
    @field:PropertyName("year") @get:PropertyName("year") val year: String = "",
    @field:PropertyName("email") @get:PropertyName("email") val email: String = "",
    @field:PropertyName("phoneNumber") @get:PropertyName("phoneNumber") val phone: String = ""
)

class ProfileViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance("guidance")
    private val auth = FirebaseAuth.getInstance()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        val userId = auth.currentUser?.uid
        android.util.Log.d("PROFILE_DEBUG", "Logged in Auth UID: $userId")

        if (userId == null) {
            _isLoading.value = false
            return
        }

        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    android.util.Log.e("PROFILE_DEBUG", "Document does not exist for UID: $userId")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                android.util.Log.d("PROFILE_DEBUG", "Document Data: ${snapshot.data}")

                val profile = snapshot.toObject(UserProfile::class.java)
                if (profile != null) {
                    _userProfile.value = profile
                }

                _isLoading.value = false
            }
    }

}

@Composable
fun ReportCardList(
  idNumber: String,
  userId: String,
  reports: List<Report>,
  onRecordClick: (reportId: String) -> Unit,
){
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }


    val involvedIn = remember(reports, idNumber) {
        if (idNumber.isEmpty()) emptyList()
        else reports.filter { report ->
            report.peopleInvolved.any { it["idList"]?.trim() == idNumber }
        }
    }

    val submitted = remember(reports, idNumber, userId) {
        reports.filter { r ->
            (idNumber.isNotBlank() && r.userIdNumber.trim() == idNumber.trim()) ||
                    (userId.isNotBlank() && r.authorId == userId)
        }
    }

    val involvedList = remember(involvedIn) { involvedIn.toTitles() }
    val submittedList = remember(submitted) { submitted.toTitles() }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 300))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Report History",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${submitted.size} submitted • ${involvedIn.size} involved in",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                RecordSection("Involved in", involvedList, onRecordClick)
                Spacer(modifier = Modifier.height(12.dp))
                RecordSection("Submitted", submittedList, onRecordClick)
            }
        }
    }
}



