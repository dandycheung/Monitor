package github.leavesczy.monitor.internal.db

import androidx.room.TypeConverter
import github.leavesczy.monitor.internal.JsonHandler

/**
 * @Author: leavesCZY
 * @Date: 2020/11/8 14:43
 * @Desc:
 * @Github：https://github.com/leavesCZY
 */
internal class MonitorTypeConverter {

    @TypeConverter
    fun fromJsonArray(json: String): List<MonitorPair> {
        return JsonHandler.fromJsonArray(json, MonitorPair::class.java)
    }

    @TypeConverter
    fun toJson(list: List<MonitorPair>): String {
        return JsonHandler.toJson(list)
    }

}