package exempl.aurelian.com.media.data;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import exempl.aurelian.com.media.R;
import exempl.aurelian.com.media.data.model.PlayList;

/**
 * Created by Aurelian Cotuna on 8/2/17.
 * Displaying the list of playlists
 */

public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.ViewHolder> {

    public interface PlayListListener {
        void onPlayListSelected(int position);

        void onEditPlayListSelected(int position);
    }

    private List<PlayList> mItems;
    private PlayListListener mListener;

    public SimpleAdapter(List<PlayList> items, PlayListListener listener) {
        this.mItems = items;
        this.mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //holder.mTitle.setText(mItems.get(position).name);
        String playlstUris;
        playlstUris = mItems.get(position).playList;
        String[] uris = playlstUris.split(";");

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(uris[0]);
        Bitmap thumbnail = mediaMetadataRetriever.getFrameAtTime();
        holder.mPlaylistCover.setImageBitmap(thumbnail);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public List<PlayList> getmItems() {
        return mItems;
    }

    public void updateItems(List<PlayList> items){
        this.mItems = items;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //private TextView mTitle;
        private ImageView mPlaylistCover;

        public ViewHolder(View itemView) {
            super(itemView);
            //mTitle = (TextView) itemView.findViewById(R.id.play_list_name);
            mPlaylistCover = (ImageView)itemView.findViewById(R.id.playlist_cover);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onPlayListSelected(getAdapterPosition());
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListener != null) {
                        mListener.onEditPlayListSelected(getAdapterPosition());
                        return true;
                    }
                    return false;
                }
            });
        }
    }
}
