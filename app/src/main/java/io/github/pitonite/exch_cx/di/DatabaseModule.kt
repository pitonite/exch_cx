package io.github.pitonite.exch_cx.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.pitonite.exch_cx.data.room.ExchDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

  @Provides
  @Singleton
  fun provideExchDatabase(@ApplicationContext context: Context): ExchDatabase {
    return Room.databaseBuilder(
            context,
            ExchDatabase::class.java,
            "data.db",
        )
        .fallbackToDestructiveMigration()
        .build()
  }
}
