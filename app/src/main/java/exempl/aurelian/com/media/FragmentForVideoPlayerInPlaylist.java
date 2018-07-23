package exempl.aurelian.com.media;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;

import exempl.aurelian.com.media.data.Constants;
import exempl.aurelian.com.media.data.PlaylistVideoAdapter;
import exempl.aurelian.com.media.data.VideoAdapter;
import exempl.aurelian.com.media.data.VideoFromPlaylistWasPlaying;
import exempl.aurelian.com.media.data.model.VideoModel;

/**
 * Created by Hermes on 24/08/2017.
 * This fragment is used to hold the first video from the playlist that we selected
 */

public class FragmentForVideoPlayerInPlaylist extends Fragment implements
        PlaylistVideoAdapter.VideoListener {
    private SimpleExoPlayerView mPlayerView;
    private SimpleExoPlayer mPlayer;
    public static final String FIRST_VIDEO = Constants.FIRST_VIDEO;
    public static final String LIST_OF_VIDEOS = "List of videos";

    private boolean mPlayWhenReady = true;
    private int mCurrentWindow;
    private long mPlaybackPosition;
    private int mPosition;
    public static final String USER_AGENT = Constants.USER_AGENT;
    private String mStringUriForFirstVideo = "";
    private Uri mUriFirstVideo;
    private ImageView mFirstVideoImage;
    private VideoAdapter mAdapter;
    private ImageView mFullScreenIcon;
    private List<VideoModel> mListOfVideos;
    private boolean mVideoWasPlayingInFullScreen;
    private VideoFromPlaylistWasPlaying mVideoFromPlaylistWasPlayingInterface;
    private int mFirstVideoPlayedForTheFirstTime;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mVideoFromPlaylistWasPlayingInterface = (VideoFromPlaylistWasPlaying)context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + Constants.ACTIVITY_DID_NOT_IMPLEMENT_VIDEOSFROMPAYLIST_INTERFACE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.player_for_first_video_in_playlist, container, false);
        mStringUriForFirstVideo = getArguments().getString(FIRST_VIDEO);
        mListOfVideos = getArguments().getParcelableArrayList(LIST_OF_VIDEOS);

        mUriFirstVideo = Uri.parse(mStringUriForFirstVideo);
        mPlayerView = (SimpleExoPlayerView) viewGroup.findViewById(R.id.video_view_in_playlist);
        mPlayerView.setVisibility(View.VISIBLE);
        mFirstVideoImage = (ImageView) viewGroup.findViewById(R.id.first_video);
        mFirstVideoImage.setBackgroundColor(Color.BLACK);
        //We set a listener to the fullscreen icon in order to be able to launch the first video
        //in full screen mode
        mFullScreenIcon = (ImageView) viewGroup.findViewById(R.id.full_screen_icon);
        mFullScreenIcon.bringToFront();
        mFullScreenIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onVideoSelected(0);
                //I set a checker that tells me that the video was played in full screen
                //in order to initialize the player when we exist from full screen mode
                mVideoWasPlayingInFullScreen = true;
            }
        });
        //I increase the counter every time I create a new instance of the fragment
        //in order to release the player
        mFirstVideoPlayedForTheFirstTime++;

        return viewGroup;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        if (Util.SDK_INT <= 23) {
            if (mVideoFromPlaylistWasPlayingInterface.videoFromPlaylistWasPLaying() &&
                    mFirstVideoPlayedForTheFirstTime != 1){
                releasePlayer();
                mPlayerView.setVisibility(View.GONE);
            }else if (mPlayer == null || mVideoWasPlayingInFullScreen){
                mFirstVideoImage.setImageResource(android.R.color.transparent);
                mFirstVideoImage.setBackgroundColor(Color.BLACK);
                mPlayerView.setVisibility(View.VISIBLE);
                //I use this counter in order to verify when is the fragment initialised
                //for the first time
                mFirstVideoPlayedForTheFirstTime++;
                initializePlayer();
                //I reset the checker that tells me that the video was playing in full screen
                mVideoWasPlayingInFullScreen = false;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
            mPlayerView.setVisibility(View.VISIBLE);
            mFullScreenIcon.setVisibility(View.GONE);
            mAdapter = new VideoAdapter();
            mAdapter.setUpVideoThumbnailFromUri(mFirstVideoImage, mUriFirstVideo);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void initializePlayer() {
        if (mPlayer == null) {
            mPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(getActivity().getApplicationContext()),
                    new DefaultTrackSelector(), new DefaultLoadControl());
            mPlayerView.setPlayer(mPlayer);
            mPlayer.setPlayWhenReady(mPlayWhenReady);
        }
        mFullScreenIcon.setVisibility(View.VISIBLE);
        //We give the uri of the first video in the playlist and we convert it into a media source
        MediaSource[] mediaSources = new MediaSource[1];
        mediaSources[0] = buildMediaSource(mUriFirstVideo);

        MediaSource mediaSource1 = new ConcatenatingMediaSource(mediaSources);
        boolean haveResumePosition = mCurrentWindow != C.INDEX_UNSET;

        if (haveResumePosition) {
            mPlayer.seekToDefaultPosition(mPosition);
        }
        mPlayerView.setResizeMode(0);
        mPlayerView.hideController();
        mPlayer.prepare(mediaSource1, true, false);
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlaybackPosition = mPlayer.getCurrentPosition();
            mCurrentWindow = mPlayer.getCurrentWindowIndex();
            mPlayWhenReady = mPlayer.getPlayWhenReady();
            mPlayer.release();
            mPlayer = null;
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource(uri, new DefaultDataSourceFactory(getActivity().getApplicationContext(), USER_AGENT),
                new DefaultExtractorsFactory(), null, null);
    }

    @Override
    public void onVideoSelected(int position) {
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_POSITION, position);
        intent.putParcelableArrayListExtra(PlayerActivity.EXTRA_LIST, (ArrayList<? extends Parcelable>) mListOfVideos);
        startActivity(intent);
    }
}
