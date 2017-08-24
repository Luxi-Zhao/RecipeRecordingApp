package com.example.lucyzhao.notetaking;

import android.support.v7.widget.RecyclerView;

/**
 * Created by LucyZhao on 2017/8/23.
 * Source: https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf
 * TODO fun ways to delete ingredients and procedures
 */

public interface ItemTouchHelperAdapter {
    void onItemMove(RecyclerView.ViewHolder holder, int fromPosition, int toPosition);

    void onItemDismiss(int position);
}
