package com.gmail.zajcevserg.maptestapp.model.di

import androidx.room.Room
import com.gmail.zajcevserg.maptestapp.model.database.LayersDB
import com.gmail.zajcevserg.maptestapp.model.database.LayersDao
import com.gmail.zajcevserg.maptestapp.model.repository.Repository

import io.reactivex.subjects.PublishSubject

import org.koin.dsl.module


val dataModule = module {

    // database object
    single<LayersDao> {
        Room.databaseBuilder(get(), LayersDB::class.java, "LayersDB.db")
            .createFromAsset("layers_database.db")
            .build()
            .getDao()
    }

    // repository
    single<Repository> {
        Repository(
            dao = get(),
            layerItemSubject = PublishSubject.create(),
            checkedFlagsSubject = PublishSubject.create(),
            searchTextSubject = PublishSubject.create()
        )
    }

}
