package exempl.aurelian.com.media.data;

/**
 * Created by Hermes on 29/08/2017.
 * This interface is used to let the fragment which holds the first video in the list of playlists
 * know that a video from the playlist was played.
 * This functionality is used in order to stop the first video from playling after we return from
 * a video form the playlist.
 */

public interface VideoFromPlaylistWasPlaying {
    boolean videoFromPlaylistWasPLaying();
}
