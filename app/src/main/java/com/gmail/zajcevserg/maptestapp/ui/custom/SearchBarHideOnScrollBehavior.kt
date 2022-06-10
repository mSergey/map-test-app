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
import com.gmail.zajcevserg.maptestapp.ui.activity.log
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

private const val ENTER_ANIMATION_DURATION = 500L
private const val EXIT_ANIMATION_DURATION = 500L

class SearchBarHideOnScrollBehavior(context: Context,
                                    attributeSet: AttributeSet
) : CoordinatorLayout.Behavior<ViewGroup>(context, attributeSet) {

    private val hideSearchFieldSubject: PublishSubject<Pair<View, Int>> = PublishSubject.create()
    private lateinit var searchLayout: ConstraintLayout

    var isSearchMode = false
    set(value) {
        if (field == value) return

        field = value
    }


    val searchBarHideDisposable: Disposable =
        hideSearchFieldSubject
            .distinctUntilChanged { previous, current ->
                val previousConsume = previous.second
                val currentConsume = current.second
                previousConsume < 0 && currentConsume < 0
                        || previousConsume > 0 && currentConsume > 0
                        || previousConsume != 0 && currentConsume == 0
                        || previousConsume == currentConsume

            }.subscribe {
                log ("consume it $it")
                if (it.second > 0) showAnim(it.first)
                else hideAnim(it.first)
            }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: ViewGroup,
        layoutDirection: Int
    ): Boolean {
        if (isSearchMode) show(child) else hide(child)
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    private fun hide(view: View) {
        val params = view.layoutParams as CoordinatorLayout.LayoutParams
        val mY = -(view.height + params.bottomMargin).toFloat()
        view.translationY = mY
    }

    private fun show(view: View) {
        view.translationY = 0f
    }

    private fun hideAnim(view: View) {
        val params = view.layoutParams as CoordinatorLayout.LayoutParams
        view.animate().apply {
            translationY(-(view.height + params.bottomMargin).toFloat())
            duration = EXIT_ANIMATION_DURATION
            interpolator = AccelerateInterpolator()
            start()
        }
    }

    private fun showAnim(view: View) {
        view.animate().apply {
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
        hideSearchFieldSubject.onNext(child to dyConsumed)
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
    }


}