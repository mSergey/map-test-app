package com.gmail.zajcevserg.maptestapp.viewmodel


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import io.reactivex.disposables.Disposable

import com.gmail.zajcevserg.maptestapp.R
import com.gmail.zajcevserg.maptestapp.model.application.*
import com.gmail.zajcevserg.maptestapp.model.database.DataItem
import com.gmail.zajcevserg.maptestapp.model.database.LayerObject
import com.gmail.zajcevserg.maptestapp.model.repository.Repository
import com.gmail.zajcevserg.maptestapp.ui.custom.SwipeCallback
import com.gmail.zajcevserg.maptestapp.ui.custom.Switch3Way


class LayersVM : ViewModel() {

    private val repository: Repository = Repository()
    private var layersFlowableDisposable: Disposable? = null

    val mSavedCheckedStates = mutableMapOf<Int, Boolean>()

    val liveDataLayers by lazy {
        MutableLiveData<MutableList<DataItem>>()
    }

    val liveDataLayersFlowable by lazy {
        MutableLiveData<MutableList<out DataItem>>()
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

    val liveDataSearch by lazy {
        MutableLiveData<List<LayerObject>?>()
    }

    init {
        repository.requestLayers { layers ->
            updateLiveDataSet(layers, liveDataLayers, mSavedCheckedStates)
            liveDataMainSwitchPosition.value = defineMainSwitchPosition(layers)
            liveDataIsSwitchTreeWay.value = isDifferent(layers)
        }
        repository.observeSearchResult {
            liveDataSearch.value = it
        }

        layersFlowableDisposable =
            repository.observeLayers {
            liveDataLayersFlowable.value = it
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.clear()
        layersFlowableDisposable?.dispose()
    }

    fun onLayerSwitchClicked(idToUpdate: Int, checked: Boolean) {
        repository.updateLayerChecked(idToUpdate, checked)
        liveDataLayers.value?.let { layers ->
            val isAllCheckedBefore = layers.allExceptHeader { it.turnedOn }
            val isNoneCheckedBefore = layers.noneExceptHeader { it.turnedOn }
            val isDifferentCheckedBefore = !isAllCheckedBefore && !isNoneCheckedBefore
            layers.findExceptHeader { it.id == idToUpdate }?.turnedOn = checked
            val isAllCheckedAfter = layers.allExceptHeader { it.turnedOn }
            val isNoneCheckedAfter = layers.noneExceptHeader { it.turnedOn }
            val isDifferentCheckedAfter = !isAllCheckedAfter && !isNoneCheckedAfter
            when {
                isAllCheckedBefore && isDifferentCheckedAfter -> {
                    mSavedCheckedStates.clear()
                    layers.forEachExceptHeader {
                        mSavedCheckedStates[it.id] = it.turnedOn
                    }
                    liveDataIsSwitchTreeWay.value = true
                    liveDataMainSwitchPosition.value = Switch3Way.SwitchPositions.MIDDLE
                }
                isNoneCheckedBefore && isDifferentCheckedAfter -> {
                    mSavedCheckedStates.clear()
                    layers.forEachExceptHeader {
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
                    layers.forEachExceptHeader {
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
        liveDataLayers.value?.forEachExceptHeader {
            it.turnedOn = checked
        }
    }

    fun setSavedCheckedStates() {
        repository.updateCheckedByFlags(mSavedCheckedStates)
        mSavedCheckedStates.forEach { mapEntry ->
            val item = liveDataLayers.value?.findExceptHeader {
                it.id == mapEntry.key
            }
            item?.let {
                it.turnedOn = mapEntry.value
            }
        }
    }

    fun removeLayers() {
        val idsToRemove =
            liveDataLayers.value
                ?.filterExceptHeader { it.selectedToRemove }
                ?.map { it.id }
        if (idsToRemove != null && idsToRemove.isNotEmpty()) {
            repository.deleteLayers(idsToRemove)
            idsToRemove.forEach { mSavedCheckedStates.remove(it) }
            val notToRemove = liveDataLayers.value?.filterNot {
                it is DataItem.LayerItem && it.selectedToRemove
            }
            if (notToRemove!!.isEmpty() ||
                (notToRemove.size == 1 && notToRemove.first() is DataItem.Header)) {
                liveDataLayers.value = mutableListOf<DataItem>().apply { add(DataItem.Header(Int.MAX_VALUE)) }
                liveDataMainSwitchPosition.value = Switch3Way.SwitchPositions.MIDDLE
                liveDataIsSwitchTreeWay.value = true
                return
            } else {
                liveDataLayers.value = notToRemove.toMutableList()
                liveDataMainSwitchPosition.value =
                    defineMainSwitchPosition(liveDataLayers.value!!)
                liveDataIsSwitchTreeWay.value = isDifferent(liveDataLayers.value!!)
            }
        }
    }

    fun expandLayer(idToUpdate: Int) {
        liveDataLayers.value?.let { layers ->
            val updatedLayers = mutableListOf<DataItem>()
            updatedLayers.addAll(layers)
            updatedLayers.forEachExceptHeader {
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
            val layerItem = list.findExceptHeader { it.id == layerId }
            val index = list.indexOf(layerItem as DataItem)
            (list[index] as DataItem.LayerItem).transparency = transparency
            repository.updateLayerTransparency(layerId, transparency)
        }
    }

    fun getCoordinates() = repository.polygonOptions


    fun onLayerBackgroundButtonClicked(viewId: Int,
                                       position: Int
    ) {
        when (viewId) {
            R.id.button_one -> liveDataBackgroundBtn.value = "Button one pressed on $position layer"
            R.id.button_two -> liveDataBackgroundBtn.value = "Button two pressed on $position layer"
        }
        liveDataBackgroundBtn.value = null
    }

    fun updateLayersOrderInDB() {
        val toUpdate = liveDataLayers.value?.filterIsInstance<DataItem.LayerItem>()
        repository.updateLayers(toUpdate)
    }

    fun onSearchTextChange(text: CharSequence
    ) {
        val searchQuery = "%$text%"
        repository.find(searchQuery)
    }

    fun updateIsSharedLayer(layer: DataItem.LayerItem
    ) {
        repository.updateIsSharedLayer(layer.id, layer.isSharedLayer)
    }

    fun addNewLayer(callback: (Int) -> Unit) {
        val toInsert = DataItem.LayerItem()
        repository.addLayer(DataItem.LayerItem()) {
            toInsert.id = it.toInt()
            val updatedLayers = mutableListOf<DataItem>()
            updatedLayers.addAll(liveDataLayers.value!!)
            liveDataLayers.value = (updatedLayers + toInsert).toMutableList()
            callback.invoke(liveDataLayers.value!!.indexOf(toInsert))
        }
    }

    private fun updateLiveDataSet(_layers: List<DataItem.LayerItem>,
                                  liveData: MutableLiveData<MutableList<DataItem>>,
                                  checkedStates: MutableMap<Int, Boolean>
    ) {
        val dataSet = mutableListOf<DataItem>()
        val headerPos = _layers.count { !it.isSharedLayer }
        dataSet.addAll(_layers)
        dataSet.add(headerPos, DataItem.Header(Int.MAX_VALUE))
        liveData.value = dataSet.toMutableList()
        checkedStates.clear()
        liveData.value?.forEachExceptHeader {
            checkedStates[it.id] = it.turnedOn
        }
    }

    private fun isDifferent(
        list: List<DataItem>
    ): Boolean {
        val isAllActive = list.allExceptHeader { it.turnedOn }
        val isAllInactive = list.noneExceptHeader { it.turnedOn }
        return !isAllActive && !isAllInactive
    }

    private fun defineMainSwitchPosition(
        layers: List<DataItem>
    ): Switch3Way.SwitchPositions {
        val isAllActive = layers.allExceptHeader { it.turnedOn }
        val isAllInactive = layers.noneExceptHeader { it.turnedOn }
        return when {
            isAllActive -> Switch3Way.SwitchPositions.END
            isAllInactive -> Switch3Way.SwitchPositions.START
            else -> Switch3Way.SwitchPositions.MIDDLE
        }
    }

}