package com.gmail.zajcevserg.maptestapp.model.application

import android.app.Application

import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

import com.gmail.zajcevserg.maptestapp.model.di.appModule
import com.gmail.zajcevserg.maptestapp.model.di.dataModule


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(listOf(appModule, dataModule))
        }
    }
}