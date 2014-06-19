package com.thrivepregnancy.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.TextView;
import au.com.bytecode.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;
import com.thrivepregnancy.R;

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
	private static final String TIP_FILE = "tips.txt";
	// name of needs input CSV file
	private static final String NEED_FILE = "needs.txt";
		
	private Dao<Event, Integer> eventDao;
	private Dao<Need, Integer> needDao;
	
	private Context m_context;
	
	private Calendar m_duedate;
	private String m_name;
	
	private static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		m_context = context;
	}
	
	/** loads name and due date from user preferences */
	public void ReadUserPreferences () {
		m_name = "Sev";
		m_duedate= Calendar.getInstance();
        m_duedate.set(2014,Calendar.DECEMBER,15);
	}
	
	/** Returns tip date by due date and week */
	public Date ReturnDate(Integer week) {

        Calendar date = (Calendar)m_duedate.clone();;
    	
        for (int i=40; i>week; i--) {
        	date.add(Calendar.DATE, -7);
        }
        
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
		
		try {
			for (int i=0; i<eventList.size(); i++) {
				eventString = eventList.get(i);
				event = new Event();
				
				event.setType(Event.Type.TIP);
				event.setDate(ReturnDate(Integer.parseInt(eventString[0])));
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
			TableUtils.createTable(connectionSource, Event.class);
			getEventDao();		
			
			// Read the "Tips" file packaged in the app, create and write Tip records to the Event table
			eventList = readCSVFile(TIP_FILE);
			createEvents(eventList);
			
			TableUtils.createTable(connectionSource, Need.class);
			getNeedDao();
			
			// Read the "Needs" file packaged in the app, create and write Need records to the Need table
			needList = readCSVFile(NEED_FILE);
			createNeeds(needList);
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
