package org.rootio.tools.media;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;
import android.content.ContentValues;

/**
 * Describes an artist associated with Media
 * 
 * @author UTL051109
 */
public class Artist {
	private String name;
	private String country;
	private String wiki;
	private Long id;

	/**
	 * Constructor for case when only name is known
	 * 
	 * @param name
	 *            The name of the artist
	 */
	public Artist(String name) {
		this(name, null, null);
	}

	/**
	 * Constructor for when the name of the artist is known as well as the
	 * country of the artist
	 * 
	 * @param name
	 *            The name of the artist
	 * @param country
	 *            The country of the artist
	 */
	public Artist(String name, String country) {
		this(name, country, null);
	}

	/**
	 * Constructor for when the name, country and wiki of the artist are known
	 * 
	 * @param name
	 *            The name of the artist
	 * @param country
	 *            The country of the artist
	 * @param wiki
	 *            The URL to a wiki with information for this artist
	 */
	public Artist(String name, String country, String wiki) {
		this.name = name;
		this.country = country;
		this.wiki = wiki;
		this.id = Utils.getCountryId(name);
		if (this.id == null) {
			this.id = this.persist();
		}
	}

	/**
	 * Gets the name of this artist
	 * 
	 * @return String representation of the name of the artist
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the country of this artist
	 * 
	 * @return String representation of the country of the artist
	 */
	public String getCountry() {
		return this.country;
	}

	/**
	 * Returns a URL to a wikipedia page for this artist
	 * 
	 * @return string representation of URL with wiki information for the artist
	 */
	public String getWiki() {
		return this.wiki;
	}

	/**
	 * Returns the ID associated with this artist in the Rootio Database
	 * 
	 * @return
	 */
	public long getId() {
		return this.id;
	}

	/**
	 * Saves the artist to the database if the artist does not yet exist
	 * 
	 * @return id of the record added to the artist table in the database
	 */
	public long persist() {
		String tableName = "artist";
		ContentValues data = new ContentValues();
		data.put("title", this.name);
		data.put("wiki", this.wiki);
		data.put("countryid", Utils.getCountryId(this.country));
		DBAgent dbagent = new DBAgent();
		return dbagent.saveData(tableName, null, data);
	}
}
