package org.rootio.tools.media;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.rootio.handset.BuildConfig;
import org.rootio.handset.R;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Class for the definition of Playlists
 *
 * @author Jude Mukundane git
 */
public class PlayList implements OnCompletionListener, OnPreparedListener{

    private ProgramActionType programActionType;
    private ArrayList<String> playlists, streams;
    private HashSet<Media> mediaList;
    private Uri streamUrl;
    private Iterator<Media> mediaIterator;
    private Iterator<String> streamIterator;
    private MediaPlayer mediaPlayer;
    private MediaPlayer callSignPlayer;
    private CallSignProvider callSignProvider;
    private Context parent;
    private Media currentMedia;
    private int mediaPosition;
    private static PlayList playListInstance;
    private MediaLibrary mediaLib;
    private BroadcastReceiver playListener;
    private boolean isShuttingDown;

    /**
     * Constructor for the playlist class
     *
     * @param tag The tag to be used to construct the playlist
     * @return
     */

    protected PlayList() {
        // do not instantiate
    }

    public void init(Context parent, ArrayList<String> playlists, ArrayList<String> streams, ProgramActionType programActionType) {
        this.isShuttingDown = false;
        this.playlists = playlists;
        this.streams = streams;
        this.parent = parent;
        this.setUpPlayListener();
        this.mediaLib = new MediaLibrary(this.parent);
        this.programActionType = programActionType;
        this.callSignProvider = new CallSignProvider(this.parent, this);
    }

    public static PlayList getInstance() {
        if (PlayList.playListInstance != null) {
            PlayList.playListInstance.stop();
        }
        PlayList.playListInstance = new PlayList();
        return playListInstance;
    }

    private void setUpPlayListener() {
        this.playListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                PlayList.this.onCompletion(PlayList.this.mediaPlayer);
            }
        };
        IntentFilter fltr = new IntentFilter();
        fltr.addAction("org.rootio.media.playChange");
        this.parent.registerReceiver(this.playListener, fltr);
    }


    /**
     * Load media for this playlist from the database
     */
    public void load() {
        mediaList = loadMedia(this.playlists);
        if (BuildConfig.DEBUG) {
            Utils.toastOnScreen("Playlist has "+ mediaList.size() +" songs in it", this.parent);
        }
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
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) - 2, AudioManager.FLAG_SHOW_UI);

        //First streams, then audio
        try {

            if (streamIterator.hasNext()) {
                String sng = this.streamIterator.next();
                mediaPlayer = MediaPlayer.create(this.parent, Uri.parse(sng));
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnCompletionListener(this);

                mediaPlayer.start();

                //streams break and sometimes do not return. detect this and restart player
                Thread thr = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true)//TODO: Is this gonna cause a memory leak?
                        {
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (!isShuttingDown && !mediaPlayer.isPlaying()) {

                                    Intent intent = new Intent();
                                    intent.setAction("org.rootio.media.playChange");
                                    PlayList.this.parent.sendBroadcast(intent);
                                    break;
                                }
                            } catch (Exception ex) {
                                //?? This loop/thread should only terminate if we the media player is being reset
                            }


                        }
                    }
                });
                thr.start();

            } else if (mediaIterator.hasNext()) {

                currentMedia = mediaIterator.next();
                if (BuildConfig.DEBUG) {
                    if (currentMedia == null) {
                        Utils.toastOnScreen("Current media is null!", this.parent);
                    }
                }
                try {

                   Utils.toastOnScreen("Playing " + currentMedia.getTitle(), this.parent);
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(this.parent, Uri.fromFile(new File(currentMedia.getFileLocation())));
                    mediaPlayer.setOnCompletionListener(this);
                    mediaPlayer.setOnPreparedListener(this);
                    mediaPlayer.prepareAsync();
                } catch (NullPointerException ex) {
                    Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.startPlayer)" : ex.getMessage());
                    this.startPlayer();
                }
            } else {
                if(BuildConfig.DEBUG) Utils.toastOnScreen("nothing on the iterator", this.parent);
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

    @Override
    public void onPrepared(MediaPlayer mp) {
        // check that a callsign is not playing before upping
        // the volume
        try {
            if (this.callSignPlayer != null || this.callSignPlayer.isPlaying()) {
                this.mediaPlayer.setVolume(0.07f, 0.07f);
            } else {
                this.mediaPlayer.setVolume(1f, 1f);
            }

        } catch (Exception ex) {
            this.mediaPlayer.setVolume(1f, 1f);
        }
        mediaPlayer.start();
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
            mediaPlayer.setVolume(volume, volume);
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
            if (mediaPlayer.isPlaying()) {
                this.mediaPosition = this.mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();

                this.callSignPlayer.release();
                this.callSignProvider.stop();
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
            // raise the volume Android levels it after phone call
            AudioManager audioManager = (AudioManager) this.parent.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) - 2, AudioManager.FLAG_SHOW_UI);

            // mediaPlayer.start(); //works fine on Galaxy grand duos (4.2.2),
            // fails
            // on Galaxy pocket (4.0.2) because Media player is reclaimed by
            // system
            this.mediaPlayer = MediaPlayer.create(this.parent, Uri.fromFile(new File(this.currentMedia.getFileLocation())));
            this.mediaPlayer.setOnCompletionListener(this);
            this.mediaPlayer.seekTo(mediaPosition);
            this.mediaPlayer.start();

            // resume the callSign provider
            this.callSignProvider.start();

        } catch (Exception ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.resume)" : ex.getMessage());
        }
    }

    /**
     * Loads media with the specified tag into the playlist
     *
     * @param tag The tag to be matched for media to be loaded into the playlist
     * @return Array of Media objects matching specified tag
     */
    private HashSet<Media> loadMedia(ArrayList<String> playlists) {
        HashSet<Media> media = new HashSet<Media>();
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

    private String getStreamingUrl() {
        String tableName = "streamingConfiguration";
        String[] columns = new String[]{"ipaddress", "port", "path"};
        String orderBy = "id desc";
        String limit = "1";
        DBAgent dbAgent = new DBAgent(this.parent);
        String result[][] = dbAgent.getData(true, tableName, columns, null, null, null, null, orderBy, limit);
        return result.length > 0 ? String.format("http://%s:%s/%s", result[0][0], result[0][1], result[0][2]) : null;
    }

    /**
     * Gets the media currently loaded in this playlist
     *
     * @return Array of Media objects loaded in this playlist
     */
    public HashSet<Media> getMedia() {
        return this.mediaList;
    }

    /**
     * Gets the tag used to construct this playlist
     *
     * @return String representation of the tag used to construct this playlist
     */

    void onReceiveCallSign(String Url) {

        try {
            //if (this.mediaPlayer != null && this.mediaPlayer.isPlaying()) {
            try {
                callSignPlayer = MediaPlayer.create(this.parent, Uri.fromFile(new File(Url)));
                if (callSignPlayer == null) {
                    return;
                }
            } catch (Exception ex) {
                Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.onReceiveCallSign)" : ex.getMessage());
                return;
            }

            this.mediaPlayer.setVolume(0.07f, 0.07f);
            callSignPlayer.setVolume(1.0f, 1.0f);
            callSignPlayer.start();
            callSignPlayer.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer arg0) {
                    try {
                        PlayList.this.mediaPlayer.setVolume(1.0f, 1.0f);
                        callSignPlayer.release();
                        callSignPlayer = null;
                    } catch (Exception ex) {
                        Log.e(PlayList.this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.onCompletion)" : ex.getMessage());

                    }
                }
            });
            //}
        } catch (Exception ex) {
            Log.e(PlayList.this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.onReceiveCallSign)" : ex.getMessage());
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (this.isShuttingDown) {
            return;
        }
        try {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        } catch (Exception ex) {
        }
        this.load();
        this.startPlayer();
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

    class CallSignProvider implements Runnable {

        private final HashSet<Media> callSigns;
        Iterator<Media> mediaIterator;
        private boolean isRunning;

        CallSignProvider(Context parent, PlayList playlist) {
            PlayList.this.parent = parent;
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
