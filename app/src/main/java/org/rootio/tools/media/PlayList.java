package org.rootio.tools.media;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import org.rootio.handset.BuildConfig;
import org.rootio.handset.R;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Class for the definition of Playlists
 *
 * @author Jude Mukundane git
 */
public class PlayList implements Player.EventListener {

    private ProgramActionType programActionType;
    private ArrayList<String> playlists, streams;
    private HashSet<Media> mediaList;
    private Iterator<Media> mediaIterator;
    private Iterator<String> streamIterator;
    private SimpleExoPlayer mediaPlayer, callSignPlayer;
    private CallSignProvider callSignProvider;
    private Context parent;
    private Media currentMedia;
    private long mediaPosition;
    private static PlayList playListInstance;
    private MediaLibrary mediaLib;
    private boolean isShuttingDown;


    protected PlayList() {
        // do not instantiate
    }

    public void init(Context parent, ArrayList<String> playlists, ArrayList<String> streams, ProgramActionType programActionType) {
        this.isShuttingDown = false;
        this.playlists = playlists;
        this.streams = streams;
        this.parent = parent;
        this.mediaLib = new MediaLibrary(this.parent);
        this.programActionType = programActionType;
        this.callSignProvider = new CallSignProvider();
    }

    public static PlayList getInstance() {
        if (PlayList.playListInstance != null) {
            PlayList.playListInstance.stop();
        }
        PlayList.playListInstance = new PlayList();
        return playListInstance;
    }

     /**
     * Load media for this playlist from the database
     */
    public void load() {
        mediaList = loadMedia(this.playlists);
        mediaIterator = mediaList.iterator();
        streamIterator = streams.iterator();
    }

    /**
     * Play the media loaded in this playlist
     */
    public void play() {
        startPlayer();
        this.callSignProvider.start();
    }

    private void startPlayer() {

        AudioManager audioManager = (AudioManager) this.parent.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) - (BuildConfig.DEBUG ? 7 : 2), AudioManager.FLAG_SHOW_UI);

        //First streams, then audio
        try {

            if (streamIterator.hasNext()) {
                String stream = this.streamIterator.next();
                mediaPlayer = ExoPlayerFactory.newSimpleInstance(this.parent, new DefaultTrackSelector()); //.n.newInstance();// MediaPlayer.create(this.parent, Uri.parse(sng));
                mediaPlayer.addListener(this);
                mediaPlayer.setPlayWhenReady(true);
                mediaPlayer.prepare(this.getMediaSource(Uri.parse(stream)));


            } else if (mediaIterator.hasNext()) {

                currentMedia = mediaIterator.next();
                try {

                    playMedia(Uri.fromFile(new File(currentMedia.getFileLocation())));

                } catch (NullPointerException ex) {
                    Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.startPlayer)" : ex.getMessage());
                    this.startPlayer();
                }
            } else {
                 if (mediaList.size() > 0) // reload playlist if only there were songs in it
                // were some songs in it
                {
                    this.load();
                    this.startPlayer();
                }
            }


        } catch (Exception ex) {

            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.play)" : ex.getMessage());
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.startPlayer();
        }
    }

    private void playMedia(Uri uri) {
        this.playMedia(uri, 0l);
    }

    private void playMedia(Uri uri, long seekPosition) {
        //begin by raising the volume
        AudioManager audioManager = (AudioManager) this.parent.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) - 2, AudioManager.FLAG_SHOW_UI);

        mediaPlayer = ExoPlayerFactory.newSimpleInstance(this.parent, new DefaultTrackSelector());
        mediaPlayer.setVolume(1f); //this is the volume of the individual player, not the music service of the phone
        mediaPlayer.addListener(this);
        mediaPlayer.prepare(this.getMediaSource(uri));
        mediaPlayer.setPlayWhenReady(true);
        mediaPlayer.seekTo(seekPosition);
    }


    private MediaSource getMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(this.parent, "rootio")).createMediaSource(uri);
    }


    /**
     * Stops the media player and disposes it.
     */
    public void stop() {
        this.isShuttingDown = true; //async nature of stuff means stuff can be called in the middle of shutdown. This flag shd be inspected...
        if (this.callSignProvider != null) {
            this.callSignProvider.stop();
            if (this.callSignPlayer != null)
                try {
                    this.callSignPlayer.stop();
                    this.callSignPlayer.release();
                } catch (Exception ex) {
                    Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.stop)" : ex.getMessage());
                }

            if (mediaPlayer != null) {
                try {
                    this.fadeOut();
                    mediaPlayer.stop();
                    mediaPlayer.removeListener(this);
                    mediaPlayer.release();
                } catch (Exception ex) {
                    Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.stop)" : ex.getMessage());
                }
            }
        }
    }

    private void fadeOut() {
        float volume = 1.0F;
        while (volume > 0) {
            volume = volume - 0.02F;
            mediaPlayer.setVolume(volume);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Pauses the currently playing media
     */
    public void pause() {
        try {
             if (mediaPlayer.getPlaybackState() == Player.STATE_READY) {
                this.mediaPosition = this.mediaPlayer.getCurrentPosition();
                mediaPlayer.stop(true); //advised that media players should never be reused, even in pause/play scenarios
                mediaPlayer.release();

                //stop the call sign player as well
                this.callSignPlayer.stop(true);
                this.callSignPlayer.release();
            }
        } catch (Exception ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.pause)" : ex.getMessage());
        }
    }

    /**
     * Resumes playback after it has been paused
     */
    public void resume() {
        try {
            this.playMedia(Uri.fromFile(new File(this.currentMedia.getFileLocation())), this.mediaPosition);

            // resume the callSign provider
            this.callSignProvider.start();

        } catch (Exception ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.resume)" : ex.getMessage());
        }
    }


    private HashSet<Media> loadMedia(ArrayList<String> playlists) {
        HashSet<Media> media = new HashSet<>();
        for (String playlist : playlists) {
             String query = "select title, item, itemtypeid from playlist where title = ?";
            String[] args = new String[]{playlist};
            DBAgent dbagent = new DBAgent(this.parent);
            String[][] data = dbagent.getData(query, args);
            for (int i = 0; i < data.length; i++) {

                if (playlist.equals("jingle")) {
                    //Utils.toastOnScreen(data[i][2], this.parent);
                }
                if (data[i][2].equals("1"))// songs
                {
                    media.add(this.mediaLib.getMedia(data[i][1]));
                } else if (data[i][2].equals("2"))// albums
                {
                    media.addAll(this.mediaLib.getMediaForAlbum(data[i][1]));
                } else if (data[i][2].equals("3"))// artists
                {
                    media.addAll(this.mediaLib.getMediaForArtist(data[i][1]));
                }
            }
        }

        return media;
    }


    /**
     * Gets the media currently loaded in this playlist
     *
     * @return Array of Media objects loaded in this playlist
     */
    public HashSet<Media> getMedia() {
        return this.mediaList;
    }


    void onReceiveCallSign(String Url) {

        try {

            callSignPlayer = ExoPlayerFactory.newSimpleInstance(this.parent, new DefaultTrackSelector());
            callSignPlayer.addListener(new Player.EventListener() {


                @Override
                public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

                }

                @Override
                public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

                }

                @Override
                public void onLoadingChanged(boolean isLoading) {

                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                    switch (playbackState) {
                        case Player.STATE_ENDED:
                            try {
                                PlayList.this.mediaPlayer.setVolume(1.0f);
                                callSignPlayer.removeListener(this);
                                callSignPlayer.release();
                            } catch (Exception ex) {
                                Log.e(PlayList.this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.onCompletion)" : ex.getMessage());

                            }
                            break;
                    }
                }

                @Override
                public void onRepeatModeChanged(int repeatMode) {

                }

                @Override
                public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {

                }

                @Override
                public void onPositionDiscontinuity(int reason) {

                }

                @Override
                public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

                }

                @Override
                public void onSeekProcessed() {

                }
            });
            this.mediaPlayer.setVolume(0.07f);
            callSignPlayer.setVolume(1.0f);
            callSignPlayer.prepare(this.getMediaSource(Uri.fromFile(new File(Url))));
            this.callSignPlayer.setPlayWhenReady(true);
        } catch (Exception ex) {
            Log.e(PlayList.this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.onReceiveCallSign)" : ex.getMessage());
        }
    }

    @Override
    protected void finalize() {
        this.stop();
        try {
            super.finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_READY:
                try {
                    if (this.callSignPlayer != null || this.callSignPlayer.getPlaybackState() == Player.STATE_READY) {
                        this.mediaPlayer.setVolume(0.07f);
                    } else {
                        this.mediaPlayer.setVolume(1f);
                    }

                } catch (Exception ex) {
                    this.mediaPlayer.setVolume(1f);
                }
                break;
            case Player.STATE_ENDED: //a song has ended
                if (this.isShuttingDown) {
                    return;
                }
                try {
                     mediaPlayer.release();
                } catch (Exception ex) {
                }
                //this.load();
                this.startPlayer();
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    class CallSignProvider implements Runnable {

        private final HashSet<Media> callSigns;
        Iterator<Media> mediaIterator;
        private boolean isRunning;

        CallSignProvider() {
            ArrayList jingles = new ArrayList<String>();
            jingles.add("jingle");
            this.callSigns = PlayList.this.loadMedia(jingles);
            this.isRunning = false;
        }


        @Override
        public void run() {
            this.isRunning = true;

            this.mediaIterator = callSigns.iterator();
            while (this.isRunning) {
                try {
                    this.playCallSign();
                    Thread.sleep(1200000);// 20 mins
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        void stop() {
            this.isRunning = false;
        }

        private void playCallSign() {
            try {
                if (this.callSigns.size() < 1)
                    return;
                if (!this.mediaIterator.hasNext()) {
                    this.mediaIterator = callSigns.iterator(); // reset the iterator to 0 if at the end
                }
                onReceiveCallSign(mediaIterator.next().getFileLocation());
            } catch (Exception ex) {
                Log.e(PlayList.this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.playCallSign)" : ex.getMessage());
            }
        }

        public void start() {
            new Thread(this).start();

        }
    }

}
