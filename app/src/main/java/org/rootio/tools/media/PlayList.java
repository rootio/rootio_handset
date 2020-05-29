package org.rootio.tools.media;

import android.content.Context;
import android.media.AudioManager;
import android.media.session.PlaybackState;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.handset.BuildConfig;
import org.rootio.handset.R;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

/**
 * Class for the definition of Playlists
 *
 * @author Jude Mukundane git
 */
public class PlayList implements Player.EventListener {

    private static PlayList playListInstance;
    private ArrayList<String> playlists, streams;
    private HashSet<Media> mediaList;
    private Iterator<Media> mediaIterator;
    private Iterator<String> streamIterator;
    private SimpleExoPlayer mediaPlayer, callSignPlayer;
    private CallSignProvider callSignProvider;
    private Context parent;
    private Uri currentMediaUri;
    private Media currentMedia, currentCallSign;
    private long mediaPosition;
    private MediaLibrary mediaLib;
    private boolean isShuttingDown;
    private Thread runnerThread;
    private int maxVolume;
    private boolean foundMedia;


    private PlayList() {
        // do not instantiate
    }

    public static PlayList getInstance() {
        if (PlayList.playListInstance != null) {
            PlayList.playListInstance.stop();
        }
        PlayList.playListInstance = new PlayList();
        return playListInstance;
    }

    public void init(Context parent, ArrayList<String> playlists, ArrayList<String> streams, ProgramActionType programActionType) {
        this.isShuttingDown = false;
        this.playlists = playlists;
        this.streams = streams;
        this.parent = parent;
        this.mediaLib = new MediaLibrary(this.parent);
        this.callSignProvider = new CallSignProvider();
    }

    /**
     * Load media for this playlist from the database
     */
    public void load(boolean hard) {
        if (hard || (this.mediaList == null || mediaList.size() == 0)) {
            mediaList = loadMedia(this.playlists);
        }
        mediaIterator = mediaList.iterator();
        streamIterator = streams.listIterator();
    }

    public void preload(){
        if (this.mediaList == null || mediaList.size() == 0) {
            mediaList = preloadMedia(this.playlists);
        }
        mediaIterator = mediaList.iterator();
        streamIterator = streams.iterator();
    }

    /**
     * Play the media loaded in this playlist
     */
    public void play() {
        this.maxVolume = this.getMaxVolume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                startPlayer();
            }
        }).start();
      //  startPlayer();
   this.callSignProvider.start();

    }

    private void startPlayer() {
         final Cloud cloud = new Cloud(this.parent);
        while (!foundMedia && !this.isShuttingDown) {
            try {
                AudioManager audioManager = (AudioManager) this.parent.getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, getMaxVolume() > 9? 9: getMaxVolume(), AudioManager.FLAG_SHOW_UI);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, getMaxVolume(), AudioManager.FLAG_SHOW_UI);

                if (streamIterator.hasNext()) {
                    String stream = this.streamIterator.next();
                    currentMedia = new Media("", stream, 0, null);
                    try {
                        playMedia(Uri.parse(currentMedia.getFileLocation()));
                        this.foundMedia = true;

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.doPostHTTP(String.format("%s://%s:%s/%s/%s/programs?api_key=%s&%s&version=%s_%s", cloud.getServerScheme(), cloud.getServerAddress(), cloud.getHTTPPort(), "api/media_play", cloud.getStationId(), cloud.getServerKey(), currentMedia.getFileLocation(), BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE), "");
                            }
                        }).start();
                    } catch (NullPointerException ex) {
                        Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.startPlayer)" : ex.getMessage());
                        this.startPlayer();
                    }
                } else if (mediaIterator.hasNext()) {
                    if(!streamIterator.hasNext())
                    {
                        streamIterator = streams.iterator();
                    }
                    currentMedia = mediaIterator.next();

                    try {
                        playMedia(Uri.fromFile(new File(currentMedia.getFileLocation())));
                        this.foundMedia = true;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.doPostHTTP(String.format("%s://%s:%s/%s/%s/programs?api_key=%s&%s&version=%s_%s", cloud.getServerScheme(), cloud.getServerAddress(), cloud.getHTTPPort(), "api/media_play", cloud.getStationId(), cloud.getServerKey(), currentMedia.getFileLocation(), BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE), "");
                            }
                        }).start();

                    } catch (NullPointerException ex) {
                        //Log.e(this.parent.getString(R.string.app_name) + " PlayList.startPlayer", ex.getMessage() == null ? "Null pointer exception(PlayList.startPlayer)" : ex.getMessage());
                        this.startPlayer();
                    }
                } else {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {

                    }
                    this.load(false);
                }


            } catch (final Exception ex) {
                try {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.doPostHTTP(String.format("%s://%s:%s/%s/%s/programs?api_key=%s&%s&version=%s_%s", cloud.getServerScheme(), cloud.getServerAddress(), cloud.getHTTPPort(), "api/media_play", cloud.getStationId(), cloud.getServerKey(), ex.getMessage() == null ? "Null pointer exception(PlayList.play)" : ex.getMessage(), BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE), "");
                        }
                    }).start();

                    Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.play)" : ex.getMessage());
            /*try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
                    this.startPlayer();
                } catch (Exception ex1) {

                    Log.e("RootIO", "startPlayer: " + ex1.getMessage() == null ? "Null pointer exception(PlayList.play)" : ex.getMessage());
                }
            }
        }
    }

    private void playMedia(Uri uri) {
        this.playMedia(uri, 0L);
    }

    private void playMedia(Uri uri, long seekPosition) {
        this.currentMediaUri = uri;
        //begin by raising the volume
        AudioManager audioManager = (AudioManager) this.parent.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, getMaxVolume() > 9? 9: getMaxVolume(), AudioManager.FLAG_SHOW_UI);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, getMaxVolume(), AudioManager.FLAG_SHOW_UI);


        mediaPlayer = ExoPlayerFactory.newSimpleInstance(this.parent, new DefaultTrackSelector());
        mediaPlayer.addListener(this);
        mediaPlayer.prepare(this.getMediaSource(uri));

        mediaPlayer.setVolume(BuildConfig.DEBUG ? 0.5F : 0.9F); //this is the volume of the individual player, not the music service of the phone
        mediaPlayer.setPlayWhenReady(true);
        //mediaPlayer.seekTo(seekPosition); this trips streams...

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
            if (this.callSignPlayer != null) {
                try {
                    this.callSignPlayer.stop();
                    this.callSignPlayer.release();
                } catch (Exception ex) {
                    Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.stop)" : ex.getMessage());
                }
            }
            try {

                Utils.logEvent(this.parent, Utils.EventCategory.MEDIA, Utils.EventAction.STOP, String.format("Title: %s, Artist: %s, Location: %s", currentMedia.getTitle(), currentMedia.getArtists(), currentMedia.getFileLocation()));
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
        //fade out in 5 secs
        float step = mediaPlayer.getVolume() / 50;
        while (mediaPlayer.getVolume() > 0) {
            //volume = volume - 0.05F;
            mediaPlayer.setVolume(mediaPlayer.getVolume() - step);
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
    public void pause(boolean soft) {
        try {
            if (mediaPlayer.getPlaybackState() == Player.STATE_READY) {
                if (soft) //typically these are thrown by SIP calls
                {
                    this.fadeOut();

                }

                Utils.logEvent(this.parent, Utils.EventCategory.MEDIA, Utils.EventAction.PAUSE, String.format("Title: %s, Artist: %s, Location: %s", currentMedia.getTitle(), currentMedia.getArtists(), currentMedia.getFileLocation()));

                try {
                    this.mediaPosition = this.mediaPlayer.getCurrentPosition();
                    mediaPlayer.stop(true); //advised that media players should never be reused, even in pause/play scenarios
                    mediaPlayer.release();
                } catch (Exception ex) {
                    Log.e(this.parent.getString(R.string.app_name) + " (PlayList.pause)", ex.getMessage() == null ? "Null pointer exception" : ex.getMessage());
                }

                try {
                    //stop the call sign player as well
                    this.callSignPlayer.stop(true); //this thread is sleeping! TODO: interrupt it
                    this.callSignPlayer.release();
                } catch (Exception ex) {
                    Log.e(this.parent.getString(R.string.app_name) + " (PlayList.pause)", ex.getMessage() == null ? "Null pointer exception" : ex.getMessage());
                }

                //stop the callSign looper so they do not play during the call
                this.callSignProvider.stop();
            }
        } catch (Exception ex) {
            Log.e(this.parent.getString(R.string.app_name) + " (PlayList.pause)", ex.getMessage() == null ? "Null pointer exception" : ex.getMessage());
        }
    }

    /**
     * Resumes playback after it has been paused
     *
     * @deprecated .This method should be called if you want to resume the media that was playing at the time the playlist was paused.
     * THis was deprecated in favor of stopping and restarting the playlist
     * However streams take care of themselves and for songs, another song will be chosen, so no big deal
     */
    @Deprecated

    public void resume() {
        try {
            this.playMedia(this.currentMediaUri, this.mediaPosition);
            try {

                Utils.logEvent(this.parent, Utils.EventCategory.MEDIA, Utils.EventAction.START, String.format("Title: %s, Artist: %s, Location: %s", currentMedia.getTitle(), currentMedia.getArtists(), currentMedia.getFileLocation()));
            } catch (Exception ex) {
                Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.stop)" : ex.getMessage());
            }
            // resume the callSign provider
            this.callSignProvider.start();

        } catch (Exception ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.resume)" : ex.getMessage());
        }
    }

    private HashSet<Media> preloadMedia(ArrayList<String> playlists) {
        Random rand = new Random();
        HashSet<Media> media = new HashSet<>();
        for (String playlist : playlists) {
            String query = "select title, item, itemtypeid from playlist where lower(title) = ?";
            String[] args = new String[]{playlist.toLowerCase()};
            // DBAgent dbagent = new DBAgent(this.parent);
            String[][] data = DBAgent.getData(query, args);
            for (int k = 0; k < 3 && k < data.length; k++) {
                int i = rand.nextInt(data.length);
                switch (data[i][2]) {
                    case "1"://songs
                        media.add(this.mediaLib.getMedia(data[i][1]));
                        break;
                    case "2":// albums
                        media.addAll(this.mediaLib.getMediaForAlbum(data[i][1]));
                        break;
                    case "3":// artists
                        media.addAll(this.mediaLib.getMediaForArtist(data[i][1]));
                        break;
                }
            }
        }

        return media;
    }

    private HashSet<Media> loadMedia(ArrayList<String> playlists) {
        HashSet<Media> media = new HashSet<>();
        for (String playlist : playlists) {
            String query = "select title, item, itemtypeid from playlist where lower(title) = ?";
            String[] args = new String[]{playlist.toLowerCase()};
            // DBAgent dbagent = new DBAgent(this.parent);
            String[][] data = DBAgent.getData(query, args);
            for (String[] record : data) {

                switch (record[2]) {
                    case "1"://songs
                        media.add(this.mediaLib.getMedia(record[1]));
                        break;
                    case "2":// albums
                        media.addAll(this.mediaLib.getMediaForAlbum(record[1]));
                        break;
                    case "3":// artists
                        media.addAll(this.mediaLib.getMediaForArtist(record[1]));
                        break;
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



    private void onReceiveCallSign(String Url) {
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
                                PlayList.this.mediaPlayer.setVolume(BuildConfig.DEBUG ? 0.5F : 0.9F);
                                //Utils.toastOnScreen("logging media...", PlayList.this.parent);
                                try {
                                    Utils.logEvent(PlayList.this.parent, Utils.EventCategory.MEDIA, Utils.EventAction.STOP, String.format("Title: %s, Artist: %s, Location: %s", currentCallSign.getTitle(), currentCallSign.getArtists(), currentCallSign.getFileLocation()));
                                } catch (Exception ex) {
                                    Log.e(PlayList.this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.stop)" : ex.getMessage());
                                }
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
            callSignPlayer.setVolume(BuildConfig.DEBUG ? 0.5F : 0.9F);
            //currentCallSign = Uri.fromFile(new File(Url));
            callSignPlayer.prepare(this.getMediaSource(Uri.fromFile(new File(Url))));
            Utils.toastOnScreen("logging media...", this.parent);
            try {
                Utils.logEvent(PlayList.this.parent, Utils.EventCategory.MEDIA, Utils.EventAction.START, String.format("Title: %s, Artist: %s, Location: %s", currentCallSign.getTitle(), currentCallSign.getArtists(), currentCallSign.getFileLocation()));
            } catch (Exception ex) {
                Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.stop)" : ex.getMessage());
            }
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
                    if (this.callSignPlayer != null && this.callSignPlayer.getPlaybackState() == Player.STATE_READY) {
                        this.mediaPlayer.setVolume(0.07f);
                    } else {
                        this.mediaPlayer.setVolume(BuildConfig.DEBUG ? 0.5F : 0.9F);
                    }
                    //Utils.toastOnScreen("logging media...", this.parent);
                    Utils.logEvent(this.parent, Utils.EventCategory.MEDIA, Utils.EventAction.START, String.format("Title: %s, Artist: %s, Location: %s", currentMedia.getTitle(), currentMedia.getArtists(), currentMedia.getFileLocation()));


                } catch (Exception ex) {

                    this.mediaPlayer.setVolume(BuildConfig.DEBUG ? 0.5F : 0.9F);
                }
                break;
            case Player.STATE_ENDED: //a song has ended
                this.foundMedia = false;
                //Utils.toastOnScreen("logging media...", this.parent);
                Utils.logEvent(this.parent, Utils.EventCategory.MEDIA, Utils.EventAction.STOP, String.format("Title: %s, Artist: %s, Location: %s", currentMedia.getTitle(), currentMedia.getArtists(), currentMedia.getFileLocation()));
                if (this.isShuttingDown) {
                    return;
                }
                try {

                    mediaPlayer.release();

                } catch (Exception ex) {
                    Log.e(PlayList.this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.onPlayerStateChanged)" : ex.getMessage());
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
        //This will be thrown when a stream is lost due to network, or an error in a local song.
        //in both cases, assume song is ended. This will cause loop of player (stream) or skipping to the next song (song list)
        this.onPlayerStateChanged(true, Player.STATE_ENDED);
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
            ArrayList<String> jingles = new ArrayList();
            jingles.add("jingle");
            jingles.add("jingles");
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
                    Thread.sleep(PlayList.this.getJingleInterval());// 2 mins debug, 12 mins release
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        void stop() {
            this.isRunning = false;
            try {
                Utils.logEvent(PlayList.this.parent, Utils.EventCategory.MEDIA, Utils.EventAction.STOP, String.format("Title: %s, Artist: %s, Location: %s", currentCallSign.getTitle(), currentCallSign.getArtists(), currentCallSign.getFileLocation()));
            } catch (Exception ex) {
                Log.e(PlayList.this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.stop)" : ex.getMessage());
            }
            if (PlayList.this.runnerThread != null) {
                PlayList.this.runnerThread.interrupt(); //so the thread can exit on state change, otherwise could sleep through on->off->on
            }
        }

        private void playCallSign() {
            try {
                if (this.callSigns.size() < 1) {
                    return;
                }
                if (!this.mediaIterator.hasNext()) {
                    this.mediaIterator = callSigns.iterator(); // reset the iterator to 0 if at the end
                }
                currentCallSign = mediaIterator.next();
                onReceiveCallSign(currentCallSign.getFileLocation());
            } catch (Exception ex) {
                Log.e(PlayList.this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.playCallSign)" : ex.getMessage());
            }
        }

        public void start() {
            PlayList.this.runnerThread = new Thread(this);
            PlayList.this.runnerThread.start();

        }
    }

    private int getMaxVolume() {
        String stationInfo = (String) Utils.getPreference("station_information", String.class, this.parent);
        try {
            JSONObject stationInfoJson = new JSONObject(stationInfo);
            if (stationInfoJson.has("station") && stationInfoJson.getJSONObject("station").has("media_volume")) {
                int volume = stationInfoJson.getJSONObject("station").getInt("media_volume");
                return volume >= 0 && volume <= 15 ? volume : 8;
            } else
                return 8;
        } catch (Exception ex) {
            Log.e(PlayList.this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.getMaxVolume)" : ex.getMessage());
        }
        return 8;
    }

    private int getJingleInterval() {
        String stationInfo = (String) Utils.getPreference("station_information", String.class, this.parent);
        try {
            JSONObject stationInfoJson = new JSONObject(stationInfo);
            if (stationInfoJson.has("station") && stationInfoJson.getJSONObject("station").has("jingle_interval")) {
                int interval = stationInfoJson.getJSONObject("station").getInt("jingle_interval");
                return interval >= 1 && interval <= 15 ? interval * 1000 : 600000;
            } else
                return 600000;
        } catch (Exception ex) {
            Log.e(PlayList.this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.getJingleInterval)" : ex.getMessage());
        }
        return 600000;
    }

}
