package com.gmail.zajcevserg.maptestapp.model.database


import android.annotation.SuppressLint
import androidx.room.*
import com.gmail.zajcevserg.maptestapp.ui.activity.log
import io.reactivex.*
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscription


@Dao
interface LayersDao {

    @Transaction
    fun updateCheckedColumn(flagsMap: Map<Int, Int>) {
        flagsMap.forEach {
            updateChecked(it.key, it.value)
        }
    }

    @Insert
    fun insert(toSave: LayerItem): Completable

    @Query("UPDATE layers SET turned_on = :checked WHERE id is :id")
    fun updateChecked(id: Int, checked: Int): Completable


    @Query("UPDATE layers SET transparency = :transparency WHERE id is :id")
    fun updateTransparency(id: Int, transparency: Int): Completable

    @Query("DELETE FROM layers WHERE id IN (:ids)")
    fun delete(ids: IntArray): Single<Int>

    /*@Update
    fun updateAllLayers(layers: List<LayerItem>): Completable*/

    @Update
    fun updateAllLayers(layers: List<LayerItem>)

    /*@Query("SELECT * FROM layers")
    fun getLayersFlowable(): Flowable<List<LayerItem>>*/

    @Query("SELECT * FROM layers")
    fun getLayersSingle(): Single<MutableList<LayerItem>>

    @Query("UPDATE layers SET turned_on = :checked")
    fun updateCheckedStateAll(checked: Int): Completable


}