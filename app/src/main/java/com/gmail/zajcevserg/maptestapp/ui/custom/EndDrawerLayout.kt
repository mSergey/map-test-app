package com.gmail.zajcevserg.maptestapp.ui.custom

import android.content.Context
import android.util.AttributeSet

import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class EndDrawerLayout : DrawerLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    override fun open() = openDrawer(GravityCompat.END)

    override fun close() = closeDrawer(GravityCompat.END)

}
