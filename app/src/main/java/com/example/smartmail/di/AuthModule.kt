package com.example.smartmail.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideGoogleAuthClient(
        app: Application
    ): com.example.smartmail.domain.auth.GoogleAuthClient {
        return com.example.smartmail.domain.auth.GoogleAuthClient(
            context = app,
            webClientClientId = "102275364921-qaasetdfukkbcfdi0un5m67nq5en6p7t.apps.googleusercontent.com"
        )
    }

}
