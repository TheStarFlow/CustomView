package com.zzs.customview.layoutmanager

import android.graphics.Rect
import android.util.Log
import android.util.SparseArray
import android.view.View
import androidx.core.view.*
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.max

/**
@author  zzs
@Date 2022/3/1
@describe  自定义 layoutManager
 */
class GreatLayoutManager : RecyclerView.LayoutManager() {

    private var itemWidth = 0
    private var itemHeight = 0
    private val mItemRect by lazy { SparseArray<Rect>() }
    private var mTotalWidth = 0

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    /**
     * 首先按照需求布局满一屏,然后看情况能不能把后续需要的东西先计算出来
     * */
    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        recycler ?: return
        if (itemCount == 0) {
            detachAndScrapAttachedViews(recycler)  //离屏缓存函数 将当前屏幕上的 view 全部剥离
            return
        }
        mTotalWidth = 0
        detachAndScrapAttachedViews(recycler)
        var itemStartX = paddingStart
        var itemStartY = paddingTop
        val firstChild = recycler.getViewForPosition(0)
        measureChildWithMargins(firstChild, 0, 0) //测量子View
        itemWidth =
            getDecoratedMeasuredWidth(firstChild) + firstChild.marginStart + firstChild.marginEnd
        itemHeight =
            getDecoratedMeasuredHeight(firstChild) + firstChild.marginTop + firstChild.marginBottom
        var visibleCount = (width-paddingStart-paddingRight) / itemWidth
        if (width%itemWidth>0){
            visibleCount++
        }
        for (index in 0 until itemCount) {
            mItemRect.put(
                index,
                Rect(itemStartX, itemStartY, itemStartX + itemWidth, itemStartY + itemHeight)
            )
            itemStartX += itemWidth
            mTotalWidth += itemWidth
        }
        for (index in 0 until visibleCount) {
            val indexChild = recycler.getViewForPosition(index)
            val rect = mItemRect.get(index)
            addView(indexChild)
            measureChildWithMargins(indexChild, 0, 0)
            layoutDecorated(
                indexChild, rect.left + indexChild.marginStart, rect.top + indexChild.marginTop,
                rect.right - indexChild.marginEnd, rect.bottom - indexChild.marginBottom
            )//布局子View
        }
        mTotalWidth = mTotalWidth.coerceAtLeast(getParentWidth())
    }

    override fun canScrollHorizontally(): Boolean {
        return true
    }

    private fun getParentWidth(): Int {
        return width - paddingStart - paddingEnd
    }

    /**
     * dx < 0  手指从左向右动
     * dx > 0 手指从右向左动
     * */

    private var mScrollX = 0
    private val mJudgeRect by lazy { Rect() }
    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        recycler ?: return dx
        if (itemCount <= 0) return dx
        if (childCount<=0) return dx
        var scrollSpace = dx
        if (mScrollX + scrollSpace < 0) {
            scrollSpace = -mScrollX
        } else if (mScrollX + scrollSpace > mTotalWidth - getParentWidth()) {
            scrollSpace = mTotalWidth - getParentWidth() - mScrollX
        }
        val viewVisibleRect = getVisibleRect(scrollSpace)
        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            val position = getPosition(child)
            val childRect = mItemRect.get(position)
            if (!Rect.intersects(viewVisibleRect, childRect)) {
                removeAndRecycleView(child, recycler)
            }
        }
        mScrollX += scrollSpace

        /**
         * 步骤1 把子view add 进来
         * 步骤2 把子view 测量
         * 步骤3 把子View摆好位置
         *
         * */
        val firstView = getChildAt(0) ?: return dx
        val lastView = getChildAt(childCount - 1) ?: return dx
        val firstViewItemPosition = getPosition(firstView)
        val lastViewItemPosition = getPosition(lastView)
        detachAndScrapAttachedViews(recycler)//打算重新布局view 先离屏缓存
        if (scrollSpace > 0) {//item从右向左滑动
            var itemPosition = firstViewItemPosition
            while (itemPosition < itemCount) {
                val childRect = mItemRect.get(itemPosition)
                if (Rect.intersects(childRect, viewVisibleRect)) {
                    val insertChild = recycler.getViewForPosition(itemPosition)
                    addView(insertChild)
                    measureChildWithMargins(insertChild, 0, 0)
                    layoutDecorated(
                        insertChild,
                        childRect.left + insertChild.marginLeft-mScrollX,
                        childRect.top + insertChild.marginTop,
                        childRect.right - insertChild.marginRight-mScrollX,
                        childRect.bottom - insertChild.marginBottom
                    )
                    handleChildAnimation(insertChild,childRect,viewVisibleRect)
                }
                itemPosition++
            }

        } else {
            var itemPosition = lastViewItemPosition
            while (itemPosition >= 0 ) {
                val childRect = mItemRect.get(itemPosition)
                if (Rect.intersects(childRect, viewVisibleRect)) {
                    val insertChild = recycler.getViewForPosition(itemPosition)
                    addView(insertChild,0)
                    measureChildWithMargins(insertChild, 0, 0)
                    layoutDecorated(
                        insertChild,
                        childRect.left + insertChild.marginLeft-mScrollX,
                        childRect.top + insertChild.marginTop,
                        childRect.right - insertChild.marginRight-mScrollX,
                        childRect.bottom - insertChild.marginBottom
                    )
                    handleChildAnimation(insertChild, childRect, viewVisibleRect)
                }
                itemPosition--
            }
        }
        return scrollSpace
    }

    private fun handleChildAnimation(insertChild: View, childRect: Rect, viewVisibleRect: Rect) {
        var space = childRect.centerX() -viewVisibleRect.centerX()
        val factor = (space*1f)/((width-paddingLeft-paddingRight)/2f)
        val degrees = factor*45f
        insertChild.rotationY = degrees
        if (space>0){
            val  scale = 1.2f - factor*0.35f
            insertChild.scaleX = scale
            insertChild.scaleY = scale
        }else{
            val  scale = 1.2f + factor*0.35f
            insertChild.scaleX = scale
            insertChild.scaleY = scale
        }
        Log.i("LayoutManager", "rotate_degrees ${degrees}  factor = $factor  space = ${space}")

    }

    //把控件view的可见区域看做是一个矩形可视窗口  在排列子view的一长串矩形上滑动
    private fun getVisibleRect(scroll: Int): Rect {
        return Rect(
            paddingLeft + mScrollX + scroll,
            paddingTop,
            width+mScrollX - paddingRight + scroll,
            height - paddingBottom
        )
    }

    override fun isAutoMeasureEnabled(): Boolean {
        return true
    }
}