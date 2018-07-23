package exempl.aurelian.com.media.data.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Aurelian Cotuna on 8/2/17.
 */
@Dao
public interface PlayListDao {
    @Query("Select * FROM playlist WHERE name=(:name)")
    PlayList getPlayListByName(String name);

    @Query("Select * FROM playlist")
    List<PlayList> getAll();

    @Delete
    void delete(PlayList playList);

    @Insert
    void insert(PlayList playList);

    @Update
    void updatePlayList(PlayList playList);
}
