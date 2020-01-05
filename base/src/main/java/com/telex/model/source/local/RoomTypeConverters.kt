package com.telex.model.source.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.telex.model.source.local.entity.Nodes
import com.telex.model.source.remote.data.NodeElementData
import java.util.Date

class RoomTypeConverters {
    private var gson = Gson()

    @TypeConverter
    fun stringToNodes(data: String?) = gson.fromJson(data, Nodes::class.java)

    @TypeConverter
    fun nodesToString(obj: Nodes?): String? = gson.toJson(obj)

    @TypeConverter
    fun stringToNodeElementData(data: String?) = gson.fromJson(data, NodeElementData::class.java)

    @TypeConverter
    fun nodeElementDataToString(obj: NodeElementData?): String? = gson.toJson(obj)

    @TypeConverter
    fun mutableMapToString(obj: MutableMap<String, String>?): String? = gson.toJson(obj)

    @TypeConverter
    fun stringToMutableMap(data: String?): MutableMap<String, String>? {
        return gson.fromJson(data, object : TypeToken<MutableMap<String, String>>() {}.type)
    }

    @TypeConverter
    fun listNodeElementDataToString(obj: List<NodeElementData>?): String? = gson.toJson(obj)

    @TypeConverter
    fun stringToListNodeElementData(data: String?): List<NodeElementData>? {
        return gson.fromJson(data, object : TypeToken<List<NodeElementData>>() {}.type)
    }

    @TypeConverter
    fun toDate(timestamp: Long): Date {
        return Date(timestamp)
    }

    @TypeConverter
    fun toTimestamp(date: Date): Long {
        return date.time
    }
}
