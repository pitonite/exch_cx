package io.github.pitonite.exch_cx.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.pitonite.exch_cx.workmanager.ExchWorkManager
import io.github.pitonite.exch_cx.workmanager.ExchWorkManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ExchWorkManagerModule {

  @Provides
  @Singleton
  fun provideWorkManager(@ApplicationContext context: Context): ExchWorkManager {
    return ExchWorkManagerImpl(context)
  }
}
