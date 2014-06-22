package com.thrivepregnancy.data;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

public class EventDataHelper{
	private Dao<Event, Integer> eventDao;
	private Dao<Need, Integer> needDao;
	
	public EventDataHelper(DatabaseHelper databaseHelper) {
		eventDao = databaseHelper.getEventDao();
		needDao = databaseHelper.getNeedDao();	
	}
	
	/** returns date-ordered list of all Tip, Diary Entry, and Appointment objects */
	public List<Event> getTimelineEvents() {
		List<Event> events = null;
		
		try {
			QueryBuilder<Event, Integer> queryBuilder = eventDao.queryBuilder();
    		
			Where<Event, Integer> where = queryBuilder.where();
    		where.eq("type", Event.Type.APPOINTMENT).or();
    		where.eq("type", Event.Type.TIP).or();
    		where.eq("type", Event.Type.DIARY_ENTRY);
    		queryBuilder.orderBy("date", true);
    		
    		PreparedQuery<Event> preparedQuery = queryBuilder.prepare();
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
			Log.e(EventDataHelper.class.getName(), "Unable to get appointment events", e);
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
			Log.e(EventDataHelper.class.getName(), "Unable to get question events", e);
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
			Log.e(EventDataHelper.class.getName(), "Unable to get test result events", e);
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

	public Dao<Need, Integer> getNeedDao() {
		return needDao;
	}

	public void setNeedDao(Dao<Need, Integer> needDao) {
		this.needDao = needDao;
	}
}
