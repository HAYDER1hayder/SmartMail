package com.example.smartmail.di

import com.example.smartmail.data.remote.N8nApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://n8n.your-friend-server.com/"

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            // تحويل الـ JSON القادم من الإنترنت إلى كائنات Kotlin (SmartMailEntity)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideN8nApi(retrofit: Retrofit): N8nApi {
        return retrofit.create(N8nApi::class.java)
    }
}