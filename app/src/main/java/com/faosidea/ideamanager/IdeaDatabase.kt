package com.faosidea.ideamanager

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Task::class],
    version = 1,
    exportSchema = false
)
@TypeConverters()
abstract class IdeaDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var instance: IdeaDatabase? = null

        fun getDatabase(context: Context): IdeaDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }

        private fun buildDatabase(appContext: Context) =
            Room.databaseBuilder(appContext, IdeaDatabase::class.java, "idea_database.db")
                .fallbackToDestructiveMigration()
                .build()
    }

}
