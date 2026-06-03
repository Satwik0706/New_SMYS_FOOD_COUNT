package com.satwik.oodapplication.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.satwik.oodapplication.data.repository.*
import com.satwik.oodapplication.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context
    ): AuthRepository = FirebaseAuthRepositoryImpl(auth, firestore, context)

    @Provides
    @Singleton
    fun provideMenuRepository(
        firestore: FirebaseFirestore
    ): MenuRepository = FirebaseMenuRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideFoodCountRepository(
        firestore: FirebaseFirestore
    ): FoodCountRepository = FirebaseFoodCountRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideAttendanceRepository(
        firestore: FirebaseFirestore
    ): AttendanceRepository = FirebaseAttendanceRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideNotificationRepository(
        firestore: FirebaseFirestore
    ): NotificationRepository = FirebaseNotificationRepositoryImpl(firestore)
}
