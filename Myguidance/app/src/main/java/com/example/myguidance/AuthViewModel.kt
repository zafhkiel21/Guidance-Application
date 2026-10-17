package com.example.myguidance

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance("guidance")

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _yearLevel = MutableStateFlow("")
    val yearLevel: StateFlow<String> = _yearLevel

    private val _userIdNumber = MutableStateFlow("")
    val userIdNumber: StateFlow<String> = _userIdNumber
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail

    private val _userRole = MutableStateFlow(UserRole.UNKNOWN)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
            if (firebaseAuth.currentUser != null) {
                fetchUserProfile()
            }
        }
    }


    fun loginUser(
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || pass.isBlank()) {
            onError("Please fill in all fields.")
            return
        }

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _currentUser.value = auth.currentUser
                    onSuccess()
                } else {
                    onError(task.exception?.localizedMessage ?: "Authentication failed.")
                }
            }
    }

    fun setUserRole(role: String) {
        _userRole.value = UserRole.fromString(role)
    }
    fun signUpUser(
        email: String,
        pass: String,
        confirmPass: String,
        name: String,
        role: String = "Student",
        idNumber: String,
        course: String,
        year: String = "First year",
        section: String = "A",
        age: String,
        phoneNumber: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,

    ) {
        if (email.isBlank() || pass.isBlank() || confirmPass.isBlank()) {
            onError("Please fill in all fields.")
            return
        }
        if (pass != confirmPass) {
            onError("Passwords do not match.")
            return
        }
        if (pass.length < 6) {
            onError("Password must be at least 6 characters.")
            return
        }

    auth.createUserWithEmailAndPassword(email, pass)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid ?: ""

                val userProfile = hashMapOf(
                    "email" to email,
                    "name" to name,
                    "role" to role,
                    "idNumber" to idNumber,
                    "course" to course,
                    "year" to year,
                    "section" to section,
                    "age" to age,
                    "phoneNumber" to phoneNumber,
                    "status" to "active",

                )

                db.collection("users").document(userId)
                    .set(userProfile)
                    .addOnSuccessListener {
                        println("FIRESTORE_SUCCESS: Data written successfully!")
                        _yearLevel.value = year
                        _userEmail.value = email
                        _currentUser.value = auth.currentUser
                        _userName.value = name
                        _userIdNumber.value = idNumber
                        _userRole.value = UserRole.fromString(role)
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onError(e.localizedMessage ?: "Failed to create user profile.")
                    }
            } else {
                onError(task.exception?.localizedMessage ?: "Registration failed.")
            }

}
    }

    fun fetchUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    _yearLevel.value = document.getString("year") ?: ""
                    _userIdNumber.value = document.getString("idNumber") ?: ""
                    _userName.value = document.getString("name") ?: ""
                    _userEmail.value = document.getString("email") ?: (auth.currentUser?.email ?: "")
                    val rawRole = document.getString("role")
                    _userRole.value = UserRole.fromString(rawRole)
                }
            }
    }



    fun logout() {
        auth.signOut()
        _userIdNumber.value = ""
        _currentUser.value = null
        _userName.value = ""
        _userEmail.value = ""
        _userRole.value = UserRole.UNKNOWN
    }
    }
