package com.gmail.zajcevserg.maptestapp.model.database


import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single


@Dao
interface LayersDao {

    @Insert
    fun insert(toSave: LayerItem): Completable

    @Delete
    fun delete(toSave: LayerItem): Completable

    @Update
    fun update(saveSize: LayerItem): Completable

    @Update
    fun updateAllLayers(layers: List<LayerItem>): Completable

    /*@Query("SELECT * FROM layers")
    fun getLayersFlowable(): Flowable<List<LayerItem>>*/

    @Query("SELECT * FROM layers")
    fun getLayersSingle(): Single<MutableList<LayerItem>>

    @Query("UPDATE layers SET turned_on = :active")
    fun updateActiveStateAll(active: Int): Completable


}