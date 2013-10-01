package org.rootio.tools.media;

import org.rootio.tools.utils.Country;

public class Artist {
	private String name;
	private ArtistType artistType;
	private Country country;
	private String wiki;

	public Artist() {

	}
	
	public String getName()
	{
		return this.name;
	}
	
	public ArtistType getArtistType()
	{
		return this.artistType;
	}
	
	public Country getCountry()
	{
		return this.country;
	}
	
	public String getWiki()
	{
		return this.wiki;
	}
}
