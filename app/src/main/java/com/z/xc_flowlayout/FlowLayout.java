package com.z.xc_flowlayout;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/*
 * ViewGroup主要是用来布局用的
 *
 * View的三大流程
 *      onMeasure
 *      onLayout
 *      onDraw
 *
 * ViewGroup: 主要是用来布局
 *      onMesurea
 *      onLayout
 *      onDraw(背景色，不常用，子view绘制完成，则viewGroup绘制完成)
 *
 * View: 主要是用来绘制
 *      onMesure
 *      onLayout(不常用，子view一般不会包含其它的view)
 *      onDraw
 *
 * 高级UI
 *      布局 + 绘制 + touch事件
 *      View(绘制 + touch事件)
 *      ViewGroup(布局 + touch事件)
 *
 * View(绘制 + touch事件)
 *      canvas
 *      paint
 *      path
 *      crop
 *      animation
 *      bezier
 *      matrix
 *      touch事件的分发，写到ViewGroup里面
 *
 * ViewGroup(布局 + touch事件)
 *
 * margin
 *      LinearLayout#generateLayoutParams
 *
 * Adapter设计模式
 *      动态添加子View
 */
public class FlowLayout extends ViewGroup {

    private static final String TAG = "FlowLayout";

    // 每个itemView横向间距
    private int mHorizontalSpacing = dp2px(16);

    // 每个itemView纵向间距
    private int mVerticalSpacing = dp2px(24);

    // 所有行, 每一行保存的view, 用于layout
    private List<List<View>> allLines;

    // 所有行的高度, 用于layout
    private List<Integer> lineHeights;

    /*
        代码中new
     */
    public FlowLayout(Context context) {
        super(context);
    }

    /*
        xml -> view
            反射
     */
    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*
        主题
            style
     */
    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    /*
        onMeasure中用的变量，都不能在构造函数中初始化
            onMeasure可能会调用多次
            onLayout可能会调用多次
     */
    private void initMeasureParams() {
        // 所有行, 每一行保存的view, 用于layout
        allLines = new ArrayList<>();
        // 所有行的高度, 用于layout
        lineHeights = new ArrayList<>();

//        if (allLines == null) {
//            allLines = new ArrayList<>();
//        } else {
//            allLines.clear();
//        }
    }


    /*
     * 度量
     *      自定义 ViewGroup过程中，需要在 onMeasure里面对子 View进行测量
     *
     *      match_parent: 宽度由父亲决定
     *      wrap_content: 宽度由内容决定
     *
     * 由上往下进行度量，DecorView -> ViewGroup -> View
     *      view参考父布局传入的尺寸，并测量子View的尺寸
     *
     * MeasureSpec
     *      int, 4 byte == 32 bit
     *
     *      Mode, 高位, 2bit, 三种状态(00, 01, 11)
     *          EXACTLY
     *              16dp
     *              match_parent
     *          AT_MOST
     *              wrap_content
     *          UNSPECIFIED
     *
     *      Size, 低位, 30bit
     *
     *      ##000000 00000000 00000000 00000000
     *
     * 为什么大家都说 MeasureSpec.UNSPECIFIED不常见呢?
     *      在编写布局时, View的宽高一般只会选择
     *          match_parent,
     *          wrap_content
     *          或者指定一个精确的尺寸
     *          或者指定一个权重
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        initMeasureParams();

        // parent, padding
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        // parent, width, height
        int selfWidth = MeasureSpec.getSize(widthMeasureSpec);
        int selfHeight = MeasureSpec.getSize(heightMeasureSpec);

        // parent, needWidth, needHeight, wrap_content的情况需要手动累加
        int parentNeededWidth = getPaddingLeft() + getPaddingRight();
        int parentNeededHeight = getPaddingTop() + getPaddingBottom();

        // 保存一行中所有的view
        List<View> lineViews = new ArrayList<>();
        // 记录这行已经使用了多宽, FlowLayout有可能是wrap_content, 所以才需要累计
        int lineWidthUsed = 0;
//        int lineWidthUsed = getPaddingLeft() + getPaddingRight();
        // 一行的行高，一行中最高的高度
        int lineHeight = 0;

        // 度量子View
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            /*
                LayoutParams -> MeasureSpec

                View的MeasureSpec是由
                    父布局的MeasureSpec和子View的宽高共同决定的
             */
            View childView = getChildAt(i);
            if (childView.getVisibility() == View.GONE) {
                continue;
            }
            //将父布局传入给自己的参考尺寸传给子View - 没有意义
            //child.measure(widthMeasureSpec, heightMeasureSpec);
            LayoutParams childViewLP = childView.getLayoutParams();
            int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, paddingLeft + paddingRight, childViewLP.width);
            int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, paddingTop + paddingBottom, childViewLP.height);
            childView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            //获取子View的尺寸
            int childMeasuredWidth = childView.getMeasuredWidth();
            int childMeasuredHeight = childView.getMeasuredHeight();

            /*
                先处理一行的情况，再处理多行的情况

                判断是否需要换行
                    selfWidth是已经包括了padding的宽度呀
                        getChildMeasureSpec
             */
            if (lineWidthUsed + mHorizontalSpacing + childMeasuredWidth > selfWidth) {

                Log.e(TAG, "lineViews#size: " + lineViews.size());

                // 用于layout
                allLines.add(lineViews);
                lineHeights.add(lineHeight);

                /*
                    累计父布局需要的宽高, 宽度是所有行中最大的宽度, wrap_content的情况需要手动累加
                        高度是累计高度
                        宽度是所有行的最大宽度
                 */
                parentNeededHeight = parentNeededHeight + lineHeight + mVerticalSpacing;
                parentNeededWidth = Math.max(parentNeededWidth, lineWidthUsed + mHorizontalSpacing);

                // 换行了，需要改变一些参数
                lineViews = new ArrayList<>();
                lineWidthUsed = 0;
//                lineWidthUsed = getPaddingLeft() + getPaddingRight();
                lineHeight = 0;
            }

            // view是要分行layout, 所以要记录每一行有哪些view, 这样可以方便layout
            lineViews.add(childView);

            /*
                每行都会有自己的宽高
                高度为一行中最高的高度
             */
            lineWidthUsed = lineWidthUsed + mHorizontalSpacing + childMeasuredWidth;
            lineHeight = Math.max(lineHeight, childMeasuredHeight);
        }

        /*
            不足一行的情况
         */
        if (lineViews.size() > 0) {
            // 用于layout
            allLines.add(lineViews);
            lineHeights.add(lineHeight);

            // 累计父布局需要的宽高, 宽度是所有行中最大的宽度
            parentNeededHeight = parentNeededHeight + lineHeight + mVerticalSpacing;
            parentNeededWidth = Math.max(parentNeededWidth, lineWidthUsed + mHorizontalSpacing);

            // 换行了，需要改变一些参数
            lineViews = null;
            lineWidthUsed = 0;
            lineHeight = 0;
        }

        // parent
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int realWidth = (widthMode == MeasureSpec.EXACTLY) ? selfWidth : parentNeededWidth;
        int realHeight = (heightMode == MeasureSpec.EXACTLY) ? selfHeight : parentNeededHeight;

        /*
            去掉最后一行的verticalSpacing, 不然很难看
         */
        if (realHeight > 0) {
            realHeight -= mVerticalSpacing;
        }

        Log.e(TAG, "widthMode == MeasureSpec.EXACTLY: " + (widthMode == MeasureSpec.EXACTLY));  // true, match_parent == exactly
        Log.e(TAG, "heightMode == MeasureSpec.EXACTLY: " + (heightMode == MeasureSpec.EXACTLY));// false, wrap_content != exactly
        Log.e(TAG, "heightMode == MeasureSpec.AT_MOST: " + (heightMode == MeasureSpec.AT_MOST));// true, wrap_content == at_most

        // 度量ViewGroup
        setMeasuredDimension(realWidth, realHeight);
    }

    /*
     * ViewGroup主要是用来布局用的
     *      只需要对子View进行布局
     *
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int currentOffsetLeft = getPaddingLeft();
        int currentOffsetTop = getPaddingTop();

        int lineCount = allLines.size();
        for (int i = 0; i < lineCount; i++) {
            List<View> lineViews = allLines.get(i);
            int lineHeight = lineHeights.get(i);
            for (int j = 0; j < lineViews.size(); j++) {
                View childView = lineViews.get(j);
                int left = currentOffsetLeft;
                int top = currentOffsetTop;

                // MeasuredWidth在childView的onMeasure之后就可以获取
                int right = left + childView.getMeasuredWidth();
                int bottom = top + childView.getMeasuredHeight();

                // TODO: 在childView的onLayout之前，无意义
                //int right = left + childView.getWidth();
                //int bottom = top + childView.getHeight();

                childView.layout(left, top, right, bottom);

                // 在childView的onLayout之后，有意义
                //int right = left + childView.getWidth();
                //int bottom = top + childView.getHeight();
                currentOffsetLeft = right + mHorizontalSpacing;

                // debug
                TextView textView = (TextView) childView;
                Log.e(TAG, "textView: " + textView.getText());
            }
            Log.e(TAG, "********************************* row: " + i);
            currentOffsetLeft = getPaddingLeft();
            currentOffsetTop = currentOffsetTop + lineHeight + mVerticalSpacing;
        }
    }

    private int dp2px(int dp) {
        // return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    private FlowAdapter mAdapter;

    public void setAdapter(FlowAdapter adapter) {

        if (adapter == null) {
            throw new RuntimeException("adapter cannot be null");
        }

        mAdapter = adapter;

        // 获取子View数量
        for (int i = 0; i < mAdapter.getCount(); i++) {
            // 通过索引获取子View
            View view = mAdapter.getView(i, this);
            addView(view);
        }
    }
}
