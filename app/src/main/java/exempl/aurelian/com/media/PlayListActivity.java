package exempl.aurelian.com.media;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import exempl.aurelian.com.media.data.Constants;
import exempl.aurelian.com.media.data.DBManager;
import exempl.aurelian.com.media.data.SimpleAdapter;
import exempl.aurelian.com.media.data.VideoAdapter;
import exempl.aurelian.com.media.data.model.VideoModel;

/**
 * Created by Aurelian Cotuna on 8/2/17.
 * Existing playlists
 */

public class PlayListActivity extends AppCompatActivity implements SimpleAdapter.PlayListListener {
    private SimpleAdapter mAdapter;
    private static final int REQUEST_UPDATE_CODE = RESULT_FIRST_USER + 1;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private Boolean mDrawerItemSelected;
    private static final String TAG = PlayListActivity.class.getCanonicalName();
    private ImageView mPlaylistThumbnail;
    private int mIndexForPlaylist = 0;

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            DBManager.getInstance().getAppDataBase(PlayListActivity.this).playListDao().delete(mAdapter.getmItems().get(position));
            mAdapter.getmItems().remove(position);
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_of_playlists_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.cust_toolbar_playlist);
        toolbar.setTitle(Constants.PLAYLISTS_ACTIVITY_TOOLBAR_TITLE);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view_playlist);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                mDrawerLayout.closeDrawers();
                selectedDrawerItem(item);
                if (mDrawerItemSelected)
                    return true;
                else
                    return false;
            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_playlist);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_playlist);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new SimpleAdapter(DBManager.getInstance().getAppDataBase(this).playListDao().getAll(), this);
        mRecyclerView.setAdapter(mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        mPlaylistThumbnail = (ImageView)findViewById(R.id.playlist_cover);
        int numberOfPlaylists = mAdapter.getItemCount();

       // getFirstVideoImageFromPlaylist(mPlaylistThumbnail, numberOfPlaylists);

    }

    private boolean selectedDrawerItem(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.first_page: {
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
            case R.id.create_playlist: {
                CreatePlayListActivity.startActivity(this);
                mDrawerItemSelected = true;
                finish();
                return true;
            }
            default: {
                Toast.makeText(this, Constants.SELECTING_NAVIGATION_VIEW_ITEMS_ERROR, Toast.LENGTH_SHORT).show();
                return true;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onPlayListSelected(int position) {
        Toast.makeText(this, Constants.POSITION_OF_VIDEO_SELECTED + position, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, ListOfVideosInPlaylist.class);

        //Primesc toate uri-urile dintr-un playlist
        String playListUris = mAdapter.getmItems().get(position).playList;
        String playListName = mAdapter.getmItems().get(position).name;
        String[] uris = playListUris.split(";");
        String[] videoTitles = titlesOfVideosInSelectedPlaylist(uris);
        int indexOfVideosTitles = 0;

        List<VideoModel> videoModelList = new ArrayList<>();
        for (String uri : uris) {
            VideoModel videoModel = new VideoModel();
            videoModel.setmUri(Uri.parse(uri));
            videoModel.setmTitle(videoTitles[indexOfVideosTitles]);
            videoModelList.add(videoModel);
            indexOfVideosTitles++;
        }

        intent.putParcelableArrayListExtra(ListOfVideosInPlaylist.VIDEO_LIST, (ArrayList<? extends Parcelable>) videoModelList);
        intent.putExtra(ListOfVideosInPlaylist.PLAYLIST_NAME, playListName);
        startActivity(intent);
    }

    private String getFirstVideoImageFromPlaylist(ImageView playlistThumbnail, int numberOfPlaylists) {
        String playlistUris;
        if (numberOfPlaylists < mIndexForPlaylist){
            return null;
        }else {
            playlistUris = mAdapter.getmItems().get(mIndexForPlaylist).playList;
            String[] uris = playlistUris.split(";");
            /*if (!TextUtils.isEmpty(uris[0])) {
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(uris[0]);
                Bitmap thumbnail = mediaMetadataRetriever.getFrameAtTime();
                playlistThumbnail.setImageBitmap(thumbnail);
            }*/
            mIndexForPlaylist++;
            return uris[0] + getFirstVideoImageFromPlaylist(playlistThumbnail, numberOfPlaylists-1);
        }


        //for (int index = 0; index < numberOfPlaylists; index++) {


        //}
    }

    /**
     * Getting the title of the individual videos from the selected palylist.
     *
     * @param uris all the videos from the selected playlist
     * @return an array of titles
     */
    public String[] titlesOfVideosInSelectedPlaylist(String[] uris) {
        int backsalshCounter = 0;
        String[] videoTitles = new String[uris.length];
        int indexOfVideos = 0;

        while (indexOfVideos < uris.length) {
            backsalshCounter = 0;
            //We count the number of backslashes in the Uri
            for (int indexUrisSubstring = 0; indexUrisSubstring < uris[indexOfVideos].length(); indexUrisSubstring++) {
                char characterInString = uris[indexOfVideos].charAt(indexUrisSubstring);
                if (characterInString == '/') {
                    backsalshCounter++;
                }
            }
            //We create an array of backslash positions
            Integer[] backslashPositionsInString = new Integer[backsalshCounter];
            backsalshCounter = 0;
            //We store the positions of every backslash from the Uri string of a video
            for (int indexUrisSubstring = 0; indexUrisSubstring < uris[indexOfVideos].length(); indexUrisSubstring++) {
                char characterInString = uris[indexOfVideos].charAt(indexUrisSubstring);
                if (characterInString == '/') {
                    backslashPositionsInString[backsalshCounter] = indexUrisSubstring;
                    backsalshCounter++;
                }
            }

            //We store the position of the last backslash right before the name of the video
            //so we can get the title of the video by using substring
            int lastBackslashIndex = 0;
            for (int index = 0; index < backslashPositionsInString.length; index++) {
                lastBackslashIndex = backslashPositionsInString[index];
            }

            lastBackslashIndex++;
            //We store only the title of the video from the Uri.
            videoTitles[indexOfVideos] = uris[indexOfVideos].substring(lastBackslashIndex, uris[indexOfVideos].length());
            indexOfVideos++;
        }
        return videoTitles;
    }

    @Override
    public void onEditPlayListSelected(int position) {
        CreatePlayListActivity.startActivityForResult(this, mAdapter.getmItems().get(position), REQUEST_UPDATE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UPDATE_CODE && resultCode == RESULT_OK) {
            mAdapter.updateItems(DBManager.getInstance().getAppDataBase(this).playListDao().getAll());
        }
    }
}
