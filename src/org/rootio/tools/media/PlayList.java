package org.rootio.tools.media;

public class PlayList {
	private String[] tags;
	private Media[] mediaList;

	public PlayList() {

	}
	
	private void loadMedia()
	{
		
	}

	public Media[] getMedia() {
		return this.mediaList;
	}

	public String[] getTags() {
		return this.tags;
	}
}
