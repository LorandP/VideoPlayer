package exempl.aurelian.com.media.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AlignmentSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import exempl.aurelian.com.media.R;
import exempl.aurelian.com.media.data.model.VideoModel;

/**
 * Created by Aurelian Cotuna on 8/1/17.
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {
    @Nullable
    private Cursor mCursor;
    @NonNull
    private List<VideoModel> mModelList;
    @Nullable
    private VideoListener mListener;
    private Activity mActivity;
    private String WATCH_LATER_STATE = Constants.WATCH_LATER_WAS_CLICKED;
    private static boolean sWatchLaterWasClicked;
    private List<Integer> mVideoId;


    public interface VideoListener {
        void onVideoSelected(int position);
    }

    public VideoAdapter() {
    }

    public VideoAdapter(@Nullable Cursor cursor, @Nullable VideoListener listener) {
        this.mCursor = cursor;
        this.mModelList = new ArrayList<>();
        this.mListener = listener;
        this.mVideoId = new ArrayList<>();
    }

    public VideoAdapter(@Nullable Cursor cursor, @Nullable VideoListener listener, Context context) {
        this.mCursor = cursor;
        this.mModelList = new ArrayList<>();
        this.mListener = listener;
        this.mActivity = (Activity) context;
        this.mVideoId = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (mCursor != null && mCursor.moveToPosition(position)) {
            editLongTitle(holder, mCursor);
            setUpVideoThumbnail(holder.mThumb, position, getmModelList());
        }
        holder.mWatchLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getWatchLaterState()){
                   // sWatchLaterWasClicked = true;
                    //We add the clicked video id from the cursor
                    mVideoId.add(Integer.parseInt(mCursor.getString(2)));
                  //  setWatchLaterState(sWatchLaterWasClicked);
                    holder.mWatchLater.setImageResource(R.drawable.ic_watchlater_pressed);

                }else{
                  //  sWatchLaterWasClicked = false;
                    //We check if the video id(received from the cursor) is found in the list
                    //if yes, then we remove it.
                    for (int index = 0; index < mVideoId.size(); index++){
                        if (mVideoId.get(index)==Integer.parseInt(mCursor.getString(2))){
                            mVideoId.remove(index);
                        }
                    }
                   // setWatchLaterState(sWatchLaterWasClicked);
                    holder.mWatchLater.setImageResource(R.drawable.ic_watchlater_unpressed);
                }

            }
        });


        if (getWatchLaterState() != sWatchLaterWasClicked){
            if (!getWatchLaterState()){
                holder.mWatchLater.setImageResource(R.drawable.ic_watchlater_unpressed);
            }else{
                Log.d("Position of cardview= ", Integer.toString(position));
                holder.mWatchLater.setImageResource(R.drawable.ic_watchlater_pressed);
            }
        }else{
            if (!sWatchLaterWasClicked){
                holder.mWatchLater.setImageResource(R.drawable.ic_watchlater_unpressed);
            }else{
                holder.mWatchLater.setImageResource(R.drawable.ic_watchlater_pressed);
            }
        }

    }
    private void setWatchLaterState(List<Integer> videoId) {
        SharedPreferences.Editor editor = mActivity.getSharedPreferences(WATCH_LATER_STATE,
                mActivity.MODE_PRIVATE).edit();
        //editor.putBoolean(Constants.WATCH_LATER_ICON_STATE, wasClicked);

        editor.apply();
    }

    private boolean getWatchLaterState(){
        SharedPreferences watchLaterState = mActivity.getSharedPreferences(WATCH_LATER_STATE,
                mActivity.MODE_PRIVATE);
        boolean wasWatchLaterClicked = watchLaterState.getBoolean(Constants.WATCH_LATER_ICON_STATE,
                false);
        return wasWatchLaterClicked;
    }

    /**
     * Inflates a popup menu to an imageview with three dots on the cardview.
     * Also has a listener on every item in the popup menu.
     *
     * @param view the view that needs to be inflated.
     */
    private void showPopupMenu(final View view) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        //We select the view that will be inflated with a menu
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        //we inflate the view with a menu
        menuInflater.inflate(R.menu.popup_menu, popupMenu.getMenu());


        MenuItem menuItemRemove = popupMenu.getMenu().findItem(R.id.remove);
        SpannableString removeMenuItem = new SpannableString(Constants.MENU_TITLE_REMOVE);
        removeMenuItem.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, removeMenuItem.length(), 0);
        menuItemRemove.setTitle(removeMenuItem);

        MenuItem menuItemAddPlaylist = popupMenu.getMenu().findItem(R.id.add_to_playlst);
        SpannableString addPlaylist = new SpannableString(Constants.MENU_TITLE_ADD_PLAYLIST);
        addPlaylist.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, addPlaylist.length(), 0);
        menuItemAddPlaylist.setTitle(addPlaylist);

        MenuItem menuItemShare = popupMenu.getMenu().findItem(R.id.share);
        SpannableString shareMenuItem = new SpannableString(Constants.MENU_TITLE_SHARE);
        shareMenuItem.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, shareMenuItem.length(), 0);
        menuItemShare.setTitle(shareMenuItem);


        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.remove:
                        Toast.makeText(view.getContext(), "Remove", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.add_to_playlst:
                        Toast.makeText(view.getContext(), "Add to playlist", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.share:
                        Toast.makeText(view.getContext(), "Share", Toast.LENGTH_SHORT).show();
                        return true;
                    default: {
                        Toast.makeText(view.getContext(), Constants.SELECTING_NAVIGATION_VIEW_ITEMS_ERROR,
                                Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });
    }

    /**
     * This method is used to verify the length of a video title and if it does not fit in one line,
     * it will shrink the title putting 3 dots between the first characters of the title and
     * the video format
     *
     * @param holder we receive a viewholder object so we can set the title to a specific view
     * @param cursor the cursor that will receive the original title from the file
     */
    private void editLongTitle(ViewHolder holder, Cursor cursor) {
        String firstPartOfTitle;
        String secondPartOfTitle;
        String entireTitle;
        if (cursor.getString(1).length() > 21) {
            firstPartOfTitle = cursor.getString(1).substring(0, 14);
            secondPartOfTitle = cursor.getString(1).substring(cursor.getString(1).length() - 4, cursor.getString(1).length());
            entireTitle = firstPartOfTitle + "... " + secondPartOfTitle;
            holder.mTitle.setText(entireTitle);
        } else {
            holder.mTitle.setText(cursor.getString(1));
        }
    }

    @Override
    public int getItemCount() {
        return (mCursor != null) ? mCursor.getCount() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mThumb;
        private TextView mTitle;
        private ImageView mOptionMenu;
        private ImageView mWatchLater;

        public ViewHolder(View itemView) {
            super(itemView);
            mThumb = (ImageView) itemView.findViewById(R.id.cardview_image_thumbnail);
            /*
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onVideoSelected(getAdapterPosition());
                    }
                }
            });*/
            mThumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onVideoSelected(getAdapterPosition());
                    }
                }
            });


            mTitle = (TextView) itemView.findViewById(R.id.cardview_video_title);
            mOptionMenu = (ImageView) itemView.findViewById(R.id.more_options);
            mWatchLater = (ImageView)itemView.findViewById(R.id.watch_later);
            mOptionMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupMenu(v);
                }
            });


        }
    }



    public Cursor getmCursor() {
        return mCursor;
    }

    public void swapCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        mModelList.clear();
        if (mCursor != null) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                VideoModel videoModel = new VideoModel();
                videoModel.setmTitle(cursor.getString(1));
                videoModel.setmUri(Uri.parse(cursor.getString(0)));
                mModelList.add(videoModel);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * In this method we get the uri of a video and bind an image from the video as thumbnail
     * to an imageView.
     *
     * @param imageView The image view which will hold the thumbnail
     * @param position  the position in the list of videos.
     */
    public void setUpVideoThumbnail(ImageView imageView, int position, List<VideoModel> videoList) {
        String thumbnailUrl;
        thumbnailUrl = videoList.get(position).getmUri().toString();

        if (!TextUtils.isEmpty(thumbnailUrl)) {
            Glide.with(imageView.getContext())
                    .load(thumbnailUrl)
//                                .placeholder(R.drawable.ic_placeholder)
//                                .error(R.drawable.ic_error)
                    .into(imageView);
        }
    }

    /**
     * This method is used to set up thumbnails fro the videos using a cursoor
     *
     * @param imageView the imageview to which we store the thumbnail
     * @param cursor    the cursor that we use to get to the video from which we get the image from
     */
    public void setUpVideoThumbnail(ImageView imageView, Cursor cursor) {
        String thumbnailUrl;
        VideoModel videoModel = new VideoModel();
        //thumbnailUrl = videoList.get(position).getmUri().toString();
        videoModel.setmUri(Uri.parse(cursor.getString(0)));
        thumbnailUrl = videoModel.getmUri().toString();

        if (!TextUtils.isEmpty(thumbnailUrl)) {
            Glide.with(imageView.getContext())
                    .load(thumbnailUrl)
//                                .placeholder(R.drawable.ic_placeholder)
//                                .error(R.drawable.ic_error)
                    .into(imageView);
        }
    }

    /**
     * In this method we set the thumbnail for a specific imageview
     *
     * @param imageView the imageview for which we set the thumbnail
     * @param videoUri  the uri of the video from which we extract the thumbnail from
     */
    public void setUpVideoThumbnailFromUri(ImageView imageView, Uri videoUri) {
        String thumbnailUrl;
        thumbnailUrl = videoUri.toString();

        if (!TextUtils.isEmpty(thumbnailUrl)) {
            Glide.with(imageView.getContext())
                    .load(thumbnailUrl)
//                                .placeholder(R.drawable.ic_placeholder)
//                                .error(R.drawable.ic_error)
                    .into(imageView);

        }
    }

    @NonNull
    public List<VideoModel> getmModelList() {
        return mModelList;
    }
}
