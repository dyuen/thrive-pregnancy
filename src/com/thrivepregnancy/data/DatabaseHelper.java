package com.thrivepregnancy.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper which creates and upgrades the database and provides the DAOs for the app.
 * The helper can be kept open across all activities in your app with the same SQLite 
 * database connection reused by all threads.
 * 
 * From http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_4.html#Use-With-Android
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper{

	// name of the database file 
	private static final String DATABASE_NAME = "database.db";
	// increment if schema changes, or a new app version contains a new Tips file
	private static final int DATABASE_VERSION = 1;
	
	private Dao<Event, Integer> eventDao;
	private Dao<Need, Integer> needDao;
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * Called when the first version of the app is installed and the db is created for the first time 
	 */
	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource){
		try {
			TableUtils.createTable(connectionSource, Event.class);
			getEventDao();			
			// Read the "Tips" file packaged in the app, create and write Tip records to the Event table
			
			TableUtils.createTable(connectionSource, Need.class);
			getNeedDao();
			// Read the "Needs" file packaged in the app, create and write Need records to the Need table
		} 
		catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to create datbases", e);
		}
	}
	
	/**
	 * Called when a database exists but the DATABASE_VERSION has been incremented.
	 */
	public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion){
	}

	public Dao<Event, Integer> getEventDao() throws SQLException {
		if (eventDao == null) {
			eventDao = getDao(Event.class);
		}
		return eventDao;
	}
	public Dao<Need, Integer> getNeedDao() throws SQLException {
		if (needDao == null) {
			needDao = getDao(Need.class);
		}
		return needDao;
	}
}
