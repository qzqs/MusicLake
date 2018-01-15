package com.cyl.musiclake.ui.onlinemusic.activity;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.afollestad.materialdialogs.MaterialDialog;
import com.cyl.musiclake.R;
import com.cyl.musiclake.data.model.Music;
import com.cyl.musiclake.service.PlayManager;
import com.cyl.musiclake.ui.base.BaseActivity;
import com.cyl.musiclake.ui.onlinemusic.SearchAdapter;
import com.cyl.musiclake.ui.onlinemusic.contract.SearchContract;
import com.cyl.musiclake.ui.onlinemusic.presenter.SearchPresenter;
import com.cyl.musiclake.utils.FileUtils;
import com.cyl.musiclake.utils.ToastUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 作者：yonglong on 2016/9/15 12:32
 * 邮箱：643872807@qq.com
 * 版本：2.5
 */
public class SearchActivity extends BaseActivity implements SearchView.OnQueryTextListener, SearchContract.View {

    //搜索信息
    private String queryString;
    private SearchAdapter mAdapter;
    private MaterialDialog mProgressDialog;
    private int mOffset = 1;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private List<Music> searchResults = new ArrayList<>();

    SearchPresenter mPresenter = new SearchPresenter();

    private int mCurrentCounter = 0;
    private int TOTAL_COUNTER = 10;

    @Override
    protected int getLayoutResID() {
        return R.layout.acitvity_search;
    }

    @Override
    protected void initView() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPresenter.attachView(this);
    }

    @Override
    protected void initData() {

        mAdapter = new SearchAdapter(searchResults);
        //初始化列表
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.bindToRecyclerView(mRecyclerView);

        mProgressDialog = new MaterialDialog.Builder(this)
                .content(R.string.loading)
                .progress(true, 0)
                .build();
    }

    @Override
    protected void listener() {
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            Music music = (Music) adapter.getItem(position);
            Log.e("TAH", music.toString());
            if (music.getType() == Music.Type.QQ) {
                PlayManager.playQQMusic(music);
            } else if (music.getType() == Music.Type.XIAMI) {
                PlayManager.playOnline(music);
            }
        });
        mAdapter.setOnLoadMoreListener(() -> mRecyclerView.postDelayed(() -> {
            if (mCurrentCounter >= TOTAL_COUNTER) {
                //数据全部加载完毕
                mAdapter.loadMoreEnd();
            } else {
                //成功获取更多数据
                mPresenter.search(queryString, 10, mOffset);
                mCurrentCounter = mAdapter.getData().size();
                TOTAL_COUNTER = 10 * mOffset;
            }
        }, 1000), mRecyclerView);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        final SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.onActionViewExpanded();
        searchView.setQueryHint(getString(R.string.search_tips));
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(true);
        try {
            Field field = searchView.getClass().getDeclaredField("mGoButton");
            field.setAccessible(true);
            ImageView mGoButton = (ImageView) field.get(searchView);
            mGoButton.setImageResource(R.drawable.ic_search_white_18dp);
            mGoButton.setOnClickListener(v -> {
                queryString = searchView.getQuery().toString();
                if (queryString.length() > 0) {
                    mOffset = 1;
                    searchResults.clear();
                    mPresenter.search(queryString, 10, mOffset);
                } else {
                    ToastUtils.show(getApplicationContext(), "不能搜索空文本");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setOnPopupMenuListener(View view, final int position) {
        PopupMenu mPopupmenu = new PopupMenu(this, view);
        mPopupmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.popup_song_play:
//                        play(searchResults.get(position));
                        break;
                    case R.id.popup_song_detail:
                        getMusicInfo(searchResults.get(position));
                        break;
                    case R.id.popup_song_goto_artist:
//                        Intent intent = new Intent(SearchActivity.this, ArtistInfoActivity.class);
//                        intent.putExtra(Extras.TING_UID, searchResults.get(position).getar());
//                        startActivity(intent);
                        break;
                    case R.id.popup_song_download:
                        break;

                }
                return false;
            }
        });
        mPopupmenu.inflate(R.menu.popup_song_online);
        mPopupmenu.show();
    }

    private void getMusicInfo(Music music) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("歌曲信息");
        StringBuilder sb = new StringBuilder();
        sb.append("歌曲名：")
                .append(FileUtils.getTitle(music.getTitle()))
                .append("\n\n")
                .append("歌手：")
                .append(FileUtils.getArtist(music.getArtist()))
                .append("\n\n")
                .append("专辑：")
                .append(music.getAlbum())
                .append("\n\n")
                .append("专辑Id：")
                .append(music.getAlbumId());
        dialog.setMessage(sb.toString());
        dialog.show();
    }


    @Override
    public void showLoading() {
        mProgressDialog.show();
    }

    @Override
    public void hideLoading() {
        mProgressDialog.cancel();
    }

    @Override
    public void showSearchResult(List<Music> list) {
        if (mOffset == 1) {
            mAdapter.setNewData(list);
        } else {
            mAdapter.addData(list);
        }
        searchResults.addAll(list);
        mOffset++;
    }

    @Override
    public void showEmptyView() {

    }
}
