package com.gmail.zajcevserg.maptestapp.viewmodel

import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gmail.zajcevserg.maptestapp.R
import com.gmail.zajcevserg.maptestapp.model.database.LayerItem
import com.gmail.zajcevserg.maptestapp.model.repository.Repository
import com.gmail.zajcevserg.maptestapp.ui.activity.log
import com.gmail.zajcevserg.maptestapp.ui.custom.Switch3Way


class LayersVM : ViewModel() {

    private val repository: Repository = Repository()
    //val checkedStates = mutableListOf<Boolean>()
    val mSavedCheckedStates = mutableMapOf<Int, Boolean>()


    val liveDataLayers by lazy {
        MutableLiveData<MutableList<LayerItem>>()
    }

    val liveDataMapInteraction by lazy {
        MutableLiveData<Int>()
    }

    val liveDataMapType by lazy {
        MutableLiveData<Int>()
    }

    val liveDataMainSwitchPosition by lazy {
        MutableLiveData<Switch3Way.SwitchPositions>()
    }

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

    val liveDataIsSwitchTreeWay by lazy {
        MutableLiveData<Boolean>().apply {
             value = true
        }
    }

    init {
        repository.requestLayers { layers ->

            liveDataLayers.value = layers
                .sortedBy { !it.isSharedLayer }
                .toMutableList()

            liveDataLayers.value?.forEach {
                log("${it.id} ${it.turnedOn}")
            }


            mSavedCheckedStates.clear()
            liveDataLayers.value?.forEach {
                mSavedCheckedStates[it.id] = it.turnedOn
            }

            liveDataMainSwitchPosition.value = defineMainSwitchPosition(layers)
            liveDataIsSwitchTreeWay.value = isDifferent(layers)
        }
    }



    fun onLayerSwitchClicked(idToUpdate: Int, checked: Boolean) {
        log("id $idToUpdate")
        repository.updateLayer(idToUpdate, checked)

        liveDataLayers.value?.let { layers ->

            val isAllCheckedBefore = layers.all { it.turnedOn }
            val isNoneCheckedBefore = layers.none { it.turnedOn }
            val isDifferentCheckedBefore = !isAllCheckedBefore && !isNoneCheckedBefore

            layers.find { it.id == idToUpdate }?.turnedOn = checked

            val isAllCheckedAfter = layers.all { it.turnedOn }
            val isNoneCheckedAfter = layers.none { it.turnedOn }
            val isDifferentCheckedAfter = !isAllCheckedAfter && !isNoneCheckedAfter

            when {
                isAllCheckedBefore && isDifferentCheckedAfter -> {
                    mSavedCheckedStates.clear()
                    layers.forEach {
                        mSavedCheckedStates[it.id] = it.turnedOn
                    }
                    liveDataIsSwitchTreeWay.value = true
                    liveDataMainSwitchPosition.value = Switch3Way.SwitchPositions.MIDDLE
                }
                isNoneCheckedBefore && isDifferentCheckedAfter -> {
                    mSavedCheckedStates.clear()
                    layers.forEach {
                        mSavedCheckedStates[it.id] = it.turnedOn
                    }
                    liveDataIsSwitchTreeWay.value = true
                    liveDataMainSwitchPosition.value = Switch3Way.SwitchPositions.MIDDLE
                }
                isDifferentCheckedBefore && isAllCheckedAfter -> {
                    liveDataIsSwitchTreeWay.value = false
                    liveDataMainSwitchPosition.value = Switch3Way.SwitchPositions.END
                }
                isDifferentCheckedBefore && isNoneCheckedAfter -> {
                    liveDataIsSwitchTreeWay.value = false
                    liveDataMainSwitchPosition.value = Switch3Way.SwitchPositions.START
                }
                isDifferentCheckedBefore && isDifferentCheckedAfter -> {
                    mSavedCheckedStates.clear()
                    layers.forEach {
                        mSavedCheckedStates[it.id] = it.turnedOn
                    }
                    liveDataIsSwitchTreeWay.value = true
                    liveDataMainSwitchPosition.value = Switch3Way.SwitchPositions.MIDDLE
                }
            }
        }
    }


    fun setCheckedStatesForAll(checked: Boolean) {
        repository.updateCheckedStateAll(checked)
        liveDataLayers.value?.forEach {
            it.turnedOn = checked
        }
    }

    fun setSavedCheckedStates() {
        repository.updateCheckedByFlags(mSavedCheckedStates)
        mSavedCheckedStates.forEach { mapEntry ->
            val item = liveDataLayers.value?.find {
                it.id == mapEntry.key
            }
            item?.let {
                it.turnedOn = mapEntry.value
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
            idsToRemove.forEach {
                mSavedCheckedStates.remove(it)
            }
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
            //repository.updateLayer(layerId, transparency)
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

    fun updateLayersOrderInDB() {
        repository.updateLayers(liveDataLayers.value!!)
    }

    override fun onCleared() {
        super.onCleared()
        repository.layerItemSubjectDisposable.dispose()
        repository.checkedFlagsSubjectDisposable.dispose()
    }

    private fun isDifferent(list: List<LayerItem>): Boolean {
        val isAllActive = list.all { it.turnedOn }
        val isAllInactive = list.none { it.turnedOn }
        return !isAllActive && !isAllInactive
    }

    private fun defineMainSwitchPosition(layers: List<LayerItem>
    ): Switch3Way.SwitchPositions {
        val isAllActive = layers.all { it.turnedOn }
        val isAllInactive = layers.none { it.turnedOn }
        return when {
            isAllActive -> Switch3Way.SwitchPositions.END
            isAllInactive -> Switch3Way.SwitchPositions.START
            else -> Switch3Way.SwitchPositions.MIDDLE
        }
    }

}