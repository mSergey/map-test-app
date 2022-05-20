package com.gmail.zajcevserg.maptestapp.ui.custom



import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import androidx.core.view.doOnLayout

import androidx.recyclerview.widget.ItemTouchHelper

import androidx.recyclerview.widget.RecyclerView
import com.gmail.zajcevserg.maptestapp.ui.activity.log

import com.gmail.zajcevserg.maptestapp.ui.adapter.LayersAdapter

import com.gmail.zajcevserg.maptestapp.viewmodel.LayersVM
import io.reactivex.subjects.PublishSubject
import kotlin.math.abs



@SuppressLint("ClickableViewAccessibility")
class SwipeCallback(
    private val mRecyclerView: RecyclerView,
    private val mViewModel: LayersVM,
    private val onItemMoveListener: OnItemMoveListener
) : ItemTouchHelper.SimpleCallback(0, 0) {

    private var threshold: Float = 0f
    private var mFrom = 0f
    private val subject: PublishSubject<Float> = PublishSubject.create()
    private var mSlop = 0
    private val swiped = mutableListOf<LayersAdapter.LayerItemViewHolder>()


    init {
        subject.distinctUntilChanged { previous, current ->
            if (current == 0f && previous != 0f) { mFrom = previous }
            true
        }.subscribe()
        mRecyclerView.doOnLayout { view ->
            view as RecyclerView
            threshold = convertDpToPixel(152)
            val vc: ViewConfiguration = ViewConfiguration.get(view.context)
            mSlop = vc.scaledTouchSlop
            //val mMinFlingVelocity: Int = vc.scaledMinimumFlingVelocity
            //val mMaxFlingVelocity: Int = vc.scaledMaximumFlingVelocity
            view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING)
                        swiped.forEach {
                            swipeClose(it, -threshold)
                        }
                }
            })
        }
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return mViewModel.liveDataDragMode.value!!.not()
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        viewHolder as LayersAdapter.LayerItemViewHolder
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                if (!viewHolder.isLayerEnabled()) return
                subject.onNext(dX)
                val viewToAnimate = viewHolder.binding.motionLayer
                if (isCurrentlyActive) {
                    viewToAnimate.translationX = dX
                    if (dX == 0f) {
                        swiped.forEach {
                            swipeClose(it, -threshold)
                        }
                    }
                } else if (dX == 0f) {
                    swipeOpen(viewHolder, mFrom)
                }
            }
            else -> super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        }
    }



    private fun swipeOpen(viewHolder: LayersAdapter.LayerItemViewHolder, from: Float) {
        val viewToAnimate = viewHolder.binding.motionLayer
        val mDuration = 120 * abs(threshold - from) / threshold

        ObjectAnimator
            .ofFloat(viewToAnimate, "translationX", from, -threshold)
            .apply {
                interpolator = DecelerateInterpolator()
                duration = mDuration.toLong()
                setEvaluator { completePart, startPos, endPos ->
                    startPos as Float; endPos as Float
                    val translationX = startPos + (endPos - startPos) * completePart

                    viewHolder.binding.backgroundButtonsLayer.scaleX = completePart
                    viewHolder.binding.backgroundButtonsLayer.scaleY = completePart


                    translationX
                }
                addListener(object : AnimatorListenerAdapter() {
                    var initialTouchX = 0f
                    var initialTouchY = 0f

                    override fun onAnimationEnd(animation: Animator?) {
                        swiped.add(viewHolder)
                        val buttonOne = viewHolder.binding.buttonOne
                        val buttonTwo = viewHolder.binding.buttonTwo
                        buttonOne.setOnClickListener {
                            mViewModel.onLayerBackgroundButtonClicked(it.id, viewHolder.adapterPosition)
                            swipeClose(viewHolder, -threshold)
                        }
                        buttonTwo.setOnClickListener {
                            mViewModel.onLayerBackgroundButtonClicked(it.id, viewHolder.adapterPosition)
                            swipeClose(viewHolder, -threshold)
                        }
                        viewToAnimate.setOnTouchListener { view, motionEvent ->
                            val deltaX = motionEvent.x - initialTouchX
                            val deltaY = motionEvent.y - initialTouchY
                            view.parent.parent.requestDisallowInterceptTouchEvent(true)

                            val buttonOneRect = Rect().apply {
                                bottom = buttonOne.bottom
                                top = buttonOne.top
                                left = buttonOne.left
                                right = buttonOne.right
                            }

                            val buttonTwoRect = Rect().apply {
                                bottom = buttonTwo.bottom
                                top = buttonTwo.top
                                left = buttonTwo.left
                                right = buttonTwo.right
                            }

                            when (motionEvent.actionMasked) {
                                MotionEvent.ACTION_DOWN -> {
                                    initialTouchX = motionEvent.x
                                    initialTouchY = motionEvent.y

                                    !buttonOneRect.contains(initialTouchX.toInt(), initialTouchY.toInt())
                                            && !buttonTwoRect.contains(initialTouchX.toInt(), initialTouchY.toInt())
                                }

                                MotionEvent.ACTION_MOVE -> {
                                    when {

                                        abs(deltaX) > mSlop -> {
                                            view.translationX = (-threshold + deltaX)
                                                .coerceAtMost(0f)
                                                .coerceAtLeast(-threshold)
                                            true
                                        }
                                        abs(deltaY) > mSlop -> {
                                            view.parent.parent.requestDisallowInterceptTouchEvent(false)
                                            false
                                        }
                                        else -> {
                                            false
                                        }
                                    }
                                }

                                MotionEvent.ACTION_UP -> {
                                    if (deltaX > 0)
                                    swipeClose(viewHolder, (-threshold + deltaX))
                                    false
                                }
                                else -> false
                            }
                        }
                    }
                })
                start()
            }
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val isDragMode = mViewModel.liveDataDragMode.value!!
        return if (isDragMode) {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = 0
            makeMovementFlags(dragFlags, swipeFlags)
        } else {
            val dragFlags = 0
            val swipeFlags = ItemTouchHelper.START
            makeMovementFlags(dragFlags, swipeFlags)
        }
    }

    override fun getAnimationDuration(
        recyclerView: RecyclerView,
        animationType: Int,
        animateDx: Float,
        animateDy: Float
    ): Long {
        return if (animationType == ItemTouchHelper.ANIMATION_TYPE_SWIPE_CANCEL) 0 else
            super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy)
    }


    private fun swipeClose(viewHolder: LayersAdapter.LayerItemViewHolder, from: Float) {
        val mDuration = 120 * abs(threshold - from) / threshold
        val viewToAnimate = viewHolder.binding.motionLayer
        swiped.remove(viewHolder)
        ObjectAnimator
            .ofFloat(
                viewToAnimate,
                "translationX",
                from.coerceAtMost(0f)
                    .coerceAtLeast(-threshold),
                0f
            )
            .apply {
                interpolator = DecelerateInterpolator()
                duration = mDuration.toLong()
                setEvaluator { completePart, startPos, endPos ->
                    startPos as Float; endPos as Float
                    val translationX = startPos + (endPos - startPos) * completePart

                    viewHolder.binding.backgroundButtonsLayer.scaleX = 1 - completePart
                    viewHolder.binding.backgroundButtonsLayer.scaleY = 1 - completePart


                    translationX
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        viewToAnimate.setOnTouchListener { view, motionEvent ->
                            view.parent.parent.requestDisallowInterceptTouchEvent(false)
                            false
                        }
                    }
                })
                start()
            }
    }
    private fun convertDpToPixel(dp: Int): Float {
        return run {
            val resources = mRecyclerView.resources
            val metrics = resources.displayMetrics
            dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = Float.MAX_VALUE
    override fun getSwipeEscapeVelocity(defaultValue: Float) = Float.MAX_VALUE
    override fun getSwipeVelocityThreshold(defaultValue: Float) = Float.MAX_VALUE
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {}
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return onItemMoveListener.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
    }

}