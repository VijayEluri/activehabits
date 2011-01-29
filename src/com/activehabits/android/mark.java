package com.activehabits.android;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class mark extends Activity implements OnClickListener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "AH.mark"; // for Log.i(TAG, ...);
    //private static FileWriter writer;
    private static int paddingValue = 7; // * 10 pixels for calculating button sizes
    private static int splashed = 0;
    protected static String currentSet = "activehabits.txt"; // current set 
    protected static int radioSet = 0; // radio selection of set 
    private View contextMenuItem; // button long pressed for context menu
    private View textEntryView;   // TextEntry for renaming
    private ActiveHabitsName ahn;
    private String sSetName;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // TODO: find onCreate / onResume bug
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);                
        this.onResume();
    }

    @Override
    public void onResume() {
        super.onResume();

        // get preference name
		Resources res = getResources();
		String sFileName = res.getString(R.string.log_event_filename); //String sFileName = "activehabits.txt";
        this.ahn = new ActiveHabitsName(this, sFileName);
        sSetName = ahn.get();
        
        // load default preferences
        SharedPreferences myMgrPrefs = getSharedPreferences(sSetName, 0);
        setContentView(R.layout.main);

        // prepare to add more buttons from myMgrPrefs if they exist
        Map<String, ?> bar = myMgrPrefs.getAll();
        //Log.i(TAG, "mark myMgrPrefs: " + bar.toString());
        int len = sizeWithoutPl(myMgrPrefs);

        // roughly each button height = screen size / 1+len
        //         subtract for padding - use self.paddingValue
        Display container = ((WindowManager)this.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        //Log.i(TAG, "mark vars1: " + container.toString() );
        //Log.i(TAG, "mark vars2: " + container.getHeight());//(int) container.getHeight() );
        Integer buttonHeight;
        if (len == 0) {
            buttonHeight = (Integer) ((container.getHeight() - (10*(mark.paddingValue+1) )) / (1)); }
        else {
            buttonHeight = (Integer) ((container.getHeight() - (10*(len+mark.paddingValue) )) / (len)); }
        // set up action0 button
        Button logEventButton = (Button) findViewById(R.id.log_event_button);
        logEventButton.setText(myMgrPrefs.getString("action0", getString(R.string.markaction)));
        logEventButton.setTag("action0");
        logEventButton.setMinLines(3);
        logEventButton.setPadding(10, 10, 10, 10);
        logEventButton.setTextSize((float)24.0);
        logEventButton.setTypeface(null, Typeface.BOLD);
        logEventButton.setOnClickListener((OnClickListener) this);
        logEventButton.setHeight(buttonHeight);
        logEventButton.setTextColor(0xFFFFFFFF); // text color
        logEventButton.getBackground().setColorFilter(0x33FFFFFF, PorterDuff.Mode.MULTIPLY); // background color
            // use 0xFFFFFFFF for default behavior
        registerForContextMenu(logEventButton);
        
        final CharSequence setTo = logEventButton.getText();
        final CharSequence defaultSetTo = getString(R.string.markaction);
        
        //Log.i(TAG, "mark splash? " + setTo + ", " + defaultSetTo);

        if (setTo.equals(defaultSetTo) & (mark.splashed == 0)) {     // strange syntax to make it compare
            mark.splashed = 1;
        	// assume if first action is not changed from default
        	//     this is first run or help is needed so show splash
        	Intent mySplashIntent = new Intent(this,splash.class);
        	startActivityForResult(mySplashIntent,1);
        	// TODO: review splash vs. about activity use
        }

        // add more buttons if they exist
        String newAction;
        for (int i = 1; i < len ; ++i) { // i=1, don't run for action0
            newAction = "action" + i;
            if ( bar.containsKey(newAction) ) { // & ! (findView(newAction)) ) {
                // add new button to activity
                //Log.i(TAG, "mark need to add: " + newAction + ", " + (String) bar.get(newAction) + ", " +buttonHeight);
                createNewButton(newAction, myMgrPrefs.getString(newAction, getString(R.string.markaction)), buttonHeight);
            }
        }
        //Log.i(TAG, "mark myMgrPrefs: " + myMgrPrefs.getAll().toString());
        
        /* show history at the bottom */
        try {
            View history = new TextView(this);
            ((TextView) history).setText(myMgrPrefs.getString("lastactionpl", ""));
            ((ViewGroup) logEventButton.getParent()).addView(history);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
    }

    private void createNewButton(String newAction, String newActionString, Integer newButtonHeight) {
    	// add new button to activity
        Button newButton = new Button(this);
        newButton.setMinLines(3);
        newButton.setPadding(10, 10, 10, 10);
        newButton.setTextSize((float)24.0);
        newButton.setTypeface(null, Typeface.BOLD);
        newButton.setTag(newAction);
        newButton.setText(newActionString);
        newButton.setClickable(true);
        newButton.setLongClickable(true);
        newButton.setFocusableInTouchMode(false);
        newButton.setFocusable(true);
        newButton.setOnClickListener((OnClickListener) this);
        newButton.setHeight(newButtonHeight);
        newButton.setTextColor(0xFFFFFFFF); // text color
        newButton.getBackground().setColorFilter(0x33FFFFFF, PorterDuff.Mode.MULTIPLY); // background color
        registerForContextMenu(newButton);
        View logEventButton = findViewById(R.id.log_event_button);
        ((ViewGroup) logEventButton.getParent()).addView(newButton);

        //Log.i(TAG, "mark added: " + newAction + ", " + newActionString);
    }
    
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // bottom menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.habit_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) { // bottom menu
        super.onPrepareOptionsMenu(menu);
        menu.removeItem(R.id.mark); // we are in mark so disable mark item
        // is it possible to make menu 1 x 3 instead of 2x2?
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.addaction:
        	addNewAction();
            return true;
        case R.id.setmenu:
        	showDialog(R.id.chooseset);
            return true;
        case R.id.chart:
        	Intent myChartIntent = new Intent(this,chart.class);
        	startActivity(myChartIntent);
        	finish();
            return true;
//      case R.id.social:
//          return true;
        case R.id.help:
            Intent myHelpIntent = new Intent(this,help.class);
            startActivity(myHelpIntent);
            return true;
        case R.id.about:
            Intent myAboutIntent = new Intent(this,about.class);
            startActivity(myAboutIntent);
            return true;
//        case R.id.quit: {
//            finish();
//            return true;
//        }
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

        String buttonText = ((Button) v).getText().toString();

        //Log.i(TAG, "buttonText: " + buttonText);
        //Log.i(TAG, "R.string.markaction: " + getString(R.string.markaction));
        //if (buttonText == ((CharSequence) getString(R.string.markaction))) {
        if (buttonText.matches(getString(R.string.markaction))) {
            // TODO: \o/ dialog - rename before pressing a button, marking an action
            //Log.i(TAG, "buttonText MATCHED");
        	return;
        }
        //Log.i(TAG, "recorded bad data?");
        
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
    	    // Something else is wrong. It may be one of many other states,
    		// however all we need to know is we can neither read nor write
    	    mExternalStorageAvailable = mExternalStorageWriteable = false;
    	}

        // get preference name & sFileName
		Resources res = getResources();
		String sFileName = res.getString(R.string.log_event_filename); //String sFileName = "activehabits.txt";
        this.ahn = new ActiveHabitsName(this, sFileName);
        sSetName = ahn.get();

        // setup file object
    	FileWriter w;
    	BufferedWriter writer;
    	if (mExternalStorageAvailable & mExternalStorageWriteable ){
    		// getExternalStorageDirectory()
    		// check if file exists
    		//read in last event from log
    		try {
    			File root = Environment.getExternalStorageDirectory();
    			// Note: defaults to root of external storage.
    			//       Spec says to use /Android/data/com.activehabits.android/files/
    			//       I see no reason to subject users to something so cumbersome.
    			//       Alternative spec says use one of {Music,Podcasts,Ringtones,
    			//       Alarms,Notifications,Pictures,Movies,Download} but our file
    			//       doesn't seem to fit any of them.
    			// TODO: use sqlite to store a single table with a
    			//       single row and single column
    			//       for the current filename
    			//       that defaults to value R.string.log_event_filename
    			File gpxfile = new File(root,sFileName);
    			if (gpxfile.exists())
    				// append
                    w = new FileWriter(gpxfile, true);
    			else
    				// doesn't exist so overwrite a new file
    				// this fixes the first click new user crash bug
                    w = new FileWriter(gpxfile, false);
                // wrap in a buffer
                writer = new BufferedWriter(w);
/*    		}
    		catch(IOException e) {
    			e.printStackTrace();
    		}
    		// needs testing
    			// create log && notify user new file was created
    	} // else { // external storage is either not available or not writeable - trouble
		// notify user of no writeable storage and no log && exit
        
        try {
*/
                long presentTime = (rightnow.getTimeInMillis() / 1000);
                // prepare to write data
            	RandomAccessFile r = new RandomAccessFile(gpxfile, "r");
            	byte[] lastchars = new byte[11];
            	long fs = r.length(); // file size
            	Log.i(TAG, "mark fs = " + Long.toString(fs));
            	r.seek(fs-10);
            	r.read(lastchars, 0, 10); // tests show -1 = 10 and -0 = 0 (NUL)
            	//String lc = new String(lastchar); // last char
            	Log.i(TAG, "mark lc = " + new String(lastchars)); // for debugging
                if (lastchars[9] != 10) { // check for an ASCII ESC
                	writer.append("\n");
                }
                // write data
                if (b < 10) { // pads hours if <10
                     Log.i(TAG, "mark write: "
                            + buttonText + "\t"
                            + presentTime + "\t"
                            + "\t" // future user's text entry
                            + x.getHours() + ".0" + b + "\t"
                            + x.toLocaleString() + "\t" + locString);
        		    writer.append( buttonText + "\t"
                            + presentTime + "\t"
                            + "\t" // future user's text entry
                            + x.getHours() + ".0" + b + "\t"
                            + x.toLocaleString() + "\t"
                            + locString + "\n");
        		} else { // doesn't pad hours
        			Log.i(TAG, "mark write: "
                            + buttonText + "\t"
                            + presentTime + "\t"
                            + "\t" // future user's text entry
                            + x.getHours() + "." + b + "\t"
                            + x.toLocaleString() + "\t"
                            + locString);
        		    writer.append( buttonText + "\t"
                            + presentTime + "\t"
                            + "\t" // future user's text entry
                            + x.getHours() + "." + b + "\t"
                            + x.toLocaleString() + "\t"
                            + locString + "\n");
        		}
                writer.flush();
            	writer.close();
            }
    		catch (IOException e) {
        		e.printStackTrace();
        	}
    	}
        // if a playlist is set, play it
        SharedPreferences myMgrPrefs = getSharedPreferences(sSetName, 0);
        String playAction = (String)v.getTag();
        String pl = myMgrPrefs.getString( playAction + "pl", null);
        if ( ! ( pl == null) ) {
            Log.i(TAG, "play list " + pl);
    	    Intent intent = new Intent(Intent.ACTION_VIEW);
    	    intent.setComponent(new ComponentName("com.android.music","com.android.music.PlaylistBrowserActivity"));
    	    intent.setType(MediaStore.Audio.Playlists.CONTENT_TYPE);
    	     intent.setFlags(0x10000000); // need to understand these 3 lines better
    	     //intent.putExtra(playlist, false);
    	    intent.putExtra("playlist", pl );
    	    final Intent fintent = intent; // for passing into postDelayed

            // Sleep 2 seconds here - a failed fix for \n bug
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    startActivity(fintent);
                }
            }, 2000);

            // Kendra Springer http://www.jamendo.com/en/artist/Kendra_Springer after login
    	    // test mp3    http://download31.jamendo.com/download/track/469312/mp32/24deaf7def/Hope.mp3
            // second link http://download29.jamendo.com/download/track/674453/mp32/ce8683423f/Hope.mp3
        }
        
        /* store clicked item as history */
        // lastactionpl is the key
        // "[button's name], [human readable date string]" is the value, ready for display
        Editor e = myMgrPrefs.edit();
        e.putString("lastactionpl", "last action: " + buttonText + " @ " +  x.toLocaleString() );
        e.commit();

    	finish();
	}

    private void addNewAction() {
        SharedPreferences myMgrPrefs = getSharedPreferences(sSetName,0);

        // add to shared preferences
        int len = sizeWithoutPl(myMgrPrefs); // -1 for 0 based, + 1 for new value = size
        String newAction = "action" + len;
        //Log.i(TAG, "mark adding: " + newAction + ", " + getString(R.string.markaction));

        Editor e = myMgrPrefs.edit();
        e.putString(newAction, getString(R.string.markaction));
        e.putString(newAction + "pl", null);
        e.commit();
        //Log.i(TAG, "mark myMgrPrefs: " + myMgrPrefs.getAll().toString());

        // calculate new buttonHeight
        Display container = ((WindowManager)this.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        final Integer buttonHeight;
        // we are adding a button, len will be OK.
        buttonHeight = (Integer) ((container.getHeight() - (10*(mark.paddingValue) )) / (len));
    	// resize existing buttons to buttonHeight
        ViewGroup context = (ViewGroup) findViewById(R.id.log_event_button).getParent();
        Integer i;
        for (i = 0; i < len; ++i) {
        	//Log.i(TAG, "mark resizing " + i + ", " + context.getChildAt(i) + " to " + buttonHeight);
            ((Button) context.getChildAt(i)).setHeight(buttonHeight);
        }
        // add button to activity
        createNewButton(newAction, getString(R.string.markaction), buttonHeight);
        // redraw
        Intent myPrefIntent = new Intent(this,mark.class);
        startActivity(myPrefIntent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        contextMenuItem = v; // stores button context menu called from // does not need to move to onPrepareContextMenu

        // submenus can NOT BE NESTED but they can be next to each other
        
        /* attach and create edit submenu */
        SubMenu editsub = menu.addSubMenu(R.string.editsubmenu);
        editsub.clear();
        editsub.add(3, R.id.renameaction, 3, R.string.renametitle);

        /* attach and create playlist submenu */
        // TODO: submenu in correct ordering
        //MenuItem bar = (MenuItem) findViewById(R.id.removeaction);
        //int foo = bar.getOrder();
        //Log.i(TAG, "foo: " + foo);
        SubMenu sub = menu.addSubMenu ( 1, 3, 0, R.string.playlistchange );
        sub.clear();
        sub.add( 0, R.id.playlistclear, 0, R.string.playlistclear );
        // I sure hope the list of playlists doesn't change on me but this function is my only choice

        /* populate submenu */
    	String playlistid = null;
    	String newListName = null;

    	Cursor cursor = getContentResolver().query(
    			MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
    			null, null, null, null);
    	if (cursor != null) {
    	 if (cursor.moveToFirst()) {
    	  do {
    	     playlistid = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
    	     newListName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
    	     //listPlay.add(newListName);
    	     //listIds.add(playlistid);
    	     Intent intent = new Intent();
    	     intent.putExtra("playlist", Long.parseLong(playlistid));
    	     // this is a fantastic intent trick from com/android/music/MusicUtils.java and MediaPlaybackActivity.java
    	     sub.add(2, R.id.playlistselected, Integer.parseInt(playlistid), newListName).setIntent(intent);
    	  } while (cursor.moveToNext());
    	     cursor.close();
    	 }
    	}
    }
    
// Doesn't Exist!!! WTF!?
//    @Override
//    public void onPrepareContextMenu(ContextMenu menu) {
//    	super.onPrepareOptionsMenu(menu);
//    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	// need these vars for playlistclear and playlistselected
        SharedPreferences myMgrPrefs = getSharedPreferences(sSetName, 0);
        Editor e = myMgrPrefs.edit();
        String theAction = (String) ((Button)contextMenuItem).getTag();
        // Handle item selection
        switch (item.getItemId()) {
        /* case R.id.editsubmenu: handled automatically */
        case R.id.renameaction:
            showDialog(R.layout.rename);
            return true;
        case R.id.removeaction:
            showDialog(R.layout.remove);
            return true;
        case R.id.moveup:
        	moveAction(theAction, "up");
            return true;
        case R.id.movedown:
        	moveAction(theAction, "down");
            return true;
        //case R.id.playlist: // done automatically
        case R.id.playlistclear:
        	/* set pl to null */
        	Log.i(TAG, "playlistclear from " + myMgrPrefs.getString(theAction + "pl", null) + " to null");
            e.putString(theAction + "pl", null);
            e.commit();
        	return true;
        case R.id.playlistselected:
            // this is a fantastic intent trick from com/android/music/MusicUtils.java and MediaPlaybackActivity.java
        	long sel = item.getIntent().getLongExtra("playlist", 0);
        	Log.i(TAG, "sel " + sel );
        	/* set pl setting to sel */
    	    Log.i(TAG, "playlistclear from " + myMgrPrefs.getString(theAction + "pl", null) + " to sel " + sel);
            e.putString(theAction + "pl", Long.toString(sel) );
            e.commit();
        	return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
    	LayoutInflater factory = LayoutInflater.from(mark.this);
    	//Dialog dialog;
        switch(id) {

        case R.layout.rename: //renameDialogInt: // from Context Item
        	textEntryView = factory.inflate(R.layout.rename, null);

        	/* return the constructed rename AlertDialog */
            // TODO: can enter be intercepted during dialog text entry?
        	return new AlertDialog.Builder(mark.this)
            .setTitle(R.string.renametitle) // add text of action
            .setView(textEntryView)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    /* User clicked OK */
                	EditText b = (EditText) textEntryView.findViewById(R.id.renametext);
                	final CharSequence ca;
                	ca = (CharSequence) b.getText();
                	// TODO: if result not null & ! equal to old result
                    /* change preference */
                	CharSequence newAction = (CharSequence) ((Button)contextMenuItem).getTag();
                	//final CharSequence x = ((Button)contextMenuItem).getText();
                    //Log.i(TAG, "change " + newAction + " from " + x + " to " + ca );
                    SharedPreferences myMgrPrefs = getSharedPreferences(sSetName, 0);
                    //Log.i(TAG, "mark myMgrPrefs before: " + myMgrPrefs.getAll().toString() );
                    Editor e = myMgrPrefs.edit();
                    e.putString( newAction.toString(), ca.toString());
                    e.commit();
                    //Log.i(TAG, "mark myMgrPrefs  after: " + myMgrPrefs.getAll().toString());
                	
                    /* change button */
                	((Button)contextMenuItem).setText(ca);
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    /* User clicked cancel so do nothing */
                }
            })
            //.setIcon(R.drawable.alert_dialog_icon)
            .create();

        case R.layout.remove:
        	View confirmView = factory.inflate(R.layout.remove, null);
        	
        	// confirm remove AlertDialog
        	return new AlertDialog.Builder(mark.this)
            .setTitle(R.string.removetitle)
            .setView(confirmView)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    /* User clicked OK */
                    /* change preferences by moving all down to fill the gap */
                	CharSequence oldAction = (CharSequence) ((Button)contextMenuItem).getTag();
                    SharedPreferences myMgrPrefs = getSharedPreferences(sSetName, 0);
                    int len = sizeWithoutPl(myMgrPrefs);
                	// assume string of exactly "actionX", X<10
                	int begin = Integer.parseInt(oldAction.subSequence(6, 7).toString());
                	//Log.i(TAG, "remove " + oldAction + ", move from " + begin + " to len " + len);
                    //Log.i(TAG, "mark myMgrPrefs before: " + myMgrPrefs.getAll().toString() );
                    Editor e = myMgrPrefs.edit();
                    for (int i = begin; i < len ; ++i) {
                        String newAction = "action" + i;
                        String movedAction = "action" + (i+1);
                        e.putString( newAction, myMgrPrefs.getString(movedAction, "error") ); // error if defaults
                    }
                    e.remove("action"+(len-1));
                    e.remove("action"+(len-1)+"pl");
                    e.commit();
                    //Log.i(TAG, "mark myMgrPrefs  after: " + myMgrPrefs.getAll().toString());

                    // redraw
                    Intent myRemovePrefIntent = new Intent(getBaseContext(), mark.class);
                    startActivity(myRemovePrefIntent);
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    /* User clicked cancel so do nothing */
                }
            })
            //.setIcon(R.drawable.alert_dialog_icon)
            .create();

        case R.id.chooseset:
        	View chooseSetView = factory.inflate(R.layout.choose_set, null);

        	// choose set AlertDialog
        	AlertDialog chooseme = new AlertDialog.Builder(mark.this)
            .setTitle(R.string.setmenutitle)
            .setView(chooseSetView)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    /* User clicked OK */
                	Log.i(TAG, "Clicked OK");
                	// If Clear, clear
                	// If New, give dialog to get the new name
                	// If selected, read in new set
                	//     and save new set name
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    /* User clicked cancel so do nothing */
                	Log.i(TAG, "Clicked cancel");
                }
            })
            //.setIcon(R.drawable.alert_dialog_icon)
            .create();

        	// dynamic soon, static for testing

        	// Prepare to add buttons
        	RadioGroup myList = (RadioGroup) chooseSetView.findViewById(R.id.setlist);

        	final LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.WRAP_CONTENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT);

        	// Add buttons
        	RadioButton newSet = new RadioButton(this);
        	newSet.setText(R.string.setadd);
        	//Log.i(TAG, " id " + newSet.getId()); // auto assigned
            myList.addView(newSet, 1, layoutParams);

        	newSet = new RadioButton(this);
        	newSet.setText(R.string.setclear);
        	myList.addView(newSet, 2, layoutParams);

            // activate the radio button of the current set
        	
            // hook up the listener for when selection changes
        	myList.setOnCheckedChangeListener(this);
        	
        	return chooseme;

        default:
            return null;
        }
    }
    
    @Override
    public void onCheckedChanged(RadioGroup arg0, int checkedId) {
    	radioSet = checkedId; // refresh global
        RadioButton rb = (RadioButton) arg0.findViewById(checkedId);
        // Perform action on the Set clicked // rb.getId() + ", " + rb.getText()
        Log.i(TAG, "Clicked radio button: " + rb.getText());
        // Only act on a Set name
        // Check if file does not exists
            // modify displayed Set/file name (new)
        // do nothing if it exists
    }

    @Override
    protected void onPrepareDialog(int id, Dialog d) {
        //super.onPrepareDialog(id, d);
        switch(id) {
        case R.layout.rename: //renameDialogInt: // from Context Item
        	// prepare default text of dialog box with button name
            View y = d.findViewById(R.id.renametext);
        	// check for default button name
    		if ( ! ((Button)contextMenuItem).getText().toString().equals(getString(R.string.markaction)) ) {
        	    ((TextView) y).setText(((Button)contextMenuItem).getText());
        	    // TODO: set cursor to end, highlight as well?
            //    Log.i(TAG, "CHANGED"); 
            //} else {
        	//	Log.i(TAG, "UNCHANGED");
            }
        }
    }
    
    private int sizeWithoutPl(SharedPreferences myMgrPrefs) {
    	// # of actions without playlist in the shared preferences
    	int total = myMgrPrefs.getAll().size(); // total   is size
        Set<String> baz = myMgrPrefs.getAll().keySet();
                                                // baz     is set of keys
        Iterator<String> bar = baz.iterator();  // bar     is loop variable
        int totalPl = 0;                        // totalPl is counter of playlist keys
        int i;                                  // i       is up counter
        String s;                               // s       is string to be tested
        for (i = 0; i < total; ++i) {
        	s = bar.next();
            if ( ! (s == null) ) {
                //Log.i(TAG, "s " + s);
            	if ( s.matches(".*pl") ) {
                    //Log.i(TAG, "s matched");
            	    totalPl = totalPl+1;
                }
            }
        }
        //Log.i(TAG, "sizeWithoutPl" + " total " + total + ", totalPl " + totalPl);
        if ((total - totalPl) == 0) {
        	return 1; // 0,0 or all must all be playlists
        } else {
            return ( total - totalPl );
        }
    }
    
    private int moveAction(CharSequence theAction, CharSequence direction) {
        SharedPreferences myMgrPrefs = getSharedPreferences(sSetName, 0);
        int len = sizeWithoutPl(myMgrPrefs);
	    // assume string of exactly "actionX", X<10
	    int begin = Integer.parseInt(theAction.subSequence(6, 7).toString());
        if (  ( (begin == len) && (direction == "up") ) || ( (begin == 1) && (direction == "down") ) ) {
            // impossible, TODO: dialog to notify of illegal action
        	return 1;
        }
	    //Log.i(TAG, "remove " + oldAction + ", move from " + begin + " to len " + len);
        //Log.i(TAG, "mark myMgrPrefs before: " + myMgrPrefs.getAll().toString() );
	    String tempAction = null;
	    // String tempPlaylist = null; // TODO: move playlists too
	    Editor e = myMgrPrefs.edit();
        if (direction == "up") { // list is top to bottom
        	tempAction = myMgrPrefs.getString("action" + (begin-1), "error");
            e.putString("action" + (begin-1), myMgrPrefs.getString("action" + (begin), "error"));
            e.putString("action" + (begin), tempAction);
        } else { // ASSUME direction == "down"
        	// increment
        	tempAction = myMgrPrefs.getString("action" + (begin+1), "error");
            e.putString("action" + (begin+1), myMgrPrefs.getString("action" + (begin), "error"));
            e.putString("action" + (begin), tempAction);            
        }
        //e.remove("action"+(len-1)+"pl");
        e.commit();
        //Log.i(TAG, "mark myMgrPrefs  after: " + myMgrPrefs.getAll().toString());

        // redraw
        Intent myRemovePrefIntent = new Intent(getBaseContext(), mark.class);
        startActivity(myRemovePrefIntent);
        return 1;
    }
    
    private static class ActiveHabitsName extends SQLiteOpenHelper {
    	private static final String DATABASE_NAME = "activehabitsname.db";
    	private static final int DATABASE_VERSION = 1;
    	private static final String TABLE_NAME = "name";
    	private final Context context;
    	private SQLiteDatabase db;
    	private static final String TABLE_CREATE =
    		"CREATE TABLE " + TABLE_NAME + " (name TEXT PRIMARY KEY);";

    	ActiveHabitsName(Context context) {
    		super(context, DATABASE_NAME, null, DATABASE_VERSION);
    		this.context = context;
    	}
    	
    	ActiveHabitsName(Context ctx, String defaultName) {
    		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    		this.context = ctx;
    		//db.execSQL("SELECT * FROM " + TABLE_NAME);
    		// if not key exists set to defaultName
    		// drop the table instead of searching?
    	}

    	@Override
    	public void onCreate(SQLiteDatabase indb) {
    		indb.execSQL(TABLE_CREATE);
    		this.db = indb;
    	}
    	
    	@Override
    	public void onUpgrade(SQLiteDatabase dbase, int oldVersion, int newVersion) {
    		dbase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    		onCreate(dbase);
    	}
    	
    	public String get() {
    		// return value
    		return "activehabits.txt"; // temp
    		// this.db.execSQL
        }

    	public void put(String name) {
    		// store value
        }
    }

};
