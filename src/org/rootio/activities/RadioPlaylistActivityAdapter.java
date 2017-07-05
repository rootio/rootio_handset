package org.rootio.activities;

import org.rootio.radioClient.R;
import org.rootio.tools.media.Artist;
import org.rootio.tools.media.Media;
import org.rootio.tools.media.PlayList;
import org.rootio.tools.media.Program;
import org.rootio.tools.utils.Utils;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RadioPlaylistActivityAdapter extends BaseAdapter {

	private Program program;
	private Media[] media;
	
	RadioPlaylistActivityAdapter(Program program)
	{
		this.program = program;
		PlayList playList = this.program.getPlayList();
		playList.load();
		if(program != null)
		{
		this.media = playList.getMedia().toArray(new Media[this.program.getPlayList().getMedia().size()]);
		}
		else
		{
			Utils.toastOnScreen("The program is null");
		}
	}
	
	@Override
	public int getCount() {
		return this.media.length;
	}

	@Override
	public Object getItem(int index) {
		return this.media[index];
		
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int index, View view, ViewGroup arg2) {
		if(view == null)
		{
			LayoutInflater layoutInflater = LayoutInflater.from(arg2.getContext());
			view = layoutInflater.inflate(R.layout.station_activity_playlist_row, arg2, false);
		}
		
		//set the Song title
		TextView songTitleTextView = (TextView)view.findViewById(R.id.radio_playList_activity_songTitle_tv);
		songTitleTextView.setText(this.media[index].getTitle());
		
		//render the song artists
		TextView artistsTextView = (TextView)view.findViewById(R.id.radio_playList_activity_artists_tv);
		artistsTextView.setText(this.getSongArtists(media[index]));
        return view;
	}
	
	/**
	 * Returns a string representation of the artist(s) for the specified media
	 * @param media The media for which artist information is desired
	 * @return Comma separated list of names of artists for the media
	 */
	private String getSongArtists(Media media)
	{
		StringBuilder mediaArtists = new StringBuilder();
		for(Artist artist : media.getArtists())
		{
			mediaArtists.append(String.format("%s, ", artist.getName()));
		}
		return mediaArtists.length() > 2? mediaArtists.substring(0, mediaArtists.length() - 2) : mediaArtists.toString();
	}

}
