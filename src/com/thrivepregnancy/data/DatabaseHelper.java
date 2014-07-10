package com.thrivepregnancy.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import au.com.bytecode.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;
import com.thrivepregnancy.R;
import com.thrivepregnancy.ui.MainActivity;
import com.thrivepregnancy.ui.StartupActivity;

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
	// name of tips input CSV file
	private static final String TIP_FILE = "tips.csv";
	// name of needs input CSV file
	private static final String NEED_FILE = "needs.csv";
		
	private Dao<Event, Integer> 	eventDao;
	private Dao<Need, Integer> 		needDao;
	
	private Context m_context;
	private Calendar m_duedate;
	private String m_name;
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		m_context = context;
		Log.d("DatabaseHelper.DatabaseHelper", "database helper constructor");
	}
	
	/** loads name and due date from user preferences */
	public void ReadUserPreferences () {
		/*
		SharedPreferences preferences = m_context.getSharedPreferences(m_context.getResources().getString(R.string.PREFERENCES), 0);
		
		m_name = preferences.getString(m_context.getResources().getString(R.string.PREFERENCE_NAME), null);
		long dateInMillis = preferences.getLong(m_context.getResources().getString(R.string.PREFERENCE_DUE_DATE), -1);
		*/
		SharedPreferences preferences = m_context.getSharedPreferences(StartupActivity.PREFERENCES, 0);
		
		m_name = preferences.getString(StartupActivity.PREFERENCE_NAME, null);
		long dateInMillis = preferences.getLong(StartupActivity.PREFERENCE_DUE_DATE, -1);

		m_duedate= Calendar.getInstance();
		m_duedate.setTimeInMillis(dateInMillis);
	}
	
	/** Returns tip date by due date and week */
	public Date ReturnDate(Integer week) {

        Calendar date = (Calendar)m_duedate.clone();
        date.add(Calendar.DAY_OF_YEAR, 7 * (week - 41));
        /*
        for (int i=41; i>week; i--) {
        	date.add(Calendar.DATE, -7);
        }
*/       
        return date.getTime();
    }
    
	
	/** reads appropriate csv file */
	private final List<String[]> readCSVFile(String file) {
		List<String[]> returnList = new ArrayList<String[]>();
		AssetManager assetManager = m_context.getAssets();
		
		try {
			InputStream csvStream = assetManager.open(file);
			InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
			CSVReader csvReader = new CSVReader(csvStreamReader);
			String [] csvLine;
			
			//header line is just the title line
			csvReader.readNext();
			
			while ((csvLine = csvReader.readNext()) != null) {
				returnList.add(csvLine);
			}
		} catch (IOException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to read input file", e);
		}
		
		return returnList;
	}
	
	/** creates events from the list in the database */
	private void createEvents(List<String[]> eventList) {
		String[] eventString;
		Event event;
// Log.d(MainActivity.DEBUG_TAG, "*** Creating Tip Events");
		boolean firstTip = true;
		try {
			for (int i=0; i<eventList.size(); i++) {
				eventString = eventList.get(i);
				event = new Event();
				
				if (firstTip){
					firstTip = false;
			    	SharedPreferences preferences = m_context.getSharedPreferences(StartupActivity.PREFERENCES, Activity.MODE_PRIVATE);
					SharedPreferences.Editor editor = preferences.edit();
			    	editor.putInt(StartupActivity.PREFERENCE_FIRST_WEEK, Integer.valueOf(eventString[0]));
			    	editor.commit();
				}
				
				event.setType(Event.Type.TIP);
				event.setDate(ReturnDate(Integer.parseInt(eventString[0])));
/*
SimpleDateFormat format = new SimpleDateFormat("MMMMMMMMM d, yyyy", Locale.CANADA);
String d = format.format(event.getDate());
Log.d(MainActivity.DEBUG_TAG, "***** " + "Week " + (i+1) + " " + d);
*/
				event.setText(eventString[1].replace("\"","\\\""));
				event.setPhotoFile(eventString[2]);
				event.setAudioFile(eventString[3]);
				
				eventDao.create(event);
			}
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to insert an event", e);
		}
	}
	
	/** creates needs from the list in the database */
	private void createNeeds(List<String[]> needList) {
		String[] needString;
		Need need;
		
		try {
			for (int i=0; i<needList.size(); i++) {
				needString = needList.get(i);
				need = new Need();
				
				need.setGotit(false);
				need.setNeedit(false);
				need.setTitle(needString[0].replace("\"","\\\""));
				need.setResources(needString[1]);
				
				needDao.create(need);
			}
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to insert a need", e);
		}
	}
	
	/**
	 * Called when the first version of the app is installed and the db is created for the first time 
	 */
	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource){
		List<String[]> eventList = new ArrayList<String[]>();
		List<String[]> needList = new ArrayList<String[]>();
		
		try {
			ReadUserPreferences();
			
			TableUtils.createTable(connectionSource, Event.class);
			getEventDao();		
			
			// Read the "Tips" file packaged in the app, create and write Tip records to the Event table
			eventList = readCSVFile(TIP_FILE);
			createEvents(eventList);
			
			Log.d("DatabaseHelper.onCreate", "tips created in database");
			
			TableUtils.createTable(connectionSource, Need.class);
			getNeedDao();
			
			// Read the "Needs" file packaged in the app, create and write Need records to the Need table
			needList = readCSVFile(NEED_FILE);
			createNeeds(needList);
			
			Log.d("DatabaseHelper.onCreate", "needs created in database");
		} 
		catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to create databases", e);
		}
	}
	
	/**
	 * Called when a database exists but the DATABASE_VERSION has been incremented.
	 */
	public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion){
	}

	public Dao<Event, Integer> getEventDao() {
		if (eventDao == null) {
			try {
				eventDao = getDao(Event.class);
			}
			catch (SQLException e){
				Log.e(MainActivity.DEBUG_TAG, "Cannot retrieve event dao", e);
			}
		}
		return eventDao;
	}
	public Dao<Need, Integer> getNeedDao() {
		if (needDao == null) {
			try {
				needDao = getDao(Need.class);
			}
			catch (SQLException e){
				Log.e(MainActivity.DEBUG_TAG, "Cannot retrieve need dao", e);
			}
		}
		return needDao;
	}
}
