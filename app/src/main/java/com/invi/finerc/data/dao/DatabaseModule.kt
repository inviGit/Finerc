package com.invi.finerc.data.dao

import android.content.Context
import android.util.Log
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): TransactionDatabase {
        return Room.databaseBuilder(context, TransactionDatabase::class.java, "finerc_database")
//            .fallbackToDestructiveMigration()
            .addMigrations(TransactionDatabase.MIGRATION_18_19)
            .setQueryCallback({ sqlQuery, bindArgs ->
                Log.d("RoomQuery", "SQL: $sqlQuery, Args: $bindArgs")
            }, Executors.newSingleThreadExecutor())
            .build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: TransactionDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideCollectionDao(database: TransactionDatabase): CollectionDao {
        return database.collectionDao()
    }

    @Provides
    @Singleton
    fun provideTransactionItemDao(database: TransactionDatabase): TransactionItemDao {
        return database.transactionItemDao()
    }
}
