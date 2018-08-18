package org.rootio.tools.media;

import android.content.Context;

import java.util.ArrayList;

public class ProgramAction {
    private int duration;
    private ArrayList<String> playlists, streams;
    private ProgramActionType programActionType;
    private Context parent;
    private PlayList playlist;

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
                this.playlist.pause(false);
                break;
            case Jingle:
                break;
            case Outcall:
                this.playlist.pause(false);
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
}
