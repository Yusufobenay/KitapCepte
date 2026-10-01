package com.kitapcepte.core.di

import com.kitapcepte.data.mapper.DeterministicPriceProvider
import com.kitapcepte.data.repository.AuthRepositoryImpl
import com.kitapcepte.data.repository.BookRepositoryImpl
import com.kitapcepte.data.repository.ProfileRepositoryImpl
import com.kitapcepte.data.repository.SessionRepositoryImpl
import com.kitapcepte.domain.model.PriceProvider
import com.kitapcepte.domain.repository.AuthRepository
import com.kitapcepte.domain.repository.BookRepository
import com.kitapcepte.domain.repository.ProfileRepository
import com.kitapcepte.domain.repository.SessionRepository
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
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindBookRepository(impl: BookRepositoryImpl): BookRepository

    @Binds
    @Singleton
    abstract fun bindPriceProvider(impl: DeterministicPriceProvider): PriceProvider

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
}
