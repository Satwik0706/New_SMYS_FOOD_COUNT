package com.satwik.oodapplication.domain.repository

import com.satwik.oodapplication.data.model.Menu
import com.satwik.oodapplication.utils.Resource
import kotlinx.coroutines.flow.Flow

interface MenuRepository {
    fun getMenu(date: String): Flow<Resource<Menu>>
    suspend fun updateMenu(menu: Menu): Resource<Unit>
    suspend fun clearOldMenu(beforeDate: String): Resource<Unit>
}
