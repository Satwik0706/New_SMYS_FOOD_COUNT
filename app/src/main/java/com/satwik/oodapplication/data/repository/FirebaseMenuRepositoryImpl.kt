package com.satwik.oodapplication.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.satwik.oodapplication.data.model.Menu
import com.satwik.oodapplication.domain.repository.MenuRepository
import com.satwik.oodapplication.utils.Constants
import com.satwik.oodapplication.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseMenuRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MenuRepository {

    override fun getMenu(date: String): Flow<Resource<Menu>> = callbackFlow {
        val subscription = firestore.collection(Constants.COLLECTION_MENU)
            .document(date)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error"))
                    return@addSnapshotListener
                }
                val menu = snapshot?.toObject(Menu::class.java) ?: Menu(date = date)
                trySend(Resource.Success(menu))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateMenu(menu: Menu): Resource<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_MENU)
                .document(menu.date)
                .set(menu)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Menu update failed")
        }
    }

    override suspend fun clearOldMenu(beforeDate: String): Resource<Unit> {
        return try {
            val snapshots = firestore.collection(Constants.COLLECTION_MENU)
                .whereLessThan("date", beforeDate)
                .get()
                .await()
            
            val batch = firestore.batch()
            snapshots.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to clear old menu")
        }
    }
}
