package com.cyl.musiclake.ui.onlinemusic.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.cyl.musiclake.R;
import com.cyl.musiclake.ui.onlinemusic.adapter.TaskItemAdapter;
import com.cyl.musiclake.data.source.download.TasksManager;
import com.cyl.musiclake.ui.base.BaseFragment;
import com.liulishuo.filedownloader.FileDownloader;

import java.lang.ref.WeakReference;

import butterknife.BindView;

/**
 * Created by yonglong on 2016/11/26.
 */

public class DownloadManagerFragment extends BaseFragment {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    TaskItemAdapter mAdapter;

    public static DownloadManagerFragment newInstance() {
        Bundle args = new Bundle();
        DownloadManagerFragment fragment = new DownloadManagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initDatas() {
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_recyclerview_notoolbar;
    }

    @Override
    public void initViews() {
        mAdapter = new TaskItemAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        TasksManager.getImpl().onCreate(new WeakReference<>(this));
    }

    public void postNotifyDataChanged() {
        if (mAdapter != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter = null;
        super.onDestroy();
    }
}
