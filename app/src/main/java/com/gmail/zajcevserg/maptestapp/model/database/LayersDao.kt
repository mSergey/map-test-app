package com.gmail.zajcevserg.maptestapp.model.database


import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
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

    @Query("SELECT * FROM layers")
    fun getLayersFlowable(): Flowable<List<LayerItem>>

    @Query("SELECT * FROM layers")
    fun getLayersSingle(): Single<List<LayerItem>>

    @Query("UPDATE layers SET visible_on_map = :active")
    fun updateActiveStateAll(active: Int): Completable


}