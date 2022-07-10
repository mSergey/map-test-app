package com.gmail.zajcevserg.maptestapp.model.di


import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

import com.gmail.zajcevserg.maptestapp.viewmodel.LayersVM

val appModule = module {

    // single view model for the app
    viewModel<LayersVM> {
        LayersVM(
            repository = get(),
            mSavedCheckedStates = get()
        )
    }

    // layers switch position storage
    single<MutableMap<Int, Boolean>> {
        mutableMapOf()
    }
}
