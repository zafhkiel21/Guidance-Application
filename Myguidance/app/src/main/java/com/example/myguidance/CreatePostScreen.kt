package com.example.myguidance

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

data class PersonInvolved(
    val name: String = "",
    val year : String = "",
    val course: String = "",
    val section: String = "",
    val role: String = "",
    val idList: String = ""

)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel = viewModel(),
    postViewModel: PostViewModel,
    onBackClick: () -> Unit = {},
    onPostSuccess: () -> Unit = {}
) {
    var isPeopleSectionExpanded by remember { mutableStateOf(true) }
    var isIncidentDetailsExpanded by remember { mutableStateOf(true) }
    val peopleList = remember { mutableStateListOf<PersonInvolved>() }
    val profile by profileViewModel.userProfile.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()
    val userIdNumber by authViewModel.userIdNumber.collectAsState()
    val isStudent = userRole.isStudent
    var isReport by remember(userRole) { mutableStateOf(isStudent) }
    var textIdNumber by remember { mutableStateOf("") }
    var wantsCounselorTalk by remember { mutableStateOf<Boolean?>(null) }
    var textRole by remember { mutableStateOf("") }
    var textName by remember { mutableStateOf("") }
    var textLocation by remember { mutableStateOf("") }
    var selectedIncident by remember { mutableStateOf("") }

    var selectedCategory by remember { mutableStateOf("Announcement") }
    var isIncidentDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var textContent by remember { mutableStateOf("") }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    val isUploading by postViewModel.isUploading.collectAsState()
    val context = LocalContext.current

    // Media Picker
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedMediaUri = uri
    }

    val courseOptions = Course.labels
    val yearOptions = YearLevel.labels

    var selectedCourse by remember { mutableStateOf("") }
    var isCourseDropdownExpanded by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf("") }
    var isYearDropdownExpanded by remember { mutableStateOf(false) }
    var selectedSection by remember { mutableStateOf("") }
    var isSectionDropdownExpanded by remember { mutableStateOf(false) }


    val incidentOption = listOf(
        "Physical fighting",
        "Improper Uniform",
        "Other"
    )

    val categoryOption = listOf(
        "Announcement",
        "Safety Announcement",
        "Seminar"
    )

    val sectionOptions = listOf(
        "A",
        "B",
        "C",
        "D"
    )


    var titleText by remember { mutableStateOf("") }
    var selectedDateInMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    var startHour by remember { mutableIntStateOf(9) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(10) }
    var endMinute by remember { mutableIntStateOf(0) }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val formattedDate = formatDate(selectedDateInMillis)
    val formattedStartTime = formatTime(startHour, startMinute)
    val formattedEndTime = formatTime(endHour, endMinute)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isReport) "Submit Incident Report" else "Create Post",
                        fontSize = 18.sp,
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
                actions = {
                    Button(
                        onClick = {
                            errorMessage = ""

                            val requiresDateAndTime = selectedCategory.equals("Seminar", ignoreCase = true)

                            when {
                                titleText.isBlank() -> errorMessage = "Please enter a title."

                                requiresDateAndTime && selectedDateInMillis == null -> {
                                    errorMessage = "Please select a date."
                                }



                                else -> {
                                    val timeRange = "$formattedStartTime - $formattedEndTime"

                                    if (isReport) {
                                        val postCategory = "Incident Report"
                                        postViewModel.createReport(
                                            time = formattedStartTime,
                                            location = textLocation,
                                            userIdNumber = userIdNumber,
                                            selectedDateInMillis = selectedDateInMillis ?: System.currentTimeMillis(),
                                            estimatedHour = startHour,
                                            estimatedMinute = startMinute,
                                            title = titleText,
                                            date = formattedDate,
                                            wantsCounselorTalk = wantsCounselorTalk,
                                            peopleList = peopleList,
                                            authorName = profile.name,
                                            category = postCategory,
                                            incident = selectedIncident,
                                            context = context,
                                            textContent = textContent,
                                            selectedMediaUri = selectedMediaUri,
                                            onSuccess = onPostSuccess,
                                            onError = { errorMessage = it }
                                        )
                                    } else {

                                        postViewModel.createPost(
                                            location = textLocation,
                                            selectedDateInMillis = selectedDateInMillis ?: System.currentTimeMillis(),
                                            startHour = startHour,
                                            startMinute = startMinute,
                                            endHour = endHour,
                                            endMinute = endMinute,
                                            title = titleText,
                                            date = formattedDate,
                                            time = timeRange,
                                            authorName = profile.name,
                                            category = selectedCategory,
                                            context = context,
                                            textContent = textContent,
                                            selectedMediaUri = selectedMediaUri,
                                            onSuccess = onPostSuccess,
                                            onError = { errorMessage = it }
                                        )
                                    }
                                }
                            }
                        },
                        enabled = !isUploading && (textContent.isNotBlank() || selectedMediaUri != null),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (isReport) "Submit" else "Post")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = profile.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(enabled = !isStudent) {
                                isReport = !isReport
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        FilterChip(
                            selected = isReport,
                            onClick = {
                                if (!isStudent) {
                                    isReport = !isReport
                                }
                            },
                            enabled = !isStudent,
                            label = {
                                Text(
                                    text = if (isReport) "Incident Report" else "General Post",
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                if (isReport) {
                                    Icon(
                                        imageVector = Icons.Outlined.ReportProblem,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.error,
                                selectedLabelColor = MaterialTheme.colorScheme.onError,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onError,
                                disabledSelectedContainerColor = MaterialTheme.colorScheme.error,
                                disabledLabelColor = MaterialTheme.colorScheme.onError,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onError
                            )
                        )

                        if (!isStudent) {
                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                imageVector = Icons.Outlined.SyncAlt,
                                contentDescription = "Switch type",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = if (isReport) "Switch to Post" else "Switch to Report",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Post Category Dropdown (For General Posts)
            if (!isReport) {
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categoryOption.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedCategory = option
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Incident Dropdown (For Reports)
            if (isReport) {
                ExposedDropdownMenuBox(
                    expanded = isIncidentDropdownExpanded,
                    onExpandedChange = { isIncidentDropdownExpanded = !isIncidentDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedIncident,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Incident Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isIncidentDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = isIncidentDropdownExpanded,
                        onDismissRequest = { isIncidentDropdownExpanded = false }
                    ) {
                        incidentOption.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    selectedIncident = selectionOption
                                    isIncidentDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
            // Title Field
            OutlinedTextField(
                value = titleText,
                onValueChange = {titleText = it},
                placeholder = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))
            if (selectedCategory == "Seminar" || isReport) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isIncidentDetailsExpanded = !isIncidentDetailsExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Incident Details",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Icon(
                        imageVector = if (isIncidentDetailsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Section",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }


                if (isIncidentDetailsExpanded) {

                    // Date Selector Input
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
                            .clickable { showDatePicker = true },
                        shape = RoundedCornerShape(12.dp),
                    )

                    Spacer(modifier = Modifier.height(12.dp))


                    if(selectedCategory == "Seminar") {
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
                                    .clickable { showStartTimePicker = true },
                                shape = RoundedCornerShape(12.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    if(isReport) {
                        OutlinedTextField(
                            value = formattedStartTime,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Estimated Time") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = "Estimated Time"
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showStartTimePicker = true },
                            shape = RoundedCornerShape(12.dp),
                        )
                    }




                    if(isReport || selectedCategory == "Seminar") {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = textLocation,
                            onValueChange = { textLocation = it },
                            placeholder = { Text("Location") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                if(isReport) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isPeopleSectionExpanded = !isPeopleSectionExpanded }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "People Involved",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Icon(
                            imageVector = if (isPeopleSectionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle Section",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }


                    if (isPeopleSectionExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = textName,
                            onValueChange = { textName = it },
                            placeholder = { Text("Person name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = textRole,
                            onValueChange = { textRole = it },
                            placeholder = { Text("Person role (e.g. Student, Witness)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = textIdNumber,
                            onValueChange = { textIdNumber = it },
                            placeholder = { Text("Person Id Number") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        ExposedDropdownMenuBox(
                            expanded = isCourseDropdownExpanded,
                            onExpandedChange = {
                                isCourseDropdownExpanded = !isCourseDropdownExpanded
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedCourse,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Course") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCourseDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()

                            )

                            ExposedDropdownMenu(
                                expanded = isCourseDropdownExpanded,
                                onDismissRequest = { isCourseDropdownExpanded = false }
                            ) {
                                courseOptions.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            selectedCourse = selectionOption
                                            isCourseDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        ExposedDropdownMenuBox(
                            expanded = isSectionDropdownExpanded,
                            onExpandedChange = { isSectionDropdownExpanded = !isSectionDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedSection,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Section") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSectionDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = isSectionDropdownExpanded,
                                onDismissRequest = { isSectionDropdownExpanded = false }
                            ) {
                                sectionOptions.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            selectedSection = selectionOption
                                            isSectionDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        ExposedDropdownMenuBox(
                            expanded = isYearDropdownExpanded,
                            onExpandedChange = { isYearDropdownExpanded = !isYearDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedYear,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Year Level") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isYearDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = isYearDropdownExpanded,
                                onDismissRequest = { isYearDropdownExpanded = false }
                            ) {
                                yearOptions.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            selectedYear = selectionOption
                                            isYearDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (textName.isNotBlank()) {
                                    peopleList.add(
                                        PersonInvolved(
                                            textName.trim(),
                                            selectedYear.trim(),
                                            selectedCourse.trim(),
                                            selectedSection.trim(),
                                            textRole.trim(),
                                            textIdNumber.trim()
                                        )
                                    )
                                    textIdNumber = ""
                                    textName = ""
                                    selectedCourse = ""
                                    selectedYear = ""
                                    selectedSection = ""
                                    textRole = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Person")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add")
                        }


                        if (peopleList.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                peopleList.forEach { person ->
                                    InputChip(
                                        selected = true,
                                        onClick = { peopleList.remove(person) },
                                        label = { Text("${person.name} • ${person.course} • ${person.year} • ${person.section} • ${person.role} (${person.idList})") },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove person",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))


            OutlinedTextField(
                value = textContent,
                onValueChange = { textContent = it },
                placeholder = {
                    Text(
                        text = if (isReport) "Describe the issue or incident..." else "What's on your mind?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 200.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors
                    (unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )

            if (isReport) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Would You Like To Talk To A Counselor About This?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { wantsCounselorTalk = true }
                            .padding(4.dp)
                    ) {
                        RadioButton(
                            selected = (wantsCounselorTalk == true),
                            onClick = { wantsCounselorTalk = true }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Yes", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { wantsCounselorTalk = false }
                            .padding(4.dp)
                    ) {
                        RadioButton(
                            selected = (wantsCounselorTalk == false),
                            onClick = { wantsCounselorTalk = false }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "No", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            selectedMediaUri?.let { uri ->
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected media",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { selectedMediaUri = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Media",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isReport) "Add to your incident report" else "Add to your post",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(onClick = { mediaPickerLauncher.launch("image/*") }) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = "Add Photo",
                            tint = Color(0xFF45BD62)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
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