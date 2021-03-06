package org.rootio.tools.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.rootio.RootioApp;
import org.rootio.handset.R;
import org.rootio.tools.utils.Utils;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class DBAgent {
    private static Context context  = RootioApp.getInstance().getApplicationContext();
    private static String databaseName = context.getFilesDir() + "/rootio.sqlite";
    //private static final Context context;

//    public DBAgent(Context context) {
//        context = context;
   //   databaseName = context.getFilesDir() + "/rootio.sqlite";
    static {
    if (!databaseFileExists()) {
        createDatabaseFile();
    }
}
   // }

    /**
     * Gets a database connection to the specified database
     *
     * @return Database connection to the specified database
     */
    private static synchronized  SQLiteDatabase getDBConnection(String databaseName, CursorFactory factory, int flag) {
        try {
            return SQLiteDatabase.openDatabase(databaseName, null, flag);
        } catch (Exception ex) {
            // db file is corrupt, reinstall db
            // createDatabaseFile();
            // return SQLiteDatabase.openDatabase(databaseName, null, flag);
        }
        return null;
    }

    /**
     * Fetches data from the database according to the criteria specified
     *
     * @param distinct      Boolean indicating whether to return distinct values
     * @param tableName     The name of the table to be queried
     * @param columns       An arrays of the names of columns to be returned
     * @param filter        A string with the where clause of the SQL query
     * @param selectionArgs String array of the values for the parameters in the where
     *                      clause
     * @param groupBy       The Group by clause of the SQL query
     * @param having        The having clause of the SQL query
     * @param orderBy       The Order by clause of the SQL query
     * @param limit         The offset and number of rows to return
     * @return Array of String arrays each representing a record in the database
     */
    public static String[][] getData(boolean distinct, String tableName, String[] columns, String filter, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        SQLiteDatabase database = getDBConnection(databaseName, null, SQLiteDatabase.OPEN_READONLY);
        try {
            Cursor cursor = database.query(distinct, tableName, columns, filter, selectionArgs, groupBy, having, orderBy, limit);
            String[][] data = new String[cursor.getCount()][cursor.getColumnCount()];
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                for (int j = 0; j < cursor.getColumnCount(); j++) {
                    data[i][j] = cursor.getString(j);
                }

            }
            return data;
        } catch (Exception ex) {
            System.err.println(ex);
            return null;
        } finally {
            database.close();
        }
    }

    /**
     * Checks if the database file exists
     *
     * @return Boolean indicating whether or not the database file exists
     */
    private static synchronized  boolean databaseFileExists() {
        File databaseFile = new File(databaseName);
        return databaseFile.exists();
    }

    /**
     * Creates the database file upon first run of the application
     */
    private static void createDatabaseFile() {
        InputStream instr = null;
        FileOutputStream foutstr = null;
        File destinationFile = null;
        try {
            instr = context.getAssets().open("rootio.sqlite");

            byte[] buffer = new byte[1024000]; // 1 MB
            instr.read(buffer);
            destinationFile = new File(databaseName);
            if (destinationFile.exists()) {
                destinationFile.delete();
            }
            if (destinationFile.createNewFile()) {
                foutstr = new FileOutputStream(destinationFile);
                foutstr.write(buffer);
            } else {
                Utils.toastOnScreen("Failed to create database file! Maybe you are out of space", context);
            }
        } catch (IOException ex) {
            Log.e(context.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(DBAgent.createDatabaseFile)" : ex.getMessage());

        } finally {
            try {
                instr.close();
            } catch (Exception ex) {
                Log.e(context.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(DBAgent.createDatabaseFile)" : ex.getMessage());
            }

            try {
                foutstr.close();
            } catch (Exception ex) {
                Log.e(context.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(DBAgent.createDatabaseFile)" : ex.getMessage());
            }
        }

    }

    /**
     * Fetches data from the database as per the specified query and filters
     *
     * @param rawQuery The SQL query to be executed against the database
     * @param args     Arguments for where clause parameters that may be specified
     * @return Array of String arrays each representing a record in the database
     */
    public static String[][] getData(String rawQuery, String[] args) {
        SQLiteDatabase database = getDBConnection(databaseName, null, SQLiteDatabase.OPEN_READONLY);

        try {
            Cursor cursor = database.rawQuery(rawQuery, args);
            String[][] data = new String[cursor.getCount()][cursor.getColumnCount()];
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                for (int j = 0; j < cursor.getColumnCount(); j++) {
                    data[i][j] = cursor.getString(j);
                }
            }
            return data;
        } catch (Exception ex) {
            Log.e(context.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception" : ex.getMessage());
            return null;
        } finally {
            database.close();
        }
    }

    /**
     * Saves a row to the Database
     *
     * @param tableName      The name of the table to which to save the data
     * @param nullColumnHack The column in which to insert a null in case of an empty row
     * @param data           Map of column names and column values to be inserted into the
     *                       specified table
     * @return The row id of the inserted row
     */
    public static synchronized long saveData(String tableName, String nullColumnHack, ContentValues data) {
        int tries;
        for (tries = 0; tries < 10; ) {
            SQLiteDatabase database = getDBConnection(databaseName, null, SQLiteDatabase.OPEN_READWRITE);
            try {

                return database.insert(tableName, nullColumnHack, data);
            } catch (SQLiteDatabaseLockedException ex) {
                tries++;
                /*try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            } catch (Exception ex) {
                Log.e(context.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(DBAgent.saveData)" : ex.getMessage());
                return 0;
            } finally {
                database.close();
            }
        }
        return 0;
    }

    /**
     * Saves a row to the Database
     *
     * @param tableName      The name of the table to which to save the data
     * @param nullColumnHack The column in which to insert a null in case of an empty row
     * @param data           Map of column names and column values to be inserted into the
     *                       specified table
     * @return The row id of the inserted row
     */
    public synchronized static boolean bulkSaveData(String tableName, String nullColumnHack, ContentValues[] data) {
        int tries;
        for (tries = 0; tries < 10; ) {
            SQLiteDatabase database = getDBConnection(databaseName, null, SQLiteDatabase.OPEN_READWRITE);
            database.beginTransaction();
            try {
                for (ContentValues datum : data) {
                    database.insert(tableName, nullColumnHack, datum);
                }
                database.setTransactionSuccessful();
                return true;
            } catch (SQLiteDatabaseLockedException ex) {
                tries++;
                /*try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            } catch (Exception ex) {
                Log.e(context.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(DBAgent.saveData)" : ex.getMessage());
                return false;
            } finally {
                database.endTransaction();
            }
        }
        return false;
    }

    /**
     * Saves multiple rows to the Database
     *
     * @param tableName      The name of the table to which to save the data
     * @param nullColumnHack The column in which to insert a null in case of an empty row
     * @param columns        Array containing names of columns into which data is to be inserted
     * @param data           Multi dimension array of records being inserted
     * @return Whether or not the transaction was successful
     */
    public static synchronized boolean bulkSaveData(String tableName, String nullColumnHack, String[] columns, String[][] data) {
        int tries;
        for (tries = 0; tries < 10; ) {
            SQLiteDatabase database = getDBConnection(databaseName, null, SQLiteDatabase.OPEN_READWRITE);
            database.beginTransaction();
            try {
                ContentValues dt = new ContentValues();

                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < columns.length; j++) {
                        dt.put(columns[j], data[i][j]);
                    }
                    database.insert(tableName, nullColumnHack, dt);

                }
                database.setTransactionSuccessful();
                return true;
            } catch (SQLiteDatabaseLockedException ex) {
                tries++;
                /*try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            } catch (Exception ex) {
                Log.e(context.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(DBAgent.bulkSaveData)" : ex.getMessage());
                return false;
            } finally {
                database.endTransaction();
            }
        }
        return false;
    }

    /**
     * Deletes records from the database according to the specified criteria
     *
     * @param tableName   The name of the table from which to delete records
     * @param whereClause The where clause specifying the records to be deleted
     * @param args        Arguments to the parameters specified in the where clause
     * @return The number of records affected by this delete action
     */
    public static synchronized  int deleteRecords(String tableName, String whereClause, String[] args) {
        int tries;
        for (tries = 0; tries < 10; ) {
            SQLiteDatabase database = getDBConnection(databaseName, null, SQLiteDatabase.OPEN_READWRITE);
            try {
            return database.delete(tableName, whereClause, args);
            } catch (SQLiteDatabaseLockedException ex) {
                tries++;
               /* try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            } catch (Exception ex) {
                Log.e(context.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(DBAgent.deleteRecords)" : ex.getMessage());
                return 0;
            } finally {
                database.close();
            }
        }
        return 0;
    }

    /**
     * Updates records in the database according to the specified criteria
     *
     * @param tableName   The name of the table whose records to update
     * @param data        The values to replace the existing values
     * @param whereClause The where clause specifying the columns to be updated
     * @param whereArgs   Arguments to the where clause
     * @return The number of rows affected by the update transaction
     */
    public static synchronized  int updateRecords(String tableName, ContentValues data, String whereClause, String[] whereArgs) {
        int tries;
        for (tries = 0; tries < 10; ) {
            SQLiteDatabase database = getDBConnection(databaseName, null, SQLiteDatabase.OPEN_READWRITE);
            try {
                return database.update(tableName, data, whereClause, whereArgs);
            } catch (SQLiteDatabaseLockedException ex) {
                tries++;
                /*try {
                    //Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            } catch (Exception ex) {
                Log.e(context.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(DBAgent.updateRecords)" : ex.getMessage());
                return 0;
            } finally {
                database.close();
            }
        }
        return 0;
    }

}
