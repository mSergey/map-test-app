package com.gmail.zajcevserg.maptestapp.model.database


import androidx.room.Dao
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Insert
import androidx.room.Query

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single


@Dao
interface LayersDao {

    @Transaction
    fun updateCheckedColumn(flagsMap: Map<Int, Boolean>) {
        flagsMap.forEach {
            updateCheckedWithUnitResult(it.key, if (it.value) 1 else 0)
        }
    }

    @Query("SELECT * FROM layer_objects WHERE object_name LIKE :query")
    fun find(query: String): List<LayerObject>

    @Insert
    fun insert(toSave: DataItem.LayerItem): Completable

    @Query("UPDATE layers SET turned_on = :checked WHERE id is :id")
    fun updateChecked(id: Int, checked: Int): Completable

    @Query("UPDATE layers SET turned_on = :checked WHERE id is :id")
    fun updateCheckedWithUnitResult(id: Int, checked: Int)

    @Query("UPDATE layers SET transparency = :transparency WHERE id is :id")
    fun updateTransparency(id: Int, transparency: Int): Completable

    @Query("UPDATE layers SET is_shared_layer = :isShared WHERE id is :id")
    fun updateIsShared(id: Int, isShared: Int): Completable

    @Query("DELETE FROM layers WHERE id IN (:ids)")
    fun delete(ids: IntArray): Single<Int>

    @Update
    fun updateAllLayers(layers: List<DataItem.LayerItem>)

    @Query("SELECT * FROM layers")
    fun getLayersSingle(): Single<MutableList<DataItem.LayerItem>>

    @Query("SELECT * FROM layers")
    fun getLayersFlowable(): Flowable<MutableList<DataItem.LayerItem>>

    @Insert
    fun addLayer(item: DataItem.LayerItem): Single<Long>

    @Query("UPDATE layers SET turned_on = :checked")
    fun updateCheckedStateAll(checked: Int): Completable

}