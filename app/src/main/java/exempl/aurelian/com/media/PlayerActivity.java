package exempl.aurelian.com.media;


import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.List;

import exempl.aurelian.com.media.data.Constants;
import exempl.aurelian.com.media.data.model.VideoModel;

/**
 * Created by Aurelian Cotuna on 8/1/17.
 * In this activity we handle the player that we use to play a selected video
 */

public class PlayerActivity extends AppCompatActivity {
    public static int sVideoIndex;
    public static final String EXTRA_POSITION = "The position of the selected video in the list of available videos";
    public static final String EXTRA_LIST = "The list of video";

    private SimpleExoPlayer mPlayer;
    private SimpleExoPlayerView mPlayerView;

    private static long mPlaybackPosition;
    private int mCurrentWindow;
    private boolean mPlayWhenReady = true;
    private List<VideoModel> mExtraList;
    private int mPosition;
    private boolean mSentByFirstVideo;
    private int mLastWindowIndex;
    private int mLastWindowIndexFromSavedInstanceState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSentByFirstVideo = getIntent().getBooleanExtra(Constants.FROM_FIRST_VIDEO_IN_PLAYLIST, false);

        // We check if this activity was called by the fragment that holds the first video
        // in the playlist, if so, we set a different layout for it
        if (mSentByFirstVideo) {
            setContentView(R.layout.list_of_videos_in_playlist_layout);
        } else {
            setContentView(R.layout.activity_player);
            mPlayerView = (SimpleExoPlayerView) findViewById(R.id.video_view);
        }

        // We receive the list of videos that we will play
        mExtraList = getIntent().getParcelableArrayListExtra(EXTRA_LIST);
        // We receive the position of the video selected in the playlist or the list of videos
        mPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.POSITION_LAST_VIDEO, mLastWindowIndex);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mLastWindowIndexFromSavedInstanceState = savedInstanceState.getInt(Constants.POSITION_LAST_VIDEO);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT <= 23 || mPlayer == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Show the controls on any key event.
        mPlayerView.showController();

        // If the event was not handled then see if the mPlayer view can handle it as a media key event.
        return super.dispatchKeyEvent(event) || mPlayerView.dispatchMediaKeyEvent(event);
    }

    private void initializePlayer() {
        if (mPlayer == null) {
            mPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this),
                    new DefaultTrackSelector(), new DefaultLoadControl());

            mPlayerView.setPlayer(mPlayer);
            mPlayer.setPlayWhenReady(mPlayWhenReady);
        }

        MediaSource[] mediaSources = new MediaSource[mExtraList.size()];
        for (int i = 0; i < mExtraList.size(); i++) {
            mediaSources[i] = buildMediaSource(mExtraList.get(i).getmUri());
        }
        MediaSource mediaSource = new LoopingMediaSource(mediaSources.length == 1 ? mediaSources[0]
                : new ConcatenatingMediaSource(mediaSources));

        //Here we check the position of the video that was clicked on from the playlist
        boolean haveResumePosition = mCurrentWindow != C.INDEX_UNSET;
        if (haveResumePosition) {
            mPlayer.seekToDefaultPosition(mPosition);
        }


        if (mLastWindowIndex != mLastWindowIndexFromSavedInstanceState) {
            // In cazul in care schimbam orientarea si schimbam si videoul
            mPlayer.seekToDefaultPosition(mLastWindowIndexFromSavedInstanceState);
            mPlayer.seekTo(mPlaybackPosition);
        } else if (mPosition != 0 && sVideoIndex == 0) {
            // In cazul in care selectez un video dupa ce am dat back
            mPlayer.seekToDefaultPosition(mPosition);
            mPlayer.seekTo(mPlaybackPosition);
        } else {
            // In cazul in care schimbam orientarea dar nu schimbam videoul
            mPlayer.seekToDefaultPosition(sVideoIndex);
            mPlayer.seekTo(mPlaybackPosition);
        }

        mPlayer.prepare(mediaSource, true, false);
        mPlayer.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            // Verificam atunci cand se schimba videoul urmarit
            @Override
            public void onPositionDiscontinuity() {
                int latestWindowIndex = mPlayer.getCurrentWindowIndex();
                Log.d("Current position= ", Long.toString(mPlayer.getCurrentPosition()));
                Log.d("Win index listener = ", Integer.toString(mPlayer.getCurrentWindowIndex()));
                if (latestWindowIndex != mLastWindowIndex) {
                    // savedInstanceState se cheama doar in momentul in care se schimba orientarea
                    // ecranului si atunci trebuie sa verificam ca ultima pozitie sa fie aceeasi
                    // cu pozitia videoului schimbat ca sa trecem la videoul selectat in momentul
                    // in care si orientarea a fost facuta
                    mLastWindowIndex = latestWindowIndex;
                    sVideoIndex = mLastWindowIndex;

                    if (mLastWindowIndex != mLastWindowIndexFromSavedInstanceState) {
                        mPlayer.seekToDefaultPosition(mLastWindowIndex);
                    }
                }

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            }
        });
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlaybackPosition = mPlayer.getCurrentPosition();
            Log.d("Pozitia videoului = ", Long.toString(mPlaybackPosition));
            mCurrentWindow = mPlayer.getCurrentWindowIndex();
            mPlayWhenReady = mPlayer.getPlayWhenReady();
            mPlayer.release();
            mPlayer = null;
        }
    }


    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource(uri, new DefaultDataSourceFactory(this, Constants.USER_AGENT),
                new DefaultExtractorsFactory(), null, null);

    }

    private void hideSystemUi() {
        mPlayerView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sVideoIndex = 0;
        mPlaybackPosition = 0;
    }
}
