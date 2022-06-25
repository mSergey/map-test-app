package com.gmail.zajcevserg.maptestapp.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout

import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

import com.gmail.zajcevserg.maptestapp.R


private const val ENTER_ANIMATION_DURATION = 500L
private const val EXIT_ANIMATION_DURATION = 500L

class SearchBarHideOnScrollBehavior(context: Context,
                                    attributeSet: AttributeSet
) : CoordinatorLayout.Behavior<ViewGroup>(context, attributeSet) {

    private val hideSearchFieldSubject: PublishSubject<Int> = PublishSubject.create()
    private var searchLayout: ConstraintLayout? = null

    var isSearchMode = false
    set(value) {
        if (field == value) return
        if (value) {
            searchLayout?.let { showAnim(it) }
        } else {
            searchLayout?.let { hideAnim(it) }
        }
        field = value
    }

    val searchBarHideDisposable: Disposable =
        hideSearchFieldSubject
            .distinctUntilChanged { previous, current ->
                previous < 0 && current < 0
                        || previous > 0 && current > 0
                        || previous != 0 && current == 0
                        || previous == current

            }.subscribe {
                if (it > 0) showAnim(searchLayout)
                else hideAnim(searchLayout)
            }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: ViewGroup,
        layoutDirection: Int
    ): Boolean {
        searchLayout = child as? ConstraintLayout
        searchLayout?.doOnLayout {
            if (isSearchMode) show(it) else hide(it)
        }
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    private fun hide(view: View?) {
        val params = view?.layoutParams as CoordinatorLayout.LayoutParams
        val mY = -(view.height + params.bottomMargin).toFloat()
        view.translationY = mY
    }

    private fun show(view: View?) {
        view?.translationY = 0f
    }

    private fun hideAnim(view: View?) {
        val params = view?.layoutParams as CoordinatorLayout.LayoutParams
        view.animate().apply {
            translationY(-(view.height + params.bottomMargin).toFloat())
            duration = EXIT_ANIMATION_DURATION
            interpolator = AccelerateInterpolator()
            start()
        }
    }

    private fun showAnim(view: View?) {
        view?.animate()?.apply {
            translationY(0f)
            duration = ENTER_ANIMATION_DURATION
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: ViewGroup,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        super.onNestedScroll(
            coordinatorLayout,
            child,
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type,
            consumed
        )
        hideSearchFieldSubject.onNext(if (dyUnconsumed == 0) dyConsumed else dyUnconsumed)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: ViewGroup,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
                && isSearchMode
                && target.id == R.id.layers_recycler_view
    }

}