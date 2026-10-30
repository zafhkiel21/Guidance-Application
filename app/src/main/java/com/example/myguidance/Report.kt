package com.example.myguidance


import android.annotation.SuppressLint
import android.text.format.DateUtils
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ripple
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds


data class ReportsTitle(
    val reportId: String = "",
    val title: String = "",
    val date: String = "",
    val status: String = "",
    val incident: String = ""
)

enum class ReportsVIEW {
    REPORTS,
    OVERVIEW,
    STATISTICS
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    authViewModel: AuthViewModel,
    postViewModel: PostViewModel,
    onScheduleAppointmentClick: (posterId: String, reportId: String) -> Unit = { _, _ -> },
    onScheduleClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onReportClick: () -> Unit = {},
    onSettingClick: () -> Unit = {},
    initialReportId: String? = null
) {

    var searchText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    var selectedCategory by remember { mutableIntStateOf(0) }
    val categories = listOf("TBS", "Pending", "Scheduled", "Resolved")
    var currentView by remember { mutableStateOf(ReportsVIEW.REPORTS) }


    val userRole by authViewModel.userRole.collectAsState()
    val viewModel: ScheduleCreation = viewModel()
    val reportsList by postViewModel.reportsList.collectAsState()

    val isViewModelRefreshing by postViewModel.isRefreshing.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    // Filter incident reports by category and active search query
    val filteredReports = remember(reportsList, searchText, selectedCategory) {
        reportsList.filter {
            val textMatches = searchText.isBlank() ||
                    it.time.contains(searchText.trim(), ignoreCase = true) ||
                    it.title.contains(searchText.trim(), ignoreCase = true) ||
                    it.name.contains(searchText.trim(), ignoreCase = true) ||
                    it.incident.contains(searchText.trim(), ignoreCase = true) ||
                    it.yearLevel.contains(searchText.trim(), ignoreCase = true)||
                    it.course.contains(searchText.trim(), ignoreCase = true)||
                    it.status.contains(searchText.trim(), ignoreCase = true)||
                    it.date.contains(searchText.trim(), ignoreCase = true)||
                    it.location.contains(searchText.trim(), ignoreCase = true)||
                    it.counselorRequest.contains(searchText.trim(), ignoreCase = true)||
                    it.textContent.contains(searchText.trim(), ignoreCase = true)||
                    it.authorName.contains(searchText.trim(), ignoreCase = true)||
                    it.authorId.contains(searchText.trim(), ignoreCase = true)
            val tabMatches = when (selectedCategory) {
                0 -> it.status.equals("TBS", ignoreCase = true)
                1 -> it.status.equals("Pending", ignoreCase = true)
                2 -> it.status.equals("Scheduled", ignoreCase = true)
                3 -> it.status.equals("Resolved", ignoreCase = true)
                else -> true
            }
            tabMatches && textMatches
        }
    }


    val listAlpha by animateFloatAsState(
        targetValue = if (isRefreshing) 0.3f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "ListFadeAnimation"
    )

    val users by postViewModel.usersList.collectAsState()
    var selectedReport by remember { mutableStateOf<Report?>(null) }
    var openedInitial by remember { mutableStateOf(false) }

    LaunchedEffect(initialReportId, reportsList) {
        if (initialReportId != null && !openedInitial) {
            reportsList.find { it.reportId == initialReportId }?.let {
                selectedReport = it
                openedInitial = true
            }
        }
    }

    val filteredUsers = remember(users, searchText) {
        users.filter {
            searchText.isBlank() ||
                    it.name.contains(searchText.trim(), ignoreCase = true) ||
                    it.idNumber.contains(searchText.trim(), ignoreCase = true) ||
                    it.email.contains(searchText.trim(), ignoreCase = true)
        }
    }


    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = { Text("Incident Reports", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
                ViewToggle(
                    currentView = currentView,
                    current = currentView,
                    onChange = { currentView = it },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onScheduleClick,
                    icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = "Schedule") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onHomeClick,
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") }
                )
                if (userRole.isCounselorOrAdmin) {
                    NavigationBarItem(
                        selected = true,
                        onClick = onReportClick,
                        icon = {
                            Icon(
                                Icons.Filled.Report,
                                contentDescription = "Reports"
                            )
                        }
                    )
                }
                NavigationBarItem(
                    selected = false,
                    onClick = onSettingClick,
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") }
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ){
            AnimatedVisibility(
                visible = currentView != ReportsVIEW.STATISTICS,
                enter = fadeIn(animationSpec = tween(300)) + expandHorizontally(
                    animationSpec = tween(300),
                    expandFrom = Alignment.Start
                ),
                exit = fadeOut(animationSpec = tween(300)) + shrinkHorizontally(
                    animationSpec = tween(300),
                    shrinkTowards = Alignment.Start
                )
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Search reports...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                scope.launch {
                                    postViewModel.fetchReports()
                                    keyboardController?.hide()
                                }
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )
                }
            }


            AnimatedContent(
                targetState = currentView,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                transitionSpec = {
                    val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                    (slideInHorizontally { it * dir } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it * dir } + fadeOut())
                },
                label = "ViewTransition"

            ) { view ->
                when (view) {
                    ReportsVIEW.REPORTS -> {
                        // Pull to Refresh Section
                        PullToRefreshBox(
                            isRefreshing = isRefreshing || isViewModelRefreshing,
                            onRefresh = {
                                scope.launch {
                                    isRefreshing = true
                                    postViewModel.fetchReports()
                                    authViewModel.fetchUserProfile()
                                    delay(1000.milliseconds)
                                    isRefreshing = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                LazyRow(
                                    modifier = Modifier
                                        .graphicsLayer { alpha = listAlpha }
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(categories.size) { index ->
                                        FilterChip(
                                            selected = selectedCategory == index,
                                            onClick = { selectedCategory = index },
                                            label = {
                                                Text(
                                                    text = categories[index],
                                                    fontWeight = if (selectedCategory == index) FontWeight.Bold else FontWeight.Medium
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                containerColor = MaterialTheme.colorScheme.surface,
                                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                    }
                                }



                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { alpha = listAlpha },
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (filteredReports.isEmpty()) {
                                        item {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 80.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.ReportProblem,
                                                    contentDescription = "No reports",
                                                    modifier = Modifier.size(64.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text(
                                                    text = when (categories[selectedCategory].lowercase()) {
                                                        "pending" -> "No pending reports"
                                                        "scheduled" -> "No scheduled reports"
                                                        "resolved" -> "No resolved reports"
                                                        else -> "No reports"
                                                    },
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    } else {
                                        items(
                                            items = filteredReports,
                                            key = { it.reportId }) { report ->
                                            ReportCardItem(
                                                report = report,
                                                onResolvedClick = { reportId ->
                                                    viewModel.markReportAsResolved(reportId = reportId)
                                                },
                                                onScheduleAppointmentClick = { posterId, reportId ->
                                                    onScheduleAppointmentClick(
                                                        posterId,
                                                        reportId
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }


                    ReportsVIEW.OVERVIEW -> {

                        LaunchedEffect(Unit) {postViewModel.fetchUsers()}

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(categories.size) {
                                }
                            }


                            // Pull to Refresh Section
                            PullToRefreshBox(
                                isRefreshing = isRefreshing || isViewModelRefreshing,
                                onRefresh = {
                                    scope.launch {
                                        isRefreshing = true
                                        postViewModel.fetchUsers()
                                        authViewModel.fetchUserProfile()
                                        delay(1000.milliseconds)
                                        isRefreshing = false
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { alpha = listAlpha },
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (filteredUsers.isEmpty() || filteredReports.isEmpty()) {
                                        item {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 80.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.ReportProblem,
                                                    contentDescription = "No reports",
                                                    modifier = Modifier.size(64.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text(
                                                    text = when (categories[selectedCategory].lowercase()) {
                                                        "pending" -> "No user found"
                                                        "scheduled" -> "No user found"
                                                        "resolved" -> "no use found"
                                                        else -> "No reports"
                                                    },
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    } else {
                                        items(
                                            items = filteredUsers,
                                            key = { it.uid?: it.idNumber }) { user ->
                                            UserCardItem(
                                                user = user,
                                                reports = reportsList,
                                                onRecordClick = { reportId ->
                                                    selectedReport = reportsList.find { it.reportId == reportId }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ReportsVIEW.STATISTICS ->{
                        StatisticsScreen(
                            reports = reportsList,
                            authViewModel = authViewModel,
                            postViewModel = postViewModel
                        )
                    }


                }

            }
        }
    }
    selectedReport?.let { report ->
        Dialog(onDismissRequest = { selectedReport = null }) {
            Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                ReportCardItem(
                    report = report,
                    onResolvedClick = { id -> viewModel.markReportAsResolved(reportId = id) },
                    onScheduleAppointmentClick = { posterId, reportId ->
                        selectedReport = null
                        onScheduleAppointmentClick(posterId, reportId)
                    }
                )
            }
        }
    }
}




@Composable
fun ReportCardItem(
    report: Report,
    onScheduleAppointmentClick: (posterId: String, reportId: String) -> Unit = { _, _ -> },
    onResolvedClick: (String) -> Unit,
) {

    var showResolveDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }


    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 300))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = MaterialTheme.colorScheme.onSurfaceVariant)
            ) { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding( 16.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ReportProblem,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.authorName.ifBlank { "Anonymous Student" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Submitted report",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = {
                        when (report.status) {
                            "TBS" -> onScheduleAppointmentClick(report.authorId, report.reportId)
                            "Scheduled" -> showResolveDialog = true
                        }
                    },
                    enabled = report.status == "TBS" || report.status == "Scheduled",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = when (report.status) {
                            "TBS" -> "Set Schedule"
                            "Pending" -> "Pending"
                            "Scheduled" -> "Set as Resolved"
                            "Resolved" -> "Resolved"
                            else -> "Unknown"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (showResolveDialog) {
                    AlertDialog(
                        onDismissRequest = { showResolveDialog = false },
                        title = { Text("Confirm Resolution") },
                        text = { Text("Are you sure you want to mark this report as resolved? This action cannot be undone.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showResolveDialog = false
                                    onResolvedClick(report.reportId) // Trigger the actual action here
                                }
                            ) {
                                Text("Confirm")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showResolveDialog = false }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }

            }



            Spacer(modifier = Modifier.height(12.dp))

            // ---------- Tags ----------
            if (report.incident.isNotBlank() || report.counselorRequest.isNotBlank()) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (report.incident.isNotBlank()) {
                        Text(
                            text = report.incident,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.onErrorContainer, RoundedCornerShape(8.dp))
                                .padding(5.dp)

                        )
                    }

                    Text(
                        text = formatTimes(report.timestamp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                }
            }

            // ---------- Expanded details ----------
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                Spacer(modifier = Modifier.height(12.dp))

                ReportDetailLine("Title", report.title)

                if (report.counselorRequest == "Yes") {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    Text(
                        text = "Requires Counselor Talk",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                ReportDetailLine("Reported by", report.authorName)
                ReportDetailLine("ID number", report.authorId)
                ReportDetailLine("Date", report.date)
                ReportDetailLine("Location", report.location)

                val personList = report.peopleInvolved.map { map ->
                    PersonInvolved(

                        name = map["name"] ?: "",
                        course = map["course"] ?: "",
                        year = map["year"] ?: "",
                        role = map["role"] ?: "",
                        idList = map["idList"] ?: ""
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                if (personList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "People Involved",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))


                    personList.forEach { person ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = person.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            PersonDetailRow("Course", person.course)
                            PersonDetailRow("Year", person.year)
                            PersonDetailRow("Section", person.section)
                            PersonDetailRow("Role", person.role)
                            PersonDetailRow("ID", person.idList)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (report.textContent.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = report.textContent,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }

                report.mediaUrl?.let { url ->
                    if (url.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        AsyncImage(
                            model = optimizedUrl(url),
                            contentDescription = "Report attachment",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun UserCardItem(
    user: PostViewModel.AppUser,
    reports: List<Report> = emptyList(),
    onRecordClick: (reportId: String) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    //user's records
    val idNumber = user.idNumber.trim()

    val involvedIn = remember(reports, idNumber) {
        if (idNumber.isEmpty()) emptyList()
        else reports.filter { report ->
            report.peopleInvolved.any { it["idList"]?.trim() == idNumber }
        }
    }

    val submitted = remember(reports, idNumber) {
        if (idNumber.isEmpty()) emptyList()
        else reports.filter { report ->
            report.userIdNumber.trim() == idNumber || report.authorId == user.uid
        }
    }

    val involvedList = remember(involvedIn) { involvedIn.toTitles() }
    val submittedList = remember(submitted) { submitted.toTitles() }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 300))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = MaterialTheme.colorScheme.onSurfaceVariant)
            ) { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ---------- Header ----------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name.ifBlank { "Unnamed" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "ID: ${user.idNumber}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (user.role.isNotBlank()) {
                    Text(
                        text = user.role.replaceFirstChar { it.uppercase() },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                if (user.status.isNotBlank()) {
                    val active = user.status.equals("active", ignoreCase = true)
                    Text(
                        text = user.status.replaceFirstChar { it.uppercase() },
                        fontSize = 14.sp,
                        color = if (active) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .background(
                                if (active) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }


            // ---------- Expanded details ----------
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                ReportDetailLine("Email", user.email)
                ReportDetailLine("ID number", user.idNumber)
                ReportDetailLine("Course", user.course)
                ReportDetailLine("Age", user.age)
                ReportDetailLine("Phone", user.phoneNumber)

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


fun List<Report>.toTitles() = map { report ->
    ReportsTitle(
        reportId = report.reportId,
        title = report.title.ifBlank { report.incident },
        date = report.date,
        status = report.status
    )
}

@Composable
fun RecordSection(
    title: String,
    records: List<ReportsTitle>,
    onRecordClick: (String) -> Unit
) {
    Text(
        text = "$title (${records.size})",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

    if (records.isEmpty()) {
        Text(
            text = "None",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }

    records.forEach { record ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { onRecordClick(record.reportId) }
                .padding(12.dp)
        ) {
            Text(
                text = record.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))

            ReportDetailLine("Date", record.date)
            ReportDetailLine("Status", record.status)
        }
    }
}


@Composable
private fun PersonDetailRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.padding(top = 2.dp)) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}



@Composable
private fun ReportDetailLine(label: String, value: String) {
    if (value.isBlank()) return
    Text(
        text = "$label: $value",
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 20.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun ViewToggle(
    currentView: ReportsVIEW,
    current: ReportsVIEW,
    onChange: (ReportsVIEW) -> Unit,
    modifier: Modifier = Modifier
) {
    val count = ReportsVIEW.entries.size
    val index = ReportsVIEW.entries.indexOf(current)
    val isLeft = index == 0
    val bigRadius = 44.dp

    val topStart by animateDpAsState(if (isLeft) 0.dp else bigRadius, label = "topStart")
    val topEnd by animateDpAsState(if (isLeft) bigRadius else 0.dp, label = "topEnd")

    val dynamicShape = when(currentView){
        ReportsVIEW.REPORTS -> RoundedCornerShape(
            topStart = topStart,
            topEnd = topEnd,
        )
        ReportsVIEW.OVERVIEW -> RoundedCornerShape(
            topStart = topStart,
            topEnd = topStart,
        )
        ReportsVIEW.STATISTICS -> RoundedCornerShape(
            topStart = topStart,
            topEnd = topEnd,

            )
    }



    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val itemWidth = maxWidth / count

        val indicatorOffset by animateDpAsState(
            targetValue = itemWidth * index,
            animationSpec = tween(250),
            label = "indicatorOffset"
        )

        // Sliding highlight
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(itemWidth)
                .fillMaxHeight()
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = dynamicShape
                )
        )

        Row(modifier = Modifier.fillMaxSize()) {
            ReportsVIEW.entries.forEach { view ->
                val selected = view == current
                val textColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "textColor"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onChange(view) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when(view) {
                            ReportsVIEW.REPORTS ->"Reports"
                            ReportsVIEW.OVERVIEW -> "Overview"
                            ReportsVIEW.STATISTICS -> "Statistics"

                        },
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}



fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        now,
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()
}

fun formatTimes(timestamp: Long): String {
    // Customize the pattern to your liking:
    // "MMM dd, yyyy • hh:mm a" -> Oct 12, 2026 • 04:24 PM
    // "dd/MM/yyyy HH:mm" -> 12/10/2026 16:24
    val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

