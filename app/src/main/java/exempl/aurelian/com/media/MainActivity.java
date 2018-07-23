package exempl.aurelian.com.media;

import android.Manifest;
import android.arch.persistence.room.Database;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import exempl.aurelian.com.media.data.Constants;
import exempl.aurelian.com.media.data.DBManager;
import exempl.aurelian.com.media.data.VideoAdapter;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        VideoAdapter.VideoListener {

    private static final String TAG = MainActivity.class.getCanonicalName();
    private static final int TASK_LOADER_ID = 0;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = RESULT_FIRST_USER + 1;
    private VideoAdapter mAdapter;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private Boolean mDrawerItemSelected = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view_main);
        mToolbar = (Toolbar) findViewById(R.id.custom_toolbar_activity_main);
        mToolbar.setTitle(Constants.MAIN_SCREEN_TITLE);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                mDrawerLayout.closeDrawers();
                // We send the selected item in the menu from the navigation drawer to a function
                // which using a switch statement, starts the specific activity for the item
                // selected in the menu
                selectingDrawerItems(item);
                if (mDrawerItemSelected)
                    return true;
                else
                    return false;
            }
        });


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_main);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new VideoAdapter(null, this, this);
        mRecyclerView.setAdapter(mAdapter);

        if (checkPermission()) {
            printNamesToLogCat();
        } else {
            requestPermission();
        }

    }

    private boolean selectingDrawerItems(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.first_page:{
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                mDrawerItemSelected = true;
                finish();
                return true;
            }
            case R.id.playlists: {
                startActivity(new Intent(getApplicationContext(), PlayListActivity.class));
                mDrawerItemSelected = true;
                return true;
            }
            case R.id.create_playlist: {
                CreatePlayListActivity.startActivity(this);
                mDrawerItemSelected = true;
                return true;
            }
            default: {
                Toast.makeText(getApplicationContext(), Constants.SELECTING_NAVIGATION_VIEW_ITEMS_ERROR,
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        }
    }


    private boolean checkPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermission() {
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
    }

    public void printNamesToLogCat() {
        getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted! Let's go!
                    printNamesToLogCat();

                } else {
                    Toast.makeText(this, Constants.REQUEST_DENIED, Toast.LENGTH_SHORT).show();
                }
            }
        }
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
    // this is called right after the loadInBackground function has finished and it returns
    // a cursor to this method.
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //data.moveToPrevious();
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onVideoSelected(int position) {
        Toast.makeText(this, Constants.POSITION_OF_VIDEO_SELECTED + position, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_POSITION, position);
        intent.putParcelableArrayListExtra(PlayerActivity.EXTRA_LIST, (ArrayList<? extends Parcelable>) mAdapter.getmModelList());
        startActivity(intent);
    }
}
