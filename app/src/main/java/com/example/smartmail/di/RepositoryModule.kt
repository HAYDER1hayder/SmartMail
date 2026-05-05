package com.example.smartmail.di

import com.example.smartmail.data.repository.MailRepositoryImpl
import com.example.smartmail.domain.repository.MailRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMailRepository(
        mailRepositoryImpl: MailRepositoryImpl
    ): MailRepository
}