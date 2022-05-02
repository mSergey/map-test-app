package com.gmail.zajcevserg.maptestapp.model.application

import android.app.Application
import androidx.room.Room
import com.gmail.zajcevserg.maptestapp.model.database.LayersDB


class App : Application() {


    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(applicationContext, LayersDB::class.java, "LayersDB.db")
            .createFromAsset("layers_database.db")
            .build()

    }

    companion object {
        lateinit var database: LayersDB
    }

}