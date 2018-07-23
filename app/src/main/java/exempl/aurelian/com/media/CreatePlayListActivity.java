package exempl.aurelian.com.media;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import exempl.aurelian.com.media.data.Constants;
import exempl.aurelian.com.media.data.DBManager;
import exempl.aurelian.com.media.data.EditAdapter;
import exempl.aurelian.com.media.data.model.PlayList;
import exempl.aurelian.com.media.data.model.VideoModel;

/**
 * Created by Aurelian Cotuna on 8/1/17.
 * In this activity we are creating a new playlist
 */

public class CreatePlayListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int TASK_LOADER_ID = 0;
    private static final String TAG = CreatePlayListActivity.class.getName();
    public static final String EXTRA_PLAYLIST = Constants.EXTRA_PLAYLISTS;

    private EditAdapter mAdapter;
    private EditText mPlayListNameEditText;
    private PlayList mPlayList;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private Boolean mDrawerItemSelected;



    public static void startActivityForResult(Activity activity, PlayList playList, int code) {
        Intent intent = new Intent(activity, CreatePlayListActivity.class);
        intent.putExtra(EXTRA_PLAYLIST, playList);
        activity.startActivityForResult(intent, code);
    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, CreatePlayListActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_play_list_activity);
        Toolbar mToolbar = (Toolbar)findViewById(R.id.toolbar_create_playlist);

        mToolbar.setTitle(Constants.CREATE_PLAYLIST_ACTIVITY_TOOLBAR_TITLE);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(mToolbar);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view2);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                mDrawerLayout.closeDrawers();
                selectDrawerItem(item);
                if (mDrawerItemSelected)
                    return true;
                else
                    return false;
            }
        });

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_create_playlist);
        mPlayListNameEditText = (EditText) findViewById(R.id.create_play_list_name);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new EditAdapter(new ArrayList<VideoModel>());
        mRecyclerView.setAdapter(mAdapter);
        //Here we receive the playlist that we want to edit
        mPlayList = getIntent().getParcelableExtra(EXTRA_PLAYLIST);

        //We check if we have received a playlist that we have to edit
        if (mPlayList != null) {
            mPlayListNameEditText.setText(mPlayList.name);
        }
        getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, this);

    }

    private boolean selectDrawerItem(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case R.id.first_page:{
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                mDrawerItemSelected = true;
                finish();
                return true;
            }
            case R.id.playlists: {
                startActivity(new Intent(this, PlayListActivity.class));
                mDrawerItemSelected = true;
                finish();
                return true;
            }
            case R.id.create_playlist:{
                CreatePlayListActivity.startActivity(this);
                mDrawerItemSelected = true;
                finish();
                return true;
            }
            default:{
                Toast.makeText(getApplicationContext(), Constants.SELECTING_NAVIGATION_VIEW_ITEMS_ERROR, Toast.LENGTH_SHORT).show();
                return true;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        char firstChar = mPlayListNameEditText.getText().toString().charAt(0);
        switch (item.getItemId()) {
            case R.id.save:
                if (mPlayList == null) {
                    PlayList playList = new PlayList();

                    if (mPlayListNameEditText.getText().toString().equals("") ||
                            firstChar == ' '){
                        Toast.makeText(this, Constants.NO_PLAYLIST_NAME, Toast.LENGTH_LONG).show();
                        break;
                    }else {
                        playList.name = mPlayListNameEditText.getText().toString();
                        if (!TextUtils.isEmpty(mAdapter.getSelectedItems())) {
                            playList.playList = mAdapter.getSelectedItems();
                            DBManager.getInstance().getAppDataBase(this).playListDao().insert(playList);
                            setResult(RESULT_OK);
                            finish();
                            enterPlaylistActivityAfterCreatingPlaylist();
                        }
                    }
                } else {
                    if (mPlayListNameEditText.getText().toString().equals("") ||
                            firstChar == ' ') {
                        Toast.makeText(this, Constants.NO_PLAYLIST_NAME, Toast.LENGTH_LONG).show();
                        break;
                    } else {
                        mPlayList.name = mPlayListNameEditText.getText().toString();
                        if (!TextUtils.isEmpty(mAdapter.getSelectedItems())) {
                            mPlayList.playList = mAdapter.getSelectedItems();

                            DBManager.getInstance().getAppDataBase(this).playListDao().updatePlayList(mPlayList);
                            setResult(RESULT_OK);
                            finish();
                            enterPlaylistActivityAfterCreatingPlaylist();
                        }
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new AsyncTaskLoader<Cursor>(this) {
            Cursor mTaskData = null;

            @Override
            protected void onStartLoading() {
                if (mTaskData != null) {
                    // If we have data the deliver it
                    deliverResult(mTaskData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {
                try {
                    Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    String[] projection = {MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.DISPLAY_NAME, MediaStore.Video.VideoColumns._ID};
                    return getContentResolver().query(uri, projection, null, null, null);
                } catch (Exception e) {
                    Log.e(TAG, Constants.LOADING_VIDEOS_IN_BACKGROUND_ERROR);
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<VideoModel> items = new ArrayList<>();
        while (data.moveToNext()) {
            VideoModel videoModel = new VideoModel(data);
            videoModel.setmChecked(isChecked(videoModel.getmUri().toString()));
            items.add(videoModel);
        }
        mAdapter.updateItems(items);
    }

    private boolean isChecked(String uri) {
        if (mPlayList != null) {
            String[] data = mPlayList.playList.split(";");
            for (String currentUri : data) {
                if (currentUri.equals(uri)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.updateItems(new ArrayList<VideoModel>());
    }

    public void enterPlaylistActivityAfterCreatingPlaylist(){
        Intent intent = new Intent(this, PlayListActivity.class);
        startActivity(intent);
    }
}
