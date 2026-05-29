package com.example.data

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class SubtaskConverter {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, Subtask::class.java)
    private val adapter = moshi.adapter<List<Subtask>>(listType)

    @TypeConverter
    fun fromString(value: String?): List<Subtask>? {
        return if (value.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                adapter.fromJson(value)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    @TypeConverter
    fun fromList(list: List<Subtask>?): String? {
        return adapter.toJson(list ?: emptyList())
    }
}
