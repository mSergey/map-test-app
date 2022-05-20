package com.gmail.zajcevserg.maptestapp.model.repository

import android.annotation.SuppressLint
import android.graphics.Color
import com.gmail.zajcevserg.maptestapp.model.application.App
import com.gmail.zajcevserg.maptestapp.model.database.LayerItem

import com.gmail.zajcevserg.maptestapp.model.database.LayersDao
import com.gmail.zajcevserg.maptestapp.ui.activity.log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import io.reactivex.Observer
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.internal.observers.SubscriberCompletableObserver
import io.reactivex.schedulers.Schedulers
import java.util.*


class Repository(private val dao: LayersDao = App.database.getDao()) {

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

    @SuppressLint("CheckResult")
    fun requestLayers(callback: (MutableList<LayerItem>) -> Unit) {
        dao.getLayersSingle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                callback.invoke(it)
            })
    }


    fun updateLayer(id: Int, checked: Boolean) {
        dao.updateChecked(id, if (checked) 1 else 0)
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun updateLayer(id: Int, transparency: Int) {
        dao.updateTransparency(id, transparency)
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun deleteLayers(list: List<Int>) {
        dao.delete(list.toIntArray())
            .subscribeOn(Schedulers.io())
            .subscribe(object : SingleObserver<Int> {
                override fun onSubscribe(d: Disposable) {
                    log("onSubscribe")
                }

                override fun onSuccess(t: Int) {
                    log("onSuccess ${t}")
                }

                override fun onError(e: Throwable) {
                    log("onSuccess $e")
                }

            })
    }

}
