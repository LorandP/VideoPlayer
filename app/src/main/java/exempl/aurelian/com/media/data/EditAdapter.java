package exempl.aurelian.com.media.data;

import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

import exempl.aurelian.com.media.R;
import exempl.aurelian.com.media.data.model.VideoModel;

/**
 * Created by Aurelian Cotuna on 8/1/17.
 * In this adapter we handle every video individually
 */

public class EditAdapter extends RecyclerView.Adapter<EditAdapter.ViewHolder> {
    private List<VideoModel> mItems;
    private VideoAdapter mVideoAdapter;
    private VideoListener mVideoListener;

    public EditAdapter(List<VideoModel> items) {mItems = items;}
    public EditAdapter(List<VideoModel> items, VideoListener videoListener) {
        mItems = items;
        this.mVideoListener = videoListener;
    }

    public interface VideoListener{
        void onVideoSelected(int position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_list_row, parent, false);
        return new EditAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        VideoModel model = mItems.get(position);
        holder.mTitle.setText(model.getmTitle());
        holder.mAppCompatCheckedBox.setChecked(model.ismChecked());
        holder.mAppCompatCheckedBox.setVisibility(View.VISIBLE);
        holder.mAppCompatCheckedBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mItems.get(position).setmChecked(isChecked);
            }
        });
        mVideoAdapter = new VideoAdapter();
        mVideoAdapter.setUpVideoThumbnail(holder.mThumb, position, getModelList());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void updateItems(List<VideoModel> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    public String getSelectedItems() {
        StringBuilder builder = new StringBuilder();
        for (VideoModel model : mItems) {
            if (model.ismChecked()) {
                builder.append(model.getmUri());
                builder.append(";");
            }
        }
        return builder.toString();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mThumb;
        private TextView mTitle;
        private AppCompatCheckBox mAppCompatCheckedBox;
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
            mThumb = (ImageView) itemView.findViewById(R.id.cw_video_image_view);
            mTitle = (TextView) itemView.findViewById(R.id.cw_playlist_video_title);
            mAppCompatCheckedBox = (AppCompatCheckBox) itemView.findViewById(R.id.checkbox);
        }
    }

    public List<VideoModel> getModelList(){
        return mItems;
    }
}
