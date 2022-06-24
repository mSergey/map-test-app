package com.gmail.zajcevserg.maptestapp.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey


@Entity(tableName = "layer_objects")
data class LayerObject(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "object_id")
    var id: Int = 0,
    @ColumnInfo(name = "object_name")
    var objectName: String = "",
    @ColumnInfo(name = "icon_res_name")
    var iconResName: String = ""
)


sealed class DataItem {

    abstract var id: Int

    data class Header(
        override var id: Int,
        var syncDate: String = ""
    ) : DataItem()

    @Entity(tableName = "layers")
    data class LayerItem(
        @PrimaryKey(autoGenerate = true)
        override var id: Int = 0,
        var title: String = "Новый слой",
        @ColumnInfo(name = "layer_icon_res_name")
        var layerIconResName: String = "layer_icon_polygon",
        @ColumnInfo(name = "enabled")
        var enabled: Boolean = true,
        @ColumnInfo(name = "turned_on")
        var turnedOn: Boolean = false,
        var transparency: Int = 24,
        @ColumnInfo(name = "element_count")
        var numberOfViews: Int = 0,
        @ColumnInfo(name = "number_of_views")
        var elementCount: Int = 1,
        @ColumnInfo(name = "zoom_min")
        var zoomMin: Int = 10,
        @ColumnInfo(name = "zoom_max")
        var zoomMax: Int = 20,
        @ColumnInfo(name = "is_shared_layer")
        var isSharedLayer: Boolean = true,
        @Ignore
        var expanded: Boolean = false,
        @Ignore
        var selectedToRemove: Boolean = false
    ) : DataItem()
}