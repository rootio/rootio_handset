package org.rootio.tools.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DBAgent {
	private String databaseName;
	private String userName;
	private String password;
	private String host;
	private int port;

	public DBAgent() {

	}

	/**
	 * Gets a database connection to the specified database
	 * @return Database connection to the specified database
	 */
	private SQLiteDatabase getDBConnection(String databaseName, CursorFactory factory, int flag) {
		return SQLiteDatabase.openDatabase(databaseName, null, flag);
	}

	public String[][] getData(boolean distinct, String tableName, String[] columns, String filter, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
		SQLiteDatabase database = this.getDBConnection(this.databaseName, null, SQLiteDatabase.OPEN_READONLY);
		Cursor cursor = database.query(distinct, tableName, columns, filter, selectionArgs, groupBy, having, orderBy, limit);
		String[][]data = new String[cursor.getCount()][cursor.getColumnCount()];
		for(int i = 0; i < cursor.getCount(); i++)
		{
			for(int j = 0; j < cursor.getColumnCount(); j++)
			{
				data[i][j] = cursor.getString(j);
			}
			cursor.moveToNext();
		}
		return data;
	}

	/**
	 * Saves a row to the Database
	 * @param tableName The name of the table to which to save the data
	 * @param nullColumnHack The column in which to insert a null in case of an empty row
	 * @param data Map of column names and column values to be inserted into the specified table
	 * @return The row id of the inserted row
	 */
	public long saveData(String tableName, String nullColumnHack, ContentValues data) {
		SQLiteDatabase database = this.getDBConnection(this.databaseName, null, SQLiteDatabase.OPEN_READWRITE);
		return database.insert(tableName, nullColumnHack, data);
	}

}
