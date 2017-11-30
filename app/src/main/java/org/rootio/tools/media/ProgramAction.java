package org.rootio.tools.media;

import android.content.Context;

import java.util.ArrayList;

public class ProgramAction {
    private int duration;
    private ArrayList<String> playlists, streams;
    private ProgramActionType programActionType;
    private Context parent;
    private PlayList playlist;
    private JingleManager jingleManager;

    public ProgramAction(Context parent, ArrayList<String> playlists, ArrayList<String> streams, ProgramActionType programType) {
        this.parent = parent;
        this.playlists = playlists;
        this.streams = streams;
        this.programActionType = programType;
    }

    void run() {
        switch (this.programActionType) {
            case Media:
            case Audio:
                this.playlist = PlayList.getInstance();
                this.playlist.init(this.parent, this.playlists, this.streams, this.programActionType);
                this.playlist.load();
                this.playlist.play();
                break;
            case Jingle:
                this.jingleManager = new JingleManager(this.parent, null);
                this.jingleManager.playJingle();
                break;
            case Outcall:
                break;
            default:
                break;
        }
    }

    void resume() {
        switch (this.programActionType) {
            case Media:
            case Audio:
                this.playlist.resume();
                break;
            case Jingle:
                this.jingleManager.play();
                break;
            case Outcall:
                break;
            default:
                break;
        }
    }

    void play() {
        switch (this.programActionType) {
            case Media:
            case Audio:
                this.playlist.play();
                break;
            case Jingle:
                this.jingleManager.play();
                break;
            case Outcall:
                break;
            default:
                break;
        }
    }

    void pause() {
        switch (this.programActionType) {
            case Media:
            case Audio:
                this.playlist.pause();
                break;
            case Jingle:
                this.jingleManager.pause();
                break;
            case Outcall:
                this.playlist.pause();
                break;
            default:
                break;
        }
    }

    void stop() {
        switch (this.programActionType) {
            case Media:
            case Audio:
                this.playlist.stop();
                break;
            case Jingle:
                this.jingleManager.stop();
                break;
            case Outcall:
                break;
            default:
                break;
        }
    }

    public int getDuration() {
        return this.duration;
    }

    public ProgramActionType getProgramType() {
        return this.programActionType;
    }

    public PlayList getPlayList() {
        return this.getPlaylist();
    }

    public PlayList getPlaylist() {
        return playlist;
    }

    public void setPlaylist(PlayList playlist) {
        this.playlist = playlist;
    }

    public JingleManager getJingleManager() {
        return jingleManager;
    }

    public void setJingleManager(JingleManager jingleManager) {
        this.jingleManager = jingleManager;
    }

}
