package com.thrivepregnancy.data;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

public class EventDataHelper extends DatabaseHelper {
	private Dao<Event, Integer> eventDao;
	private Dao<Need, Integer> needDao;
	
	public EventDataHelper(Context context) {
		super(context);
		
		try {
			eventDao = getEventDao();
			needDao = getNeedDao();
		} catch (SQLException e) {
			Log.e(EventDataHelper.class.getName(), "Unable to get dao", e);
		}
		
		
		Log.d("EventDataHelper.EventDataHelper", "EventDataHelper constructor");
	}
	
	/** returns date-ordered list of all event objects */
	public List<Event> getTimelineEvents() {
		List<Event> events = null;
		
		try {
			QueryBuilder<Event, Integer> queryBuilder = eventDao.queryBuilder();
			PreparedQuery<Event> preparedQuery;
			
			queryBuilder.orderBy("date",true);
			preparedQuery = queryBuilder.prepare();
			
			events = eventDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(EventDataHelper.class.getName(), "Unable to get timeline events", e);
		}
        
		return events;
	}
	
	/** returns date-ordered list of all Appointment event objects */
	public List<Event> getAppointments() {
		List<Event> events = null;
		
		try {
			QueryBuilder<Event, Integer> queryBuilder = eventDao.queryBuilder();
			PreparedQuery<Event> preparedQuery;
			
			queryBuilder.where().eq("type", Event.Type.APPOINTMENT);
			queryBuilder.orderBy("date",true);
			
			preparedQuery = queryBuilder.prepare();
			
			events = eventDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(EventDataHelper.class.getName(), "Unable to get timeline events", e);
		}
        
		return events;
	}
	
	/** returns date-ordered list of all Question event objects */
	public List<Event> getQuestions() {
		List<Event> events = null;
		
		try {
			QueryBuilder<Event, Integer> queryBuilder = eventDao.queryBuilder();
			PreparedQuery<Event> preparedQuery;
			
			queryBuilder.where().eq("type", Event.Type.QUESTION);
			queryBuilder.orderBy("date",true);
			
			preparedQuery = queryBuilder.prepare();
			
			events = eventDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(EventDataHelper.class.getName(), "Unable to get timeline events", e);
		}
        
		return events;
	}
	
	/** returns date-ordered list of all Test Result event objects */
	public List<Event> getTestResults() {
		List<Event> events = null;
		
		try {
			QueryBuilder<Event, Integer> queryBuilder = eventDao.queryBuilder();
			PreparedQuery<Event> preparedQuery;
			
			queryBuilder.where().eq("type", Event.Type.TEST_RESULT);
			queryBuilder.orderBy("date",true);
			
			preparedQuery = queryBuilder.prepare();
			
			events = eventDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(EventDataHelper.class.getName(), "Unable to get timeline events", e);
		}
        
		return events;
	}
	
	/** returns list of all need objects */
	public List<Need> getNeeds() {
		List<Need> needs = null;
		
		try {
			needs = needDao.queryForAll();
		} catch (SQLException e) {
			Log.e(EventDataHelper.class.getName(), "Unable to get needs", e);
		}
        
		return needs;
	}
}
