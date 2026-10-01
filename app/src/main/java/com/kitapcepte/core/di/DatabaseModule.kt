package com.kitapcepte.core.di

import android.content.Context
import androidx.room.Room
import com.kitapcepte.data.local.AppDatabase
import com.kitapcepte.data.local.dao.CartDao
import com.kitapcepte.data.local.dao.FavoriteDao
import com.kitapcepte.data.local.dao.SavedCardDao
import com.kitapcepte.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    fun provideCartDao(database: AppDatabase): CartDao = database.cartDao()

    @Provides
    fun provideSavedCardDao(database: AppDatabase): SavedCardDao = database.savedCardDao()
}

