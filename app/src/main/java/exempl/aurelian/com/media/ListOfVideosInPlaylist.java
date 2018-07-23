package exempl.aurelian.com.media;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import exempl.aurelian.com.media.data.Constants;
import exempl.aurelian.com.media.data.PlaylistVideoAdapter;
import exempl.aurelian.com.media.data.VideoAdapter;
import exempl.aurelian.com.media.data.VideoFromPlaylistWasPlaying;
import exempl.aurelian.com.media.data.model.VideoModel;


/**
 * Created by Hermes on 04/08/2017.
 * In this class we have an activity that holds a list of videos from within a playlist.
 */

public class ListOfVideosInPlaylist extends AppCompatActivity implements
        PlaylistVideoAdapter.VideoListener, VideoFromPlaylistWasPlaying {
    public static final String VIDEO_LIST = Constants.VIDEO_LIST;
    public static final String PLAYLIST_NAME = Constants.PLAYLIST_NAME;
    public static final String USER_AGENT = Constants.USER_AGENT;
    public static final String MY_PREFS_NAME = "playlist was liked or not";
    private static final String TAG = ListOfVideosInPlaylist.class.getCanonicalName();
    private static final int TASK_LOADER_ID = 0;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = RESULT_FIRST_USER + 1;
    private static String sToolbarTitleAfterOrientationChange;
    private static boolean sLikeButtonStateChangedAfterBackPressed;

    private PlaylistVideoAdapter mPlaylistVideoAdapter;
    private List<VideoModel> mListOfVideosFromPlaylist;
    private String mVideoUriFromPlaylist;
    private boolean sentByFirstVideo = false;
    private Bundle mSendListOfVideosToFragment;
    private ImageView mFirstVideoThumbnail;
    private boolean mVideoFromPlaylist;

    public ListOfVideosInPlaylist() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_of_videos_in_playlist_layout);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        // Here I get the uri's of the videos in the selected playlist
        mListOfVideosFromPlaylist = getIntent().getParcelableArrayListExtra(VIDEO_LIST);
        Uri uri = mListOfVideosFromPlaylist.get(0).getmUri();
        String uriForFirstVideoInPlaylist = uri.toString();

        mSendListOfVideosToFragment = new Bundle();
        mSendListOfVideosToFragment.putString(FragmentForVideoPlayerInPlaylist.FIRST_VIDEO, uriForFirstVideoInPlaylist);
        mSendListOfVideosToFragment.putParcelableArrayList(FragmentForVideoPlayerInPlaylist.LIST_OF_VIDEOS,(ArrayList<? extends Parcelable>) mListOfVideosFromPlaylist);

        mFirstVideoThumbnail = (ImageView)findViewById(R.id.first_video_playlist);

        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.frame_layout);
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new FragmentForVideoPlayerInPlaylist();
                fragment.setArguments(mSendListOfVideosToFragment);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame_layout, fragment);
                fragmentTransaction.commitNow();
            }
        });

        VideoAdapter mAdapter = new VideoAdapter();
        mAdapter.setUpVideoThumbnailFromUri(mFirstVideoThumbnail, uri);

        //We initialise the recycle view and we set the adapter for it
        RecyclerView mRecyclerView = (RecyclerView)findViewById(R.id.playlist_recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);

        mPlaylistVideoAdapter = new PlaylistVideoAdapter(mListOfVideosFromPlaylist, this);
        mRecyclerView.setAdapter(mPlaylistVideoAdapter);

        Bundle bundle = getIntent().getExtras();
        if (savedInstanceState == null){
            if (bundle == null){
                mVideoUriFromPlaylist = null;
            }
            else{
                mVideoUriFromPlaylist = bundle.getString(PLAYLIST_NAME);
            }
        }
        else{
            mVideoUriFromPlaylist = (String) savedInstanceState.getSerializable(PLAYLIST_NAME);
        }


        TextView toolbar_title = (TextView)findViewById(R.id.toolbar_title);
        if (mVideoUriFromPlaylist != null) {
            toolbar_title.setText(mVideoUriFromPlaylist);
            sToolbarTitleAfterOrientationChange = mVideoUriFromPlaylist;
        }
        else {
            toolbar_title.setText(sToolbarTitleAfterOrientationChange);
        }

        final ImageView like_button = (ImageView)findViewById(R.id.like_button);

        // Verify the state of the like button when loading the activity and setting
        // the right rescource according to the state
        if (getLikeButtonState() != sLikeButtonStateChangedAfterBackPressed){
            if (!getLikeButtonState()) {
                like_button.setImageResource(R.drawable.ic_like_unpressed);

            }else{
                like_button.setImageResource(R.drawable.ic_like_pressed);
            }
        }else{
            if (!sLikeButtonStateChangedAfterBackPressed) {
                like_button.setImageResource(R.drawable.ic_like_unpressed);

            }else{
                like_button.setImageResource(R.drawable.ic_like_pressed);
            }
        }


        like_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getLikeButtonState()) {
                    // if the button was not pressed, we change the state of the button
                    // to pressed in shared preferences
                    sLikeButtonStateChangedAfterBackPressed = true;
                    setLikeButtonState(sLikeButtonStateChangedAfterBackPressed);
                    like_button.setImageResource(R.drawable.ic_like_pressed);
                }else{
                    sLikeButtonStateChangedAfterBackPressed = false;
                    setLikeButtonState(sLikeButtonStateChangedAfterBackPressed);
                    like_button.setImageResource(R.drawable.ic_like_unpressed);
                }
            }
        });


        if (mListOfVideosFromPlaylist.size() == 0 || mListOfVideosFromPlaylist == null){
            Toast.makeText(this, Constants.EMPTY_PLAYLIST_ERROR_MESSAGE, Toast.LENGTH_LONG);
        }

        if (!checkPermission()){
            requestPermission();
        }

    }
    private void setLikeButtonState(boolean wasClicked){
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(Constants.PLAYLIST_LIKED, wasClicked);
        editor.apply();
    }
    private boolean getLikeButtonState(){
        SharedPreferences likeButtonclicked = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        boolean wasLikeButtonClicked = likeButtonclicked.getBoolean(Constants.PLAYLIST_LIKED, false);
        return wasLikeButtonClicked;
    }

    private boolean checkPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermission() {
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
    }

    @Override
    public void onVideoSelected(int position) {
        mVideoFromPlaylist = true;
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_POSITION, position);
        // Add extra data to the intent
        intent.putParcelableArrayListExtra(PlayerActivity.EXTRA_LIST, (ArrayList<? extends Parcelable>)mPlaylistVideoAdapter.getModelList());
        intent.putExtra(Constants.FROM_FIRST_VIDEO_IN_PLAYLIST, sentByFirstVideo);
        startActivity(intent);
    }

    @Override
    public boolean videoFromPlaylistWasPLaying() {
        if (mVideoFromPlaylist) {
            mVideoFromPlaylist = false;
            return true;
        }
        else {
            mVideoFromPlaylist = false;
            return false;
        }
    }
}
