package com.somanath.chatdemo.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.getstream.chat.android.ui.common.helper.DateFormatter
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object UtilModule {

    @Singleton
    @Provides
    fun providesDateFormatter(application: Application): DateFormatter {
        return DateFormatter.from(application)
    }
}