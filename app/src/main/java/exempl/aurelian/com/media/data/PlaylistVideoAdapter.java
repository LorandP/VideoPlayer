package exempl.aurelian.com.media.data;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import exempl.aurelian.com.media.R;
import exempl.aurelian.com.media.data.model.VideoModel;

/**
 * Created by Hermes on 21/08/2017.
 * In this activity we handle all the videos from within a playlist
 */

public class PlaylistVideoAdapter extends RecyclerView.Adapter<PlaylistVideoAdapter.ViewHolder> {
    private VideoListener mVideoListener;
    private List<VideoModel> mModelList;
    private VideoAdapter mVideoAdapter;

    public interface VideoListener{
        void onVideoSelected(int position);
    }

    public PlaylistVideoAdapter(List<VideoModel> mModelList, VideoListener mVideoListener) {
        this.mModelList = mModelList;
        this.mVideoListener = mVideoListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VideoModel videoModel = mModelList.get(position);
        editLongTitle(holder, videoModel);
        mVideoAdapter = new VideoAdapter();
        mVideoAdapter.setUpVideoThumbnail(holder.mThumbnail, position, mModelList);
    }

    /**
     * This method is used to verify the length of a video title and if it does not fit in one line,
     * it will shrink the title putting 3 dots between the first characters of the title and
     * the video format
     * @param holder we receive a viewholder object so we can set the title to a specific view
     * @param videoModel a Videomodel object that holds the title of a specific video
     */
    private void editLongTitle(ViewHolder holder, VideoModel videoModel){
        String firstPartOfTitle;
        String secondPartOfTitle;
        String entireTitle;
        if (videoModel.getmTitle().length() > 21) {
            firstPartOfTitle = videoModel.getmTitle().substring(0, 14);
            secondPartOfTitle = videoModel.getmTitle().substring(videoModel.getmTitle().length()-4, videoModel.getmTitle().length());
            entireTitle = firstPartOfTitle + "... " + secondPartOfTitle;
            holder.mTitle.setText(entireTitle);
        } else {
            holder.mTitle.setText(videoModel.getmTitle());
        }
    }

    @Override
    public int getItemCount() {
        return mModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView mThumbnail;
        private TextView mTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mVideoListener != null){
                        mVideoListener.onVideoSelected(getAdapterPosition());
                    }
                }
            });
            mThumbnail = (ImageView)itemView.findViewById(R.id.cardview_image_thumbnail);
            mTitle = (TextView) itemView.findViewById(R.id.cardview_video_title);
        }
    }
    public List<VideoModel> getModelList(){
        return mModelList;
    }
}
