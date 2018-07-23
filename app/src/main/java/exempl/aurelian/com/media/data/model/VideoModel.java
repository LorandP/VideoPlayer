package exempl.aurelian.com.media.data.model;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Aurelian Cotuna on 8/1/17.
 */

public class VideoModel implements Parcelable {
    private String mTitle;
    private Uri mUri;
    private boolean mChecked;

    public VideoModel() {

    }

    public VideoModel(Cursor cursor) {
        mTitle = cursor.getString(1);
        mUri = Uri.parse(cursor.getString(0));
        mChecked = false;
    }

    protected VideoModel(Parcel in) {
        mTitle = in.readString();
        mUri = in.readParcelable(Uri.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeParcelable(mUri, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VideoModel> CREATOR = new Creator<VideoModel>() {
        @Override
        public VideoModel createFromParcel(Parcel in) {
            return new VideoModel(in);
        }

        @Override
        public VideoModel[] newArray(int size) {
            return new VideoModel[size];
        }
    };

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public Uri getmUri() {
        return mUri;
    }

    public void setmUri(Uri mUri) {
        this.mUri = mUri;
    }

    public boolean ismChecked() {
        return mChecked;
    }

    public void setmChecked(boolean mChecked) {
        this.mChecked = mChecked;
    }
}
