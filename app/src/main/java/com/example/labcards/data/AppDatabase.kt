package com.example.labcards.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.labcards.data.dao.CardDao
import com.example.labcards.data.dao.ExperimentDao
import com.example.labcards.data.model.CardTemplateEntity
import com.example.labcards.data.model.ExperimentCardEntity
import com.example.labcards.data.model.ExperimentTemplateEntity
import com.example.labcards.data.model.FlowNodeEntity

@Database(
    entities = [
        CardTemplateEntity::class,
        ExperimentTemplateEntity::class,
        ExperimentCardEntity::class,
        FlowNodeEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun experimentDao(): ExperimentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "labcards_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
