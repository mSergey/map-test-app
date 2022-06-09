package com.gmail.zajcevserg.maptestapp.model.database


import androidx.room.*
import io.reactivex.*


@Dao
interface LayersDao {

    @Transaction
    fun updateCheckedColumn(flagsMap: Map<Int, Boolean>) {
        flagsMap.forEach {
            updateCheckedWithUnitResult(it.key, if (it.value) 1 else 0)
        }
    }


    @Insert
    fun insert(toSave: LayerItem): Completable

    @Query("UPDATE layers SET turned_on = :checked WHERE id is :id")
    fun updateChecked(id: Int, checked: Int): Completable

    @Query("UPDATE layers SET turned_on = :checked WHERE id is :id")
    fun updateCheckedWithUnitResult(id: Int, checked: Int)

    @Query("UPDATE layers SET transparency = :transparency WHERE id is :id")
    fun updateTransparency(id: Int, transparency: Int): Completable

    @Query("DELETE FROM layers WHERE id IN (:ids)")
    fun delete(ids: IntArray): Single<Int>

    @Update
    fun updateAllLayers(layers: List<LayerItem>)

    @Query("SELECT * FROM layers")
    fun getLayersSingle(): Single<MutableList<LayerItem>>

    @Query("UPDATE layers SET turned_on = :checked")
    fun updateCheckedStateAll(checked: Int): Completable


}