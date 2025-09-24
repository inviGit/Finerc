package com.invi.finerc.transaction

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.invi.finerc.models.SMSTransactionEntity
import com.invi.finerc.models.CollectionEntity
import com.invi.finerc.models.CollectionSmsMappingEntity

@Database(
    entities = [
        SMSTransactionEntity::class,
        CollectionEntity::class,
        CollectionSmsMappingEntity::class
    ], 
    version = 4, 
    exportSchema = false
)
abstract class SmsTransactionDatabase : RoomDatabase() {
    abstract fun smsTransactionDao(): SmsTransactionDao
    abstract fun collectionDao(): CollectionDao

    companion object {
        private const val DATABASE_NAME = "sms_transactions.db"

        @Volatile
        private var INSTANCE: SmsTransactionDatabase? = null

        // Migration from version 1 to 3
        private val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create collections table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS collections (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL
                    )
                """)
                
                // Create collection_sms_mapping table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS collection_sms_mapping (
                        collection_id INTEGER NOT NULL,
                        sms_id INTEGER NOT NULL,
                        PRIMARY KEY(collection_id, sms_id),
                        FOREIGN KEY(collection_id) REFERENCES collections(id) ON DELETE CASCADE,
                        FOREIGN KEY(sms_id) REFERENCES sms_transactions(id) ON DELETE CASCADE
                    )
                """)
                
                // Create indices for better performance
                database.execSQL("CREATE INDEX IF NOT EXISTS index_collection_sms_mapping_collection_id ON collection_sms_mapping(collection_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_collection_sms_mapping_sms_id ON collection_sms_mapping(sms_id)")
            }
        }

        fun getInstance(context: Context): SmsTransactionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmsTransactionDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("SmsTransactionDatabase", "Database created successfully")
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d("SmsTransactionDatabase", "Database opened successfully")

                            // Check if table exists
                            val cursor =
                                db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='sms_transactions'")
                            val tableExists = cursor.count > 0
                            cursor.close()

                            Log.d(
                                "SmsTransactionDatabase",
                                "Table 'sms_transactions' exists: $tableExists"
                            )

                            if (tableExists) {
                                // Get table info
                                val tableInfoCursor =
                                    db.query("PRAGMA table_info(sms_transactions)")
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
                    .build()

                INSTANCE = instance
                instance
            }
        }

        fun destroyInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}