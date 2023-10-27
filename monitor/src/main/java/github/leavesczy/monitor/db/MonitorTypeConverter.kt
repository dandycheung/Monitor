package github.leavesczy.monitor.db

import androidx.room.TypeConverter
import github.leavesczy.monitor.provider.JsonHnadler

/**
 * @Author: leavesCZY
 * @Date: 2020/11/8 14:43
 * @Desc:
 * @Github：https://github.com/leavesCZY
 */
internal class MonitorTypeConverter {

    @TypeConverter
    fun fromJsonArray(json: String): List<MonitorPair> {
        return JsonHnadler.fromJsonArray(json, MonitorPair::class.java)
    }

    @TypeConverter
    fun toJson(list: List<MonitorPair>): String {
        return JsonHnadler.toJson(list)
    }

}