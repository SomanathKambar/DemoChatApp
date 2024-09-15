package com.somanath.chatdemo.di

import android.app.Application
import com.somanath.chatdemo.data.repository.UserCredentialsRepository
import com.somanath.chatdemo.data.repository.UserCredentialsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object UserModule {

    @Singleton
    @Provides
    fun providesUserCredentialRepository(application: Application): UserCredentialsRepository {
        return UserCredentialsRepositoryImpl(application)
    }
}