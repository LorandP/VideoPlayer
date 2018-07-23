package exempl.aurelian.com.media.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import exempl.aurelian.com.media.data.model.PlayList;
import exempl.aurelian.com.media.data.model.PlayListDao;

/**
 * Created by Aurelian Cotuna on 8/2/17.
 */

@Database(entities = {PlayList.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PlayListDao playListDao();
}
