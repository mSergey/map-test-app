package com.gmail.zajcevserg.maptestapp.model.repository

import android.annotation.SuppressLint
import android.graphics.Color
import com.gmail.zajcevserg.maptestapp.model.application.App
import com.gmail.zajcevserg.maptestapp.model.database.LayerItem

import com.gmail.zajcevserg.maptestapp.model.database.LayersDao
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers


class Repository(private val dao: LayersDao = App.database.getDao()) {

    //private val layersCache: MutableList<LayerItem>? = null

    //private lateinit var disposable: Disposable
    val polygonOptions = PolygonOptions()
        .add(LatLng(55.82344398214789, 37.71777048265712))
        .add(LatLng(55.823955375520974, 37.7159497389675))
        .add(LatLng(55.820750532781155, 37.71206548576296))
        .add(LatLng(55.82017090537237, 37.70878814712162))
        .add(LatLng(55.82013680937355, 37.70514665974237))
        .add(LatLng(55.82211432791708, 37.70502527682973))
        .add(LatLng(55.81580637466369, 37.68530055352542))
        .add(LatLng(55.81369212910806, 37.68712129721505))
        .add(LatLng(55.81386263703993, 37.687242680127696))
        .add(LatLng(55.811748285884114, 37.685543319350714))
        .add(LatLng(55.81150956160167, 37.68936688109893))
        .add(LatLng(55.809906660641744, 37.691491082070165))
        .add(LatLng(55.80878118009703, 37.68827443488515))
        .add(LatLng(55.805711522296825, 37.691976613720726))
        .add(LatLng(55.79318471844065, 37.679884754090104))
        .add(LatLng(55.791495846421704, 37.66580852275441))
        .add(LatLng(55.79269075873887, 37.658602790982265))
        .add(LatLng(55.797248010426976, 37.65710406615229))
        .add(LatLng(55.797707535620425, 37.651381662256036))
        .add(LatLng(55.819987999108314, 37.667663263818))
        .add(LatLng(55.83873046301243, 37.66978800508339))
        .add(LatLng(55.8239711445992, 37.7182197872275))
        .strokeColor(Color.RED)
        .fillColor(Color.RED)



    /*@SuppressLint("CheckResult")
    fun observeLayers(callback: (List<LayerItem>) -> Unit): Disposable {
        disposable = dao.getLayersFlowable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                callback.invoke(it)
            }
        return disposable
    }*/

    @SuppressLint("CheckResult")
    fun requestLayers(callback: (List<LayerItem>) -> Unit) {
        dao.getLayersFlowable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                callback.invoke(it)
            }
    }

    fun save(layers: List<LayerItem>) {
        dao.updateAllLayers(layers)
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun saveLayer(toSave: LayerItem) {
        dao.insert(toSave)
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun deleteLayer(toDelete: LayerItem) {
        dao.delete(toDelete)
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    /*fun updateAllLayers(layers: List<LayerItem>) {
        layersCache?.let {
            it.clear()
            it.addAll(layers)
        }
    }

    fun updateLayer(index: Int, toUpdate: LayerItem) {
        layersCache?.let {
            it.removeAt(index)
            it.add(index, toUpdate)
        }
    }*/

    /*fun stopObserve() {
        disposable.dispose()
    }*/

    /*fun updateActiveStateAll(active: Boolean) {
        dao.updateActiveStateAll(if (active) 1 else 0)
            .subscribeOn(Schedulers.io())
            .subscribe()
    }*/



}
