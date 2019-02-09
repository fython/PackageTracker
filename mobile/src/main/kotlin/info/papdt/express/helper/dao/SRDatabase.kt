package info.papdt.express.helper.dao

import android.app.Application
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import info.papdt.express.helper.model.Category

object SRDatabase {

    const val DATABASE_NAME = "sr_database"

    private lateinit var instance: SRDatabase.Instance

    fun init(context: Context) {
        val appContext = if (context is Application) {
            context
        } else {
            context.applicationContext
        }
        instance = Room.databaseBuilder(appContext, Instance::class.java, DATABASE_NAME)
                .build()
    }

    val categoryDao: CategoryDao get() = instance.categoryDao

    @Database(
            entities = [Category::class],
            version = 1
    )
    abstract class Instance : RoomDatabase() {

        abstract val categoryDao: CategoryDao

    }


}