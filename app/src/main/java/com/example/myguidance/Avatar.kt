package com.example.myguidance

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore

/** App-wide cache so each user's photo is read from Firestore only once. */
object AvatarCache {
    val photos = mutableStateMapOf<String, String>()
    private val requested = mutableSetOf<String>()
    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance("guidance")

    fun load(userId: String) {
        if (userId.isBlank() || !requested.add(userId)) return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { photos[userId] = it.getString("photoUrl").orEmpty() }
            .addOnFailureListener { photos[userId] = "" }
    }

    /** Call after a user changes their picture so every screen updates. */
    fun set(userId: String, url: String) {
        requested.add(userId)
        photos[userId] = url
    }
}

/** Shows a photo from a URL, or the default person icon if there is none. */
@Composable
fun Avatar(
    photoUrl: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl.isNotBlank()) {
            AsyncImage(
                model = photoUrl.replace("http://", "https://"),
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "User Avatar",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

/** Same thing, but you only give it a user ID. It loads and caches the photo itself. */
@Composable
fun UserAvatar(
    userId: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    onClick: (() -> Unit)? = null
) {
    LaunchedEffect(userId) { AvatarCache.load(userId) }
    Avatar(
        photoUrl = AvatarCache.photos[userId].orEmpty(),
        modifier = modifier,
        size = size,
        onClick = onClick
    )
}