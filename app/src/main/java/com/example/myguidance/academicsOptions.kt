package com.example.myguidance

enum class Course(val label: String, val code: String) {
    ACCOUNTANCY("BS Accountancy", "BSA"),
    ACCOUNTING_INFO_SYSTEM("BS Accounting Information System", "BSAIS"),
    ACCOUNTING_TECHNOLOGY("BS Accounting Technology", "BSAT"),
    BUSINESS_ADMINISTRATION("BS Business Administration", "BSBA"),
    CIVIL_ENGINEERING("BS Civil Engineering", "BSCE"),
    CRIMINOLOGY("BS Criminology", "BSCrim"),
    HOSPITALITY_MANAGEMENT("BS Hospitality Management", "BSHM"),
    HOTEL_RESTAURANT_MANAGEMENT("BS Hotel and Restaurant Management", "BSHRM"),
    INFORMATION_SYSTEMS("BS Information Systems", "BSIS"),
    PSYCHOLOGY("BS Psychology", "BSPsych");

    companion object {
        /** Full names, in the same order as before, for the dropdown. */
        val labels: List<String> = entries.map { it.label }

        /** "BS Psychology" -> "BSPsych". Unknown values are returned unchanged. */
        fun codeOf(label: String): String =
            entries.find { it.label == label }?.code ?: label
    }
}

enum class YearLevel(val label: String, val code: String) {
    FIRST("First Year", "1st"),
    SECOND("Second Year", "2nd"),
    THIRD("Third Year", "3rd"),
    FOURTH("Fourth Year", "4th");

    companion object {
        val labels: List<String> = entries.map { it.label }

        fun codeOf(label: String): String =
            entries.find { it.label == label }?.code ?: label
    }
}