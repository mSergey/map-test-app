package com.gmail.zajcevserg.maptestapp.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.PolygonOptions


@Entity(tableName = "layers")
data class LayerItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,
    var title: String = "",
    @ColumnInfo(name = "main_icon_res_name")
    var mainIconResName: String = "",
    var expanded: Boolean = false,
    @ColumnInfo(name = "active_on_list")
    var activeOnList: Boolean = true,
    @ColumnInfo(name = "visible_on_map")
    var visibleOnMap: Boolean = false,
    var transparency: Int = 0,
    @ColumnInfo(name = "sync_date")
    var syncDate: String = "",
    @ColumnInfo(name = "element_count")
    var elementCount: Int = 0,
    @ColumnInfo(name = "zoom_min")
    var zoomMin: Int = 0,
    @ColumnInfo(name = "zoom_max")
    var zoomMax: Int = 0,
    @ColumnInfo(name = "group_feature")
    var groupFeature: String = ""

)