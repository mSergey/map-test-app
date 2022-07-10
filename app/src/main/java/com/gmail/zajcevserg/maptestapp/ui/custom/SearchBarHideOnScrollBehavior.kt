package com.gmail.zajcevserg.maptestapp.ui.custom

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.RecyclerView
import com.gmail.zajcevserg.maptestapp.model.application.log

import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject


private const val ENTER_ANIMATION_DURATION = 500L
private const val EXIT_ANIMATION_DURATION = 500L

class SearchBarHideOnScrollBehavior(context: Context,
                                    attributeSet: AttributeSet
) : CoordinatorLayout.Behavior<ViewGroup>(context, attributeSet) {

    private val mHideSearchFieldSubject: PublishSubject<Int> = PublishSubject.create()
    private var mSearchLayout: ConstraintLayout? = null
    private var mRecyclerView: RecyclerView? = null

    var isSearchMode = false
    set(value) {
        if (field == value) return
        if (value) {
            mSearchLayout?.let { showAnim(it) }
        } else {
            mSearchLayout?.let { hideAnim(it) }
        }
        field = value
    }

    val searchBarHideDisposable: Disposable =
        mHideSearchFieldSubject
            .distinctUntilChanged { previous, current ->
                previous < 0 && current < 0
                        || previous > 0 && current > 0
                        || previous != 0 && current == 0
                        || previous == current

            }.subscribe {
                if (it > 0) showAnim(mSearchLayout)
                else hideAnim(mSearchLayout)
            }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: ViewGroup,
        layoutDirection: Int
    ): Boolean {

        mSearchLayout = child as? ConstraintLayout
        mSearchLayout?.doOnLayout {
            if (isSearchMode) show(it) else hide(it)
        }

        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: ViewGroup,
        dependency: View
    ): Boolean {
        if (mRecyclerView == null) mRecyclerView = dependency as? RecyclerView
        return super.layoutDependsOn(parent, child, dependency)
    }

    private fun hide(view: View?) {
        val params = view?.layoutParams as CoordinatorLayout.LayoutParams
        val mY = -(view.height + params.bottomMargin).toFloat()
        view.translationY = mY
        mRecyclerView?.setPadding(0, 0, 0, 0)
    }

    private fun show(view: View?) {
        view?.translationY = 0f
        mRecyclerView?.setPadding(0, mSearchLayout!!.height, 0, 0)

    }

    private fun hideAnim(view: View?) {
        val params = view?.layoutParams as CoordinatorLayout.LayoutParams
        view.animate().apply {
            translationY(-(view.height + params.bottomMargin).toFloat())
            duration = EXIT_ANIMATION_DURATION
            interpolator = AccelerateInterpolator()
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    mRecyclerView?.setPadding(0, 0, 0, 0)
                }
            })
            start()
        }
    }

    private fun showAnim(view: View?) {
        view?.animate()?.apply {
            translationY(0f)
            duration = ENTER_ANIMATION_DURATION
            interpolator = DecelerateInterpolator()
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    mRecyclerView?.setPadding(0, mSearchLayout!!.height, 0, 0)
                }
            })
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
        mHideSearchFieldSubject.onNext(if (dyUnconsumed == 0) dyConsumed else dyUnconsumed)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: ViewGroup,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return false
    }



}