package io.github.pitonite.exch_cx.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.pitonite.exch_cx.data.room.ExchDatabase
import java.io.File
import javax.inject.Singleton

private const val DATABASE_FILE_NAME = "data.db"

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

  @Provides
  @Singleton
  fun provideExchDatabase(@ApplicationContext context: Context): ExchDatabase {
    return Room.databaseBuilder(
            context,
            ExchDatabase::class.java,
            DATABASE_FILE_NAME,
        )
        .fallbackToDestructiveMigration()
        .build()
  }
}
