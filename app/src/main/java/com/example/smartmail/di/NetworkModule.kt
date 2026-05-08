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

    // 👈 تأكد من وضع رابط سيرفر Spring Boot الخاص بك هنا (مثلاً localhost أو IP السيرفر)
    private const val BASE_URL = "http://10.0.2.2:9090/"

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideBackendApi(retrofit: Retrofit): BackendApi {
        return retrofit.create(BackendApi::class.java)
    }
}
