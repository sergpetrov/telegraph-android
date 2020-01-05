package com.telex.model.source.local

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.telex.BaseApp
import com.telex.model.source.local.dao.PageDao
import com.telex.model.source.local.dao.UserDao
import com.telex.model.source.local.entity.Page
import com.telex.model.source.local.entity.User
import com.telex.utils.TelegraphContentConverter
import timber.log.Timber

/**
 * @author Sergey Petrov
 */
@Database(entities = [User::class, Page::class], version = 6, exportSchema = false)
@TypeConverters(RoomTypeConverters::class)
abstract class TelegraphDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun pageDao(): PageDao

    companion object {
        @Volatile
        private var INSTANCE: TelegraphDatabase? = null
        private val DATABASE_NAME = "Telegraph.db"

        fun getInstance(context: Context): TelegraphDatabase {
            if (INSTANCE == null) {
                synchronized(TelegraphDatabase::class.java) {
                    if (INSTANCE == null) {
                        val builder = Room.databaseBuilder(context.applicationContext, TelegraphDatabase::class.java, DATABASE_NAME)
                                .addMigrations(
                                        MIGRATION_2_3,
                                        MIGRATION_3_4,
                                        MIGRATION_4_5,
                                        MIGRATION_5_6
                                )

                        INSTANCE = builder.build()
                    }
                }
            }
            return INSTANCE!!
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val telegraphContentConverter = TelegraphContentConverter()
                val roomTypeConverters = RoomTypeConverters()

                val cursor = database.query("SELECT * FROM Page WHERE draft=1")
                cursor.use {
                    while (cursor.moveToNext()) {
                        try {
                            val values = ContentValues()
                            DatabaseUtils.cursorRowToContentValues(cursor, values)

                            val html = values.getAsString("content")
                            val nodes = telegraphContentConverter.htmlToNodes(html.orEmpty())
                            val json = roomTypeConverters.listNodeElementDataToString(nodes) ?: " "

                            values.put("content", json)

                            database.insert("Page", CONFLICT_REPLACE, values)
                        } catch (error: Exception) {
                            Timber.e(error)
                        }
                    }
                }

                database.execSQL("CREATE TABLE Page_New (" +
                        "id INTEGER PRIMARY KEY NOT NULL," +
                        "path TEXT," +
                        "url TEXT," +
                        "title TEXT," +
                        "imageUrl TEXT," +
                        "views INTEGER NOT NULL," +
                        "authorName TEXT," +
                        "authorUrl TEXT," +
                        "content TEXT NOT NULL," +
                        "visible INTEGER," +
                        "draft INTEGER NOT NULL" +
                        ")"
                )
                database.execSQL("DELETE FROM Page WHERE draft=0")
                database.execSQL("INSERT INTO Page_New SELECT * FROM Page")
                database.execSQL("DROP TABLE Page")
                database.execSQL("ALTER TABLE Page_New RENAME TO Page")
            }
        }

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Page ADD COLUMN number INTEGER")
                database.execSQL("DELETE FROM Page WHERE draft=0")
            }
        }

        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val appData = BaseApp.instance.appData

                database.execSQL("ALTER TABLE Page ADD COLUMN userId TEXT NOT NULL DEFAULT '${appData.getCurrentAccessToken() ?: ""}'")
            }
        }

        val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Page ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
