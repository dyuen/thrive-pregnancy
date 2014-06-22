package com.thrivepregnancy.ui;

import android.support.v4.app.FragmentActivity;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.thrivepregnancy.data.DatabaseHelper;
/**
 * A convenience superclass for Activity classes which may require
 * the ORMLITE DatabaseHelper object
 */
public class BaseActivity extends FragmentActivity{

	private DatabaseHelper 	databaseHelper = null;

	/**
	 * @return a DatabaseHelper
	 */
	protected DatabaseHelper getHelper() {
	    if (databaseHelper == null) {
	        databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
	    }
	    return databaseHelper;
	}

	/**
	 * Releases the database helper
	 */
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    if (databaseHelper != null) {
	        OpenHelperManager.releaseHelper();
	        databaseHelper = null;
	    }
	}
}
