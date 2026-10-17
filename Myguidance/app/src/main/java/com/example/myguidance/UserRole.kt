package com.example.myguidance

enum class UserRole(val key: String) {
    STUDENT("student"),
    TEACHER("teacher"),
    COUNSELOR("counselor"),
    ADMIN("admin"),
    UNKNOWN("");

    val isStudent: Boolean
        get() = this == STUDENT
    val isCounselorOrAdmin: Boolean
        get() = this == COUNSELOR || this == ADMIN

    val canCreatePost: Boolean
        get() = this != UNKNOWN

    companion object {
        fun fromString(roleString: String?): UserRole {
            val cleanRole = roleString?.trim()?.lowercase().orEmpty()
            return entries.find { it.key == cleanRole } ?: UNKNOWN
        }
    }
}