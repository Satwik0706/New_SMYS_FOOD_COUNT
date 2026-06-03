package com.satwik.oodapplication.domain.repository

import com.satwik.oodapplication.data.model.Attendance
import com.satwik.oodapplication.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    fun getAttendanceForDate(date: String): Flow<Resource<List<Attendance>>>
    suspend fun saveAttendance(attendanceList: List<Attendance>): Resource<Unit>
    suspend fun clearOldAttendance(beforeDate: String): Resource<Unit>
}
