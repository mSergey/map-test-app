package com.gmail.zajcevserg.maptestapp.ui.custom

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.gmail.zajcevserg.maptestapp.model.database.LayerItem


class HeaderItemDecorator(
    layers: List<LayerItem>, val context: Context,
) : RecyclerView.ItemDecoration() {

    /*private val headersSchema =
        layers.foldIndexed(mutableMapOf<String, Int>()) { index, acc, layer ->
            if (!acc.keys.contains(layer.isSharedLayer)) {
                acc[layer.isSharedLayer] = index
            }
            acc
        }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.LTGRAY
        textSize = convertSpToPixel(12)
    }

    private val linePaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = convertDpToPixel(1)
    }

    private val rectPaint = Paint().apply {
        color = ResourcesCompat.getColor(context
            .resources, R.color.surface_color, context.theme)

    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {

        val position = parent.getChildAdapterPosition(view).let {
            if (it == RecyclerView.NO_POSITION) return
            else it
        }
        headersSchema.forEach {
            if (it.value == position && it.key.isNotEmpty()) {
                outRect.top = convertDpToPixel(24).toInt()
            }
        }

    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {


        parent.children.forEach { view ->
            val position = parent.getChildAdapterPosition(view).let {
                if (it == RecyclerView.NO_POSITION) return
                else it
            }

            headersSchema.forEach {
                if (it.value == position && it.key.isNotEmpty()) {
                    val start = 0f
                    val end = view.right.toFloat()
                    val topAndBottom = view.top.toFloat() - convertDpToPixel(24)

                    c.drawRect(start, topAndBottom, end, topAndBottom + convertDpToPixel(24).toInt(), rectPaint)
                    c.drawText(it.key, convertDpToPixel(16), view.top.toFloat(), textPaint)
                    c.drawLine(start, topAndBottom, end, topAndBottom, linePaint)

                }
            }
        }
    }

    private fun convertDpToPixel(dp: Int): Float {
        return run {
            val resources = context.resources
            val metrics = resources.displayMetrics
            dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }

    private fun convertSpToPixel(sp: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), context.resources.displayMetrics)
    }*/

}