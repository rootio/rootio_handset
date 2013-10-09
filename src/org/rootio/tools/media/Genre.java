package org.rootio.tools.media;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;
import android.content.ContentValues;

/**
 * Describes genre associated with Media
 * @author UTL051109
 *
 */
public class Genre {
	private String name;
	private Long id;

	/**
	 * Constructor for the genre class
	 * @param name The name of the genre
	 */
	public Genre(String name) {
		this.name = name;
		this.id = Utils.getGenreId(name);
		if (this.id == null) {
			this.id = this.persist();
		}

	}

	/**
	 * Returns the name of the Genre
	 * @return String representation of the name of the Genre
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the Id of this genre as stored in the Rootio Database
	 * @return Long Id of this genre
	 */
	public long getId() {
		return this.id;
	}

	/**
	 * Stores the genre in the database in case it is not yet stored
	 * @return Long rowid of the row stored in the Rootio database 
	 */
	private Long persist() {
		String tableName = "artist";
		ContentValues data = new ContentValues();
		data.put("title", this.name);
		DBAgent dbagent = new DBAgent();
		return dbagent.saveData(tableName, null, data);
	}
}
