package org.rootio.tools.media;

public class Media {
	private String title;
	private int duration;
	private byte[] content;
	private Genre[] genre;
	private String[] tags;
	private Artist[] artists;
	
	public Media()
	{
		
	}
	
	public Artist[] getArtists()
	{
		return this.artists;
	}
	
	public Genre[] getGenre()
	{
		return this.genre;
	}
	
	public byte[] getContent()
	{
		return this.content;
	}
	
	public String[] getTags()
	{
		return this.tags;
	}
	
	public int getDuration()
	{
		return this.duration;
	}
	
	public String getTitle()
	{
		return this.title;
	}
	
	

}
