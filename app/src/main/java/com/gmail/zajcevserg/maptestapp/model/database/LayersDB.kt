package com.gmail.zajcevserg.maptestapp.model.database


import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [DataItem.LayerItem::class, LayerObject::class],
    version = 2
)
abstract class LayersDB : RoomDatabase() {
    abstract fun getDao(): LayersDao
}
