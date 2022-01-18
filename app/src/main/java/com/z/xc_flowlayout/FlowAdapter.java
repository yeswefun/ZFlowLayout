package com.z.xc_flowlayout;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

/*
    动态添加view
 */
public abstract class FlowAdapter {

    /*
        子View的个数
     */
    public abstract int getCount();

    /*
        通过索引获取子View
     */
    public abstract View getView(int position, ViewGroup parent);

    /*
        参考android.widget.BaseAdapter
     */
    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }
}
