package com.gmail.zajcevserg.maptestapp.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.PolygonOptions


@Entity(tableName = "layers")
data class LayerItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,
    var title: String = "",
    @ColumnInfo(name = "layer_icon_res_name")
    var layerIconResName: String = "",
    @ColumnInfo(name = "enabled")
    var enabled: Boolean = true,
    @ColumnInfo(name = "turned_on")
    var turnedOn: Boolean = false,
    var transparency: Int = 0,
    @ColumnInfo(name = "sync_date")
    var syncDate: String = "",
    @ColumnInfo(name = "element_count")
    var elementCount: Int = 0,
    @ColumnInfo(name = "zoom_min")
    var zoomMin: Int = 0,
    @ColumnInfo(name = "zoom_max")
    var zoomMax: Int = 0,
    @ColumnInfo(name = "is_shared_layer")
    var isSharedLayer: Boolean = false,
    @Ignore
    var expanded: Boolean = false,
    @Ignore
    var selectedToRemove: Boolean = false

)