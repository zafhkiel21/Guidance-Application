package com.example.myguidance

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

private const val POST_LIFETIME_MS = 7L * 24 * 60 * 60 * 1000 // 7 days

data class Post(
    val location: String = "",
    val title: String = "",
    val date: String = "",
    val time: String = "",
    val startTimeMillis: Long = 0L,
    val endTimeInMillis: Long = 0L,
    val category: String = "",
    val postId: String = "",
    val authorId: String = "",
    val name: String = "",
    val textContent: String = "",
    val mediaUrl: String? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + POST_LIFETIME_MS
)

data class Report(
    val location: String = "",
    val yearLevel: String = "",
    val course: String = "",
    val userIdNumber: String = "",
    val title: String = "",
    val date: String = "",
    val time: String = "",
    val estimatedTimeInMillis: Long = 0L,
    val name: String = "",
    val reportId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val category: String = "",
    val incident: String = "",
    val counselorRequest: String = "",
    val peopleInvolved: List<Map<String, String>> = emptyList(),
    val textContent: String = "",
    val mediaUrl: String? = null,
    val status: String = "PENDING",
    val timestamp: Long = System.currentTimeMillis()
)

class PostViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance("guidance")

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _postsList = MutableStateFlow<List<Post>>(emptyList())
    val postsList: StateFlow<List<Post>> = _postsList.asStateFlow()

    private val _reportsList = MutableStateFlow<List<Report>>(emptyList())
    val reportsList: StateFlow<List<Report>> = _reportsList.asStateFlow()

    private var postsListener: ListenerRegistration? = null
    private var reportsListener: ListenerRegistration? = null

    init {
        fetchPosts()
        fetchReports()
    }

    @SuppressLint("EmptySuperCall")
    override fun onCleared() {
        usersListener?.remove()
        postsListener?.remove()
        reportsListener?.remove()
        super.onCleared()
    }


    fun fetchPosts() {
        postsListener?.remove()
        postsListener = db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                _isRefreshing.value = false
                if (error != null || snapshot == null) return@addSnapshotListener


                _postsList.value = snapshot.documents
                    .mapNotNull { it.toObject(Post::class.java) }

            }
    }

    fun fetchReports() {
        reportsListener?.remove()
        reportsListener = db.collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                _reportsList.value = snapshot.documents
                    .mapNotNull { it.toObject(Report::class.java) }
            }
    }




    data class PersonRecord(
        val uid: String?,
        val profile: Map<String, Any>?,
        val involvedIn: List<Report>,
        val submitted: List<Report>
    )

    fun loadPersonRecord(idNumber: String, onResult: (PersonRecord) -> Unit) {
        val id = idNumber.trim()
        val empty = PersonRecord(null, null, emptyList(), emptyList())
        if (id.isEmpty()) { onResult(empty); return }


        db.collection("users")
            .whereEqualTo("idNumber", id)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                val userDoc = snapshot.documents.firstOrNull()
                val reports = _reportsList.value   // already loaded by fetchReports()

                onResult(
                    PersonRecord(
                        uid = userDoc?.id,
                        profile = userDoc?.data,
                        involvedIn = reports.filter { report ->
                            report.peopleInvolved.any { it["idList"]?.trim() == id } &&
                                    report.status.equals("Resolved", ignoreCase = true)
                        },
                        submitted = reports.filter { report ->
                            report.userIdNumber == id ||
                                    (userDoc != null && report.authorId == userDoc.id)
                        }
                    )
                )
            }
            .addOnFailureListener { onResult(empty) }
    }

    data class AppUser(
        @DocumentId val uid: String? = "",
        val name: String = "",
        val email: String = "",
        val idNumber: String = "",
        val course: String = "",
        val age: String = "",
        val phoneNumber: String = "",
        val role: String = "",
        val status: String = ""
    )

    private val _usersList = MutableStateFlow<List<AppUser>>(emptyList())
    val usersList: StateFlow<List<AppUser>> = _usersList.asStateFlow()
    private var usersListener: ListenerRegistration? = null

    fun fetchUsers() {
        usersListener?.remove()
        usersListener = db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _usersList.value = snapshot.documents
                    .mapNotNull { it.toObject(AppUser::class.java) }
                    .sortedBy { it.name.lowercase() }
            }
    }


    fun toggleLike(postId: String) {
        val userId = auth.currentUser?.uid ?: return
        val postRef = db.collection("posts").document(postId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)

            @Suppress("UNCHECKED_CAST")
            val likedBy = snapshot.get("likedBy") as? List<String> ?: emptyList()
            val likes = snapshot.getLong("likesCount") ?: 0L
            val alreadyLiked = userId in likedBy

            transaction.update(
                postRef,
                mapOf(
                    "likedBy" to if (alreadyLiked) likedBy - userId else likedBy + userId,
                    "likesCount" to if (alreadyLiked) (likes - 1).coerceAtLeast(0) else likes + 1
                )
            )
        }
    }


    fun createReport(
        location: String,
        userIdNumber: String,
        selectedDateInMillis: Long,
        estimatedHour: Int,
        estimatedMinute: Int,
        date: String,
        time: String,
        title: String,
        wantsCounselorTalk: Boolean?,
        peopleList: List<PersonInvolved>,
        authorName: String,
        category: String,
        incident: String,
        textContent: String,
        context: Context,
        selectedMediaUri: Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = currentUserOrError(onError) ?: return
        val resolvedName = user.resolveName(authorName)
        val estimatedTimeInMillis = calculateTimestamp(selectedDateInMillis, estimatedHour, estimatedMinute)

        submitDocument(
            collection = "reports",
            context = context,
            mediaUri = selectedMediaUri,
            failureMessage = "Failed to save report.",
            onSuccess = onSuccess,
            onError = onError
        ) { reportId, mediaUrl ->
            mapOf(
                "location" to location,
                "userIdNumber" to userIdNumber,
                "title" to title,
                "date" to date,
                "time" to time,
                "estimatedTimeInMillis" to estimatedTimeInMillis,
                "reportId" to reportId,
                "authorId" to user.uid,
                "authorName" to resolvedName,
                "category" to category,
                "incident" to incident,
                "counselorRequest" to counselorRequestText(wantsCounselorTalk),
                "peopleInvolved" to peopleList.map { it.toFirestoreMap() },
                "textContent" to textContent,
                "mediaUrl" to mediaUrl,
                "status" to "TBS",
                "timestamp" to System.currentTimeMillis()
            )
        }
    }

    fun createPost(
        location: String,
        date: String,
        time: String,
        title: String,
        selectedDateInMillis: Long,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        category: String,
        authorName: String,
        textContent: String,
        context: Context,
        selectedMediaUri: Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = currentUserOrError(onError) ?: return

        if (textContent.isBlank() && selectedMediaUri == null) {
            onError("Post cannot be empty.")
            return
        }

        val resolvedName = user.resolveName(authorName)
        val startTimeMillis = calculateTimestamp(selectedDateInMillis, startHour, startMinute)
        val endTimeInMillis = calculateTimestamp(selectedDateInMillis, endHour, endMinute)

        submitDocument(
            collection = "posts",
            context = context,
            mediaUri = selectedMediaUri,
            failureMessage = "Failed to save post.",
            onSuccess = onSuccess,
            onError = onError
        ) { postId, mediaUrl ->
            val now = System.currentTimeMillis()
            mapOf(
                "title" to title,
                "date" to date,
                "time" to time,
                "startTimeMillis" to startTimeMillis,
                "endTimeInMillis" to endTimeInMillis,
                "location" to location,
                "category" to category,
                "postId" to postId,
                "authorId" to user.uid,
                "name" to resolvedName,
                "textContent" to textContent,
                "mediaUrl" to mediaUrl,
                "likesCount" to 0,
                "commentsCount" to 0,
                "likedBy" to emptyList<String>(),
                "timestamp" to now,
                "expiresAt" to now + POST_LIFETIME_MS
            )
        }
    }


    private fun submitDocument(
        collection: String,
        context: Context,
        mediaUri: Uri?,
        failureMessage: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        buildDocument: (id: String, mediaUrl: String?) -> Map<String, Any?>
    ) {
        _isUploading.value = true

        val fail: (String) -> Unit = { message ->
            _isUploading.value = false
            onError(message)
        }

        fun save(mediaUrl: String?) {
            val ref = db.collection(collection).document()
            ref.set(buildDocument(ref.id, mediaUrl))
                .addOnSuccessListener {
                    _isUploading.value = false
                    onSuccess()
                }
                .addOnFailureListener { e -> fail(e.localizedMessage ?: failureMessage) }
        }

        if (mediaUri == null) {
            save(mediaUrl = null)
        } else {
            CloudinaryHelper.uploadMedia(
                context = context,
                fileUri = mediaUri,
                onSuccess = { uploadedUrl -> save(uploadedUrl) },
                onError = { err -> fail(err) }
            )
        }
    }

    private fun currentUserOrError(onError: (String) -> Unit): FirebaseUser? =
        auth.currentUser ?: run {
            onError("User not authenticated.")
            null
        }

    private fun FirebaseUser.resolveName(provided: String): String =
        provided.ifBlank { displayName ?: email?.substringBefore("@") ?: "Anonymous" }

    private fun counselorRequestText(wantsCounselorTalk: Boolean?): String =
        when (wantsCounselorTalk) {
            true -> "Yes, please reach out to schedule a private chat."
            false -> "No, I just wanted to report this incident for awareness."
            null -> "Not specified"
        }

    private fun PersonInvolved.toFirestoreMap(): Map<String, String> = mapOf(
        "name" to name,
        "role" to role,
        "idList" to idList,
        "section" to section,
        "year" to year,
        "course" to course
    )

    private fun calculateTimestamp(dateInMillis: Long, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            timeInMillis = dateInMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis


    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private var currentUid: String? = null

    fun loadNotifications(
        uid: String

    ) {
        currentUid = uid

        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            val idNumber = doc.getString("idNumber").orEmpty()
            val seen = (doc.get("seenNotifIds") as? List<*>)
                ?.filterIsInstance<String>()?.toSet() ?: emptySet()

            loadPersonRecord(idNumber) { record ->
                val items = buildList {
                    record.involvedIn.forEach { r ->
                        val nid = "involved_${r.reportId}_${r.status}"
                        add(NotificationItem(
                            id = nid,
                            title = "Report resolved",
                            message = "You were involved in \"${r.title}\", which is now ${r.status}.",
                            timeAgo = relativeTime(r.timestamp),
                            isRead = nid in seen
                        ))
                    }


                    record.submitted.forEach { r ->
                        val nid = "submitted_${r.reportId}_${r.status}"
                        add(NotificationItem(
                            id = nid,
                            title = "Report update",
                            message = "Your report \"${r.title}\" is now ${r.status}.",
                            timeAgo = relativeTime(r.timestamp),
                            isRead = nid in seen ,
                            reportId = r.reportId,
                            status = r.status
                        ))
                    }
                }.sortedBy { it.isRead }

                _notifications.value = items
            }
        }
    }

    fun markAsSeen(item: NotificationItem) {
        if (item.isRead) return
        // update the UI immediately
        _notifications.value = _notifications.value.map {
            if (it.id == item.id) it.copy(isRead = true) else it
        }
        // persist it
        currentUid?.let { uid ->
            db.collection("users").document(uid)
                .update("seenNotifIds", FieldValue.arrayUnion(item.id))
        }
    }

    private fun relativeTime(millis: Long): String =
        android.text.format.DateUtils.getRelativeTimeSpanString(
            millis, System.currentTimeMillis(), android.text.format.DateUtils.MINUTE_IN_MILLIS
        ).toString()
}

