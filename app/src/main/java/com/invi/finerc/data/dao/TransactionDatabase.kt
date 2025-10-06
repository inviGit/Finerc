package com.invi.finerc.data.dao

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.invi.finerc.data.entity.BillCycleEntity
import com.invi.finerc.data.entity.CollectionEntity
import com.invi.finerc.data.entity.CollectionItemExclusionEntity
import com.invi.finerc.data.entity.CollectionSmsMappingEntity
import com.invi.finerc.data.entity.CreditCardEntity
import com.invi.finerc.data.entity.TransactionEntity
import com.invi.finerc.data.entity.TransactionItemEntity
import java.util.concurrent.Executors

@Database(
    entities = [
        TransactionEntity::class,
        CollectionEntity::class,
        CollectionSmsMappingEntity::class,
        CreditCardEntity::class,
        BillCycleEntity::class,
        TransactionItemEntity::class,
        CollectionItemExclusionEntity::class
    ],
    version = 19,
    exportSchema = true
)
abstract class TransactionDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun collectionDao(): CollectionDao

    abstract fun transactionItemDao(): TransactionItemDao

    companion object {
        private const val DATABASE_NAME = "transactions.db"

        @Volatile
        private var INSTANCE: TransactionDatabase? = null

        fun getInstance(context: Context): TransactionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TransactionDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_18_19)
//                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("SmsTransactionDatabase", "Database created successfully")
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d("SmsTransactionDatabase", "Database opened successfully")

                            // Check if table exists
                            val cursor =
                                db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='transactions'")
                            val tableExists = cursor.count > 0
                            cursor.close()

                            Log.d(
                                "SmsTransactionDatabase",
                                "Table 'transactions' exists: $tableExists"
                            )

                            if (tableExists) {
                                // Get table info
                                val tableInfoCursor =
                                    db.query("PRAGMA table_info(transactions)")
                                Log.d("SmsTransactionDatabase", "Table columns:")
                                while (tableInfoCursor.moveToNext()) {
                                    val columnName = tableInfoCursor.getString(1)
                                    val columnType = tableInfoCursor.getString(2)
                                    Log.d("SmsTransactionDatabase", "  - $columnName: $columnType")
                                }
                                tableInfoCursor.close()
                            }
                        }
                    })
                    .setQueryCallback(object : RoomDatabase.QueryCallback {
                        override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
                            Log.d("RoomQuery", "SQL: $sqlQuery | Args: $bindArgs")
                        }
                    }, Executors.newSingleThreadExecutor())
                    .build()

                INSTANCE = instance
                instance
            }
        }

        fun destroyInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }

        // Simple migration: Only adds new table, no data changes
        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("Migration", "Starting migration from 18 to 19 - Adding collection_item_exclusions table")

                // Create the new table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `collection_item_exclusions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `collection_id` INTEGER NOT NULL,
                        `item_id` INTEGER NOT NULL,
                        FOREIGN KEY(`collection_id`) REFERENCES `collections`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`item_id`) REFERENCES `transaction_items`(`itemId`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())

                // Create index on collection_id
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS `index_collection_item_exclusions_collection_id` 
                    ON `collection_item_exclusions` (`collection_id`)
                """.trimIndent())

                // Create index on item_id
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS `index_collection_item_exclusions_item_id` 
                    ON `collection_item_exclusions` (`item_id`)
                """.trimIndent())

                Log.d("Migration", "Migration from 18 to 19 completed successfully")
            }
        }
    }
}