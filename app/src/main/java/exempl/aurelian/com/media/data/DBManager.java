package exempl.aurelian.com.media.data;

import android.arch.persistence.room.Room;
import android.content.Context;

/**
 * Created by Aurelian Cotuna on 8/2/17.
 */

public class DBManager {
    private static DBManager instance;
    AppDatabase mAppDatabase;

    private DBManager() {
    }

    public static DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
        }
        return instance;
    }

    public AppDatabase getAppDataBase(Context context) {
        if (mAppDatabase == null) {
            mAppDatabase = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, Constants.MEDIA).allowMainThreadQueries().build();
        }
        return mAppDatabase;
    }


}
