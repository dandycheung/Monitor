package github.leavesczy.monitor.internal.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import github.leavesczy.monitor.internal.ContextProvider

/**
 * @Author: leavesCZY
 * @Date: 2020/11/8 14:43
 * @Desc:
 * @Github：https://github.com/leavesCZY
 */
@Database(
    entities = [Monitor::class],
    version = 32
)
@TypeConverters(MonitorTypeConverter::class)
internal abstract class MonitorDatabase : RoomDatabase() {

    companion object {

        private const val DB_NAME = "Monitor"

        const val MonitorTableName = "MonitorHttp"

        private var monitorDatabase: MonitorDatabase? = null

        val instance: MonitorDatabase
            get() {
                return monitorDatabase ?: synchronized(lock = MonitorDatabase::class.java) {
                    val cache = monitorDatabase
                    if (cache != null) {
                        return@synchronized cache
                    }
                    val db = createDb(context = ContextProvider.context)
                    monitorDatabase = db
                    return@synchronized db
                }
            }

        private fun createDb(context: Context): MonitorDatabase {
            return Room.databaseBuilder(
                context,
                MonitorDatabase::class.java,
                DB_NAME
            ).fallbackToDestructiveMigration()
                .build()
        }

    }

    abstract val monitorDao: MonitorDao

}