package com.gmail.zajcevserg.maptestapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gmail.zajcevserg.maptestapp.R
import com.gmail.zajcevserg.maptestapp.model.database.LayerItem
import com.gmail.zajcevserg.maptestapp.model.repository.Repository
import com.gmail.zajcevserg.maptestapp.ui.activity.log



class LayersVM : ViewModel() {

    private val repository: Repository = Repository()
    //private var state: SwitchStates = SwitchStates.STATE_NONE
    private val savedLayers = mutableListOf<LayerItem>()
    private var needToSaveCurrentLayers = true
    private var modeWithoutUndefineState = false

    val liveDataLayers by lazy {
        MutableLiveData<MutableList<LayerItem>>()
    }

    val liveDataMapInteraction by lazy {
        MutableLiveData<Int>()
    }

    val liveDataMapType by lazy {
        MutableLiveData<Int>()
    }

    /*val liveDataSwitchControlAllAppearance by lazy {
        MutableLiveData<SwitchStates>()
    }*/

    val liveDataSearchMode by lazy {
        MutableLiveData<Boolean>().apply {
            value = false
        }
    }

    val liveDataDragMode by lazy {
        MutableLiveData<Boolean>().apply {
            value = false
        }
    }

    val liveDataBackgroundBtn by lazy {
        MutableLiveData<String>()
    }

    init {
        repository.requestLayers { layers ->


            liveDataLayers.value = layers
                .sortedBy { !it.isSharedLayer }
                .toMutableList()

            val isAllActive = layers.all { it.turnedOn }
            val isAllInactive = layers.none { it.turnedOn }

            if (needToSaveCurrentLayers) {
                savedLayers.clear()
                savedLayers.addAll(layers)
            }

            modeWithoutUndefineState = needToSaveCurrentLayers && (isAllActive || isAllInactive)

            /*state = when {
                isAllActive -> SwitchStates.STATE_ALL_ACTIVE
                isAllInactive -> SwitchStates.STATE_ALL_INACTIVE
                else -> SwitchStates.STATE_UNDEFINED
            }
            liveDataSwitchControlAllAppearance.value = state*/
        }

    }

    fun turnOn(idToUpdate: Int, checked: Boolean) {
        repository.updateLayer(idToUpdate, checked)
        liveDataLayers.value?.let { layers ->
            layers.forEach {
                if (it.id == idToUpdate) {
                    it.turnedOn = checked
                }
            }
        }
    }

    fun removeLayers() {
        val idsToRemove =
            liveDataLayers.value?.filter { it.selectedToRemove }?.map { it.id }

        if (idsToRemove != null && idsToRemove.isNotEmpty()) {
            repository.deleteLayers(idsToRemove)
            val notToRemove = liveDataLayers.value?.filter { !it.selectedToRemove }
            liveDataLayers.value = notToRemove?.toMutableList()
        }
    }

    fun expandLayer(idToUpdate: Int) {
        liveDataLayers.value?.let { layers ->
            val updatedLayers = mutableListOf<LayerItem>()
            updatedLayers.addAll(layers)
            updatedLayers.forEach {
                when {
                    it.id == idToUpdate && it.expanded -> {
                        it.expanded = false
                    }
                    it.id == idToUpdate && !it.expanded -> {
                        it.expanded = true
                    }
                    it.id != idToUpdate && it.expanded -> {
                        it.expanded = false
                    }
                    it.id != idToUpdate && !it.expanded -> {
                        // nothing to do  ...
                    }
                }
            }
            liveDataLayers.value = updatedLayers
        }
    }

    fun transparencyChange(layerId: Int, transparency: Int) {
        liveDataLayers.value?.let { list ->
            val layerItem = list.find { it.id == layerId }
            val index = list.indexOf(layerItem)
            list[index].transparency = transparency
            repository.updateLayer(layerId, transparency)
        }
    }

    fun getCoordinates() = repository.polygonOptions


    fun onLayerBackgroundButtonClicked(viewId: Int, position: Int) {

        when (viewId) {
            R.id.button_one -> liveDataBackgroundBtn.value = "Button one pressed on $position layer"
            R.id.button_two -> liveDataBackgroundBtn.value = "Button two pressed on $position layer"
        }
        liveDataBackgroundBtn.value = null
    }

    /*fun onSwitchControlAllLayersClick(currentSwitchStateLevel: SwitchStates) {
        if (!modeWithoutUndefineState) needToSaveCurrentLayers = false
        //log("currentSwitchStateLevel $currentSwitchStateLevel")
        if (modeWithoutUndefineState) {
            state = when (currentSwitchStateLevel) {
                SwitchStates.STATE_ALL_ACTIVE -> {
                    repository.updateActiveStateAll(false)
                    SwitchStates.STATE_ALL_INACTIVE
                }

                SwitchStates.STATE_ALL_INACTIVE -> {
                    repository.updateActiveStateAll(true)
                    SwitchStates.STATE_ALL_ACTIVE
                }
                SwitchStates.STATE_UNDEFINED -> return
                SwitchStates.STATE_NONE -> return
            }
        } else {
            state = when (currentSwitchStateLevel) {
                SwitchStates.STATE_ALL_ACTIVE -> {
                    repository.updateActiveStateAll(false)
                    SwitchStates.STATE_ALL_INACTIVE
                }

                SwitchStates.STATE_ALL_INACTIVE -> {
                    repository.updateAllLayers(savedLayers)
                    SwitchStates.STATE_UNDEFINED
                }

                SwitchStates.STATE_UNDEFINED -> {
                    repository.updateActiveStateAll(true)
                    SwitchStates.STATE_ALL_ACTIVE
                }
                SwitchStates.STATE_NONE -> return
            }
        }

        liveDataSwitchControlAllAppearance.value = state
    }*/

}