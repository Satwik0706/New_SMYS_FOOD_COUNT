package com.satwik.oodapplication.utils

import java.security.MessageDigest

data class OfficeBearer(
    val title: String,
    val name: String
)

object AppInfo {
    const val APP_NAME = "SMYS Food Count"
    const val SUBTITLE = "75th Year Special Edition"
    
    const val PURPOSE = "SMYS Food Count is a digital hostel food management system designed to collect daily meal counts efficiently. It helps manage Breakfast, Lunch, Snacks, and Dinner requirements. The system reduces manual work, minimizes food wastage, and helps the administration plan food preparation accurately."

    const val ACKNOWLEDGEMENT = "We sincerely thank the hostel administration, wardens, staff, and students for their valuable support and cooperation. Their feedback and involvement helped us improve the system and make the food-counting process more efficient. We appreciate everyone who contributed to the successful implementation of SMYS Food Count."

    const val SATHPANATHA_TITLE = "Sathpanatha 2K26–2K27 — 75th Year"
    
    val OFFICE_BEARERS = listOf(
        OfficeBearer("General Secretary", "Aditya Mathad"),
        OfficeBearer("Joint General Secretary", "Pushkar Deshpande"),
        OfficeBearer("Treasurer", "Aniruddh Joshi"),
        OfficeBearer("Cultural Secretary", "Aprameya N. Acharya"),
        OfficeBearer("Joint Cultural Secretary", "Govardhan M. Jatkar"),
        OfficeBearer("Food Secretary", "G. Shree Skanda Upadhya"),
        OfficeBearer("Joint Food Secretary", "Koustubh B. Adi"),
        OfficeBearer("Sports Secretary", "K. Abhiram Purankia"),
        OfficeBearer("Joint Sports Secretary", "Satwik G J"),
        OfficeBearer("Internal Secretary", "Suvyaktha S U"),
        OfficeBearer("Joint Internal Secretary", "Amol S H"),
        OfficeBearer("Library & Media Secretary", "Pranav T M"),
        OfficeBearer("Joint Library & Media Secretary", "Poornaprajna Kulkarni"),
        OfficeBearer("Member", "V. Yashas"),
        OfficeBearer("Member", "Abhiram Ashrit")
    )

    const val INITIATED_BY = "Pranav T M "
    const val SUPPORTED_BY = "Abhisheka Holla, Pushkar Deshpande"
    const val CREDITS_SUPPORT_TEXT = "Their suggestions, encouragement, and support played an important role in shaping the idea and improving the application."
    const val TESTING_FEEDBACK = "Our sincere thanks to the 2024–2028 batch of SMYS Hostel students who participated in testing the application, provided valuable feedback, identified issues, and helped improve the overall experience."

    const val LETTER_TO_FUTURE_TITLE = "A Letter to the Future"
    const val LETTER_TO_FUTURE_BODY = "Dear SMYS Community,\n\nSMYS Food Count was created with a simple purpose — to make hostel food management easier, smarter, and more efficient for everyone. We hope this application continues to serve its users, reduce food wastage, and make the daily food-counting process easier for students and hostel management for years to come.\n\nAs batches change and new students become part of SMYS, we hope this small initiative continues to grow and remain useful to every generation that uses it. May this application continue to serve the SMYS community for as long as it runs, carrying forward the effort and support of everyone who contributed to it."

    const val DEVELOPER_NAME = "Satwik G J"
    const val DEVELOPER_HANDLE = "@developing_developer"

    // Cryptographic SHA-256 hash signature of "$APP_NAME|$DEVELOPER_NAME|$DEVELOPER_HANDLE|${OFFICE_BEARERS.size}"
    private const val EXPECTED_SIGNATURE = "f7e306a77a4b0ebf930177ee70d55da6f76da4a3d1f4ed4fac2e8ac85d4f842d"

    fun verifyIntegrity(): Boolean {
        return try {
            val payload = "$APP_NAME|$DEVELOPER_NAME|$DEVELOPER_HANDLE|${OFFICE_BEARERS.size}"
            val bytes = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
            val computedHash = bytes.joinToString("") { "%02x".format(it) }
            computedHash == EXPECTED_SIGNATURE
        } catch (e: Exception) {
            false
        }
    }
}
