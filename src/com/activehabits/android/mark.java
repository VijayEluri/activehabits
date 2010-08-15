package com.activehabits.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.String;

public class mark extends Activity implements OnClickListener {
	private static final String TAG = "ActiveHabits.mark";
	private static FileWriter writer;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    public void onResume() {
        super.onResume();
        // load default preferences
        SharedPreferences myMgrPrefs = PreferenceManager
            .getDefaultSharedPreferences(this);
        setContentView(R.layout.main);

        // prepare to add more buttons if they exist
        Map<String, ?> bar = myMgrPrefs.getAll();
        Log.i(TAG, "mark myMgrPrefs: " + bar.toString());
        int len = bar.size();

        // roughly each button height = screen size / 1+len
        //         subtract for padding
        Display container = ((WindowManager)this.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
//        Log.i(TAG, "mark vars1: " + container.toString() );
        Log.i(TAG, "mark vars2: " + container.getHeight());//(int) container.getHeight() );
        final Integer buttonHeight;
        if (len == 0) {
            buttonHeight = (Integer) ((container.getHeight() - (10*(len+2) )) / (1)); }
        else {
            buttonHeight = (Integer) ((container.getHeight() - (10*(len+2) )) / (len)); }
        // set up action0 button
        Button logEventButton = (Button) findViewById(R.id.log_event_button);
        logEventButton.setText(myMgrPrefs.getString("action0", "Mark Action"));
        logEventButton.setMinLines(3);
        logEventButton.setPadding(10, 10, 10, 10);
        logEventButton.setOnClickListener((OnClickListener) this);
        logEventButton.setHeight(buttonHeight);

        // prepare to add more buttons if they exist
        String newAction;
        //View container = findViewById(R.layout.main);
//        Log.i(TAG, "mark container:" + container.toString());
        for (int i = 1; i < len ; ++i) { // i=1, don't run for action0
            newAction = "action" + i;
            //Log.i(TAG, "mark testing: '" + newAction + "'");
            if ( bar.containsKey(newAction) ) { // TODO: & ! (findView(newAction)) ) {
                Log.i(TAG, "mark 003");
                //if (container.findViewWithTag(newAction) == null) {
                //    Log.i(TAG, "ADD");
                //}
                Log.i(TAG, "mark 004");
                // add new button to activity
                Log.i(TAG, "mark need to add: " + newAction + ", " + (String) bar.get(newAction));

                Button newButton = new Button(this);
                newButton.setMinLines(3);
                newButton.setPadding(10, 10, 10, 10);
                newButton.setTag(newAction);
                newButton.setText(myMgrPrefs.getString(newAction, "Mark Action"));
                newButton.setClickable(true);
                newButton.setFocusableInTouchMode(false);
                newButton.setFocusable(true);
                newButton.setLongClickable(false); // for now
                newButton.setOnClickListener((OnClickListener) this);
                newButton.setHeight(buttonHeight);
                //logEventButton.getParent();
                //logEventButton.getLayout().addTouchables(newButton);
                //.addPreference(newButton);
                //((ViewGroup) container).addView(newButton));
                ((ViewGroup) logEventButton.getParent()).addView(newButton);

                Log.i(TAG, "mark added: " + newAction + ", " + myMgrPrefs.getString(newAction, "Mark Action"));
            }
        }
        //Map<String, ?> foo = myGetPrefs.getAll();
        //Log.i(TAG, "mark myGetPrefs: " + foo.toString());
        //Log.i(TAG, "mark action1 Mgr: " + myMgrPrefs.getString("action1", "Mark Action"));
        Log.i(TAG, "mark myMgrPrefs: " + myMgrPrefs.getAll().toString());

//        Log.i(TAG, "mark action0: " + myPrefs.getString("action0", "Mark Action"));
//        registerForContextMenu(logEventButton);

        boolean mExternalStorageAvailable = false;
    	boolean mExternalStorageWriteable = false;
    	String state = Environment.getExternalStorageState();
    	if (Environment.MEDIA_MOUNTED.equals(state)) {
    	    // We can read and write the media
    	    mExternalStorageAvailable = mExternalStorageWriteable = true;
    	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
    	    // We can only read the media
    	    mExternalStorageAvailable = true;
    	    mExternalStorageWriteable = false;
    	} else {
    	    // Something else is wrong. It may be one of many other states, but all we need
    	    //  to know is we can neither read nor write
    	    mExternalStorageAvailable = mExternalStorageWriteable = false;
    	}

    	// begin work

    	if (mExternalStorageAvailable) {
    		// getExternalStorageDirectory()
    		// check if f1le exists
    		//read in last event from log
    		try {
    			File root = Environment.getExternalStorageDirectory();
    			Resources res = getResources();
    			String sFileName = res.getString(R.string.log_event_filename); //String sFileName = "activehabits.txt";
    			File gpxfile = new File(root,sFileName);
    			//if gpxfile.exists()
    			writer = new FileWriter(gpxfile, true); // appends
    			//else
    		}
    		catch(IOException e) {
    			e.printStackTrace();
    		}
    	} else  { // file doesn't exist - needs testing but works OK for now
    		if (mExternalStorageWriteable) {
    			// create log & notify user
    		} else {
    			// notify user no storage and no log && exit
    		}
    	  }
    }

    @Override
    public void onPause() {
    	try {
    	writer.flush();
    	writer.close();
    	}
    	catch(IOException e) {
    		e.printStackTrace();
    	}
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.habit_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.removeItem(R.id.mark); // we are in mark so disable mark item
        // is it possible to make menu 1 x 3 instad of 2x2?
//      ((ViewGroup) menu.getItem(R.id.chart)).setPadding(1,10,10,1);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
//      case R.id.mark: // item removed
//          return true;
        case R.id.chart:
        	Intent myChartIntent = new Intent(this,chart.class);
        	startActivity(myChartIntent);
        	finish();
            return true;
//      case R.id.social:
//          return true;
        case R.id.prefs:
            Intent myPrefIntent = new Intent(this,prefs.class);
            startActivity(myPrefIntent);
            return true;
        case R.id.about:
            return true; // TODO: about from mark
        case R.id.quit: {
            finish();
            return true;
        }
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public void onClick(View v) {
		Calendar rightnow = Calendar.getInstance();
		Date x = rightnow.getTime();
	        // x.getTime() should be identical rightnow.getTimeInMillis()
		Integer b = x.getMinutes() * 100 / 60;

        LocationManager locator = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location loc = null;
        try {
            loc = locator.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null) {
                // Fall back to coarse location.
                loc = locator.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//              Criteria c = new Criteria();
//              c.setAccuracy(Criteria.NO_REQUIREMENT);
//              Location loc = locator.getLastKnownLocation(locationManager.getBestProvider(c, true));
                             // criteria, enabledOnly - getLastKnownLocation error check?
            }
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        String locString;
        if (loc == null)
        	locString = "";
        else
        	locString = loc.toString();

        CharSequence buttonText = ((Button) v).getText();

        try {
        	if (b < 10) {
        		Log.i(TAG, "mark write: "
        		        + buttonText + "\t"
        		        + (rightnow.getTimeInMillis() / 1000) + "\t"
        		        + x.getHours() + ".0" + b + "\t"
        		        + x.toLocaleString() + "\t" + locString);
        		writer.append( buttonText + "\t" +
        		        (rightnow.getTimeInMillis() / 1000) + "\t"
        		        + x.getHours() + ".0" + b + "\t"
        		        + x.toLocaleString() + "\t"
        		        + locString + "\n");
        	} else {
        		Log.i(TAG, "mark write: "
        		        + buttonText + "\t"
        		        + (rightnow.getTimeInMillis() / 1000) + "\t"
        		        + x.getHours() + "." + b + "\t"
        		        + x.toLocaleString() + "\t"
        		        + locString);
        		writer.append( buttonText + "\t"
        		        + (rightnow.getTimeInMillis() / 1000) + "\t"
        		        + x.getHours() + "." + b + "\t"
        		        + x.toLocaleString() + "\t"
        		        + locString + "\n");
        	}
        }
        catch (IOException e) {
        	e.printStackTrace();
        }
    	finish();
	}

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.context_menu, menu);
//    }

//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
//        // Handle item selection
//        switch (item.getItemId()) {
//        case R.id.rename:
//            renameEvent(info.id);
//            return true;
//        case R.id.remove:
//            removeEvent(info.id);
//            return true;
//        default:
//            return super.onContextItemSelected(item);
//        }
//    }

//    private void renameEvent(long id) {
//        // from Context Item
//        showDialog(R.id.dialog_rename);
//    }
//
//    private void removeEvent(long id) {
//        // from Context Item
//
//    }

//    @Override
//    protected Dialog onCreateDialog(int id) {
//        switch(id) {
//        case R.id.rename:
//            rn.SetText("eventname");
//            default:
//                return null;
//        }
//    }

};
