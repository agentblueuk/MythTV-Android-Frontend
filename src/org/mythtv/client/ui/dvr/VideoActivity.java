/**
 * This file is part of MythTV Android Frontend
 *
 * MythTV Android Frontend is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MythTV Android Frontend is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MythTV Android Frontend.  If not, see <http://www.gnu.org/licenses/>.
 *
 * This software can be found at <https://github.com/MythTV-Clients/MythTV-Android-Frontend/>
 */
package org.mythtv.client.ui.dvr;
	
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import org.joda.time.DateTime;
import org.mythtv.R;
import org.mythtv.client.ui.AbstractMythtvFragmentActivity;
import org.mythtv.client.ui.preferences.LocationProfile;
import org.mythtv.db.content.LiveStreamDaoHelper;
import org.mythtv.db.dvr.RecordedDaoHelper;
import org.mythtv.service.content.GetLiveStreamTask;
import org.mythtv.services.api.v027.beans.LiveStreamInfo;
import org.mythtv.services.api.v027.beans.Program;

/**
 * @author John Baab
 * 
 */
public class VideoActivity extends AbstractMythtvFragmentActivity implements GetLiveStreamTask.TaskFinishedListener {

	private static final String TAG = VideoActivity.class.getSimpleName();
	private static final String DISMISS = "org.mythtv.videoActivity.dismissDialog";
	
	public static final String EXTRA_CHANNEL_ID = "org.mythtv.client.ui.dvr.programGroup.EXTRA_CHANNEL_ID";
	public static final String EXTRA_START_TIME = "org.mythtv.client.ui.dvr.programGroup.EXTRA_START_TIME";
	public static final String EXTRA_RAW = "org.mythtv.client.ui.dvr.programGroup.EXTRA_RAW";
	
	private ProgressDialog progressDialog;

	private LiveStreamDaoHelper mLiveStreamDaoHelper = LiveStreamDaoHelper.getInstance();
	private RecordedDaoHelper mRecordedDaoHelper = RecordedDaoHelper.getInstance();
	
	private Program program = null;
	private LiveStreamInfo liveStreamInfo = null;
	
	private LocationProfile mLocationProfile;
	
	// ***************************************
	// Activity methods
	// ***************************************

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		Log.v( TAG, "onCreate : enter" );
		super.onCreate( savedInstanceState );

	    setContentView( R.layout.activity_video );
	    
	    int channelId = getIntent().getIntExtra( EXTRA_CHANNEL_ID, -1 );
	    Long startTime = getIntent().getLongExtra( EXTRA_START_TIME, -1 );
	    boolean raw = getIntent().getBooleanExtra( EXTRA_RAW, false );
	    
	    mLocationProfile = mLocationProfileDaoHelper.findConnectedProfile( this );
	    
	    program = mRecordedDaoHelper.findOne( this, mLocationProfile, channelId, new DateTime( startTime ) );
	    liveStreamInfo = mLiveStreamDaoHelper.findByProgram( this, mLocationProfile, program );
	    
	    if( null != program ) {

	    	if( raw ) {
	    		
	    		startVideo( true );
	    		
	    	} else {
	    		
	    		try {
	    			progressDialog = ProgressDialog.show( VideoActivity.this, getString( R.string.please_wait_title_str ), getString( R.string.video_playback_retrieving ), true, true );
	    		} catch( Exception e ) {
	    			Log.w( TAG, "onCreate : error", e );
	    		}
    		
	    		checkLiveStreamInfo();
	    	
	    	}
	    	
	    }
	    
		Log.v( TAG, "onCreate : exit" );
	}
	
	/* (non-Javadoc)
	 * @see org.mythtv.client.ui.dvr.AbstractDvrActivity#onResume()
	 */
	@Override
	protected void onResume() {
		Log.v( TAG, "onResume : enter" );
		super.onResume();

		Log.v( TAG, "onResume : exit" );
	}

	/* (non-Javadoc)
	 * @see org.mythtv.client.ui.dvr.AbstractDvrActivity#onPause()
	 */
	@Override
	protected void onPause() {
		Log.v( TAG, "onPause : enter" );
		super.onPause();

		Log.v( TAG, "onPause : exit" );
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onRestart()
	 */
	protected void onRestart() {
		Log.v( TAG, "onRestart : enter" );
		super.onStop();

		finish();
		
		Log.v( TAG, "onRestart : exit" );
	}

	/* (non-Javadoc)
	 * @see org.mythtv.client.ui.dvr.AbstractDvrActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		Log.v( TAG, "onDestroy : enter" );
		super.onDestroy();

		if( null != progressDialog && progressDialog.isShowing() ) {
			progressDialog.dismiss();
		}
		progressDialog = null;
		
		Log.v( TAG, "onDestroy : exit" );
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState( Bundle outState ) {
		Log.v( TAG, "onSaveInstanceState : enter" );
		super.onSaveInstanceState( outState );

		Log.v( TAG, "onSaveInstanceState : exit" );
	}

	// internal helpers
	
	private void startVideo( boolean raw ) {
		Log.v( TAG, "startVideo : enter" );
		
		Log.v( TAG, "startVideo : program=" + program.toString() );
		
		String temp = mLocationProfile.getUrl();
		temp = temp.replaceAll( "/$", "" );
		String url = "";
		if( raw ) {
			url = temp + "/Content/GetFile?StorageGroup=" + program.getRecording().getStorageGroup() + "&FileName=" + program.getFileName();
		} else {
			url = temp + liveStreamInfo.getRelativeURL();
		}
		
	    Log.v( TAG, "URL: " + url );
	    
	    if( null != progressDialog ) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	    
	    final Intent tostart = new Intent( Intent.ACTION_VIEW );
//	    tostart.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
//	    tostart.setDataAndType( Uri.parse(url), "application/x-mpegurl" );
//	    tostart.setDataAndType( Uri.parse(url), "application/vnd.apple.mpegurl" );
	    tostart.setDataAndType( Uri.parse( url ), "video/*" );

	    if( !preferences.getBoolean( DISMISS, false ) ) {
			Log.v( TAG, "startVideo : displaying warning" );
	    
			View dismissView = View.inflate( this, R.layout.dismiss_checkbox, null );
			CheckBox dismiss = (CheckBox) dismissView.findViewById( R.id.dismiss );
			dismiss.setOnCheckedChangeListener( new OnCheckedChangeListener() {

			    @Override
			    public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) {

			    	setDismissPreferences( isChecked );
			    	
			    }
			    
			});
			dismiss.setText( getString( R.string.video_playback_doNotDisplay ) );

			AlertDialog.Builder builder = new AlertDialog.Builder( this );
	    	builder
	    	.setTitle( R.string.video_playback_title )
	    	.setMessage( R.string.video_playback_message )
	    	.setView( dismissView )
	    	.setPositiveButton( R.string.video_playback_button_play, new DialogInterface.OnClickListener() {

	    		@Override
	    		public void onClick( DialogInterface dialog, int which ) {

	    			startActivity( tostart );

	    		}
	    	} )
	    	.setNegativeButton( R.string.video_playback_button_cancel, new DialogInterface.OnClickListener() {

	    		@Override
	    		public void onClick( DialogInterface dialog, int which ) {
	    		
	    			finish();
	    		
	    		}
	    		
	    	} )
	    	.show();
	    } else {
			Log.v( TAG, "startVideo : not displaying warning" );

	        startActivity( tostart );

	    }
	    
		Log.v( TAG, "startVideo : exit" );
	}
	
	private void setDismissPreferences( boolean isChecked ) {
		
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean( DISMISS, isChecked );
		editor.commit();
		
	}
	
	public void checkLiveStreamInfo() {
		Log.v( TAG, "checkLiveStreamInfo : enter" );

		if( liveStreamInfo.getPercentComplete() < 2 || liveStreamInfo.getPercentComplete() <= 2 ) {
			Log.v( TAG, "checkLiveStreamInfo : stream not ready" );
			
			startUpdateStreamService();
			
		} else {
			Log.v( TAG, "checkLiveStreamInfo : starting video playback" );

			startVideo( false );
			
		}

		Log.v( TAG, "checkLiveStreamInfo : exit" );
	}
	
	private void startUpdateStreamService() {
		Log.v( TAG, "startUpdateStreamService : enter" );
		
		GetLiveStreamTask getLiveStreamTask = new GetLiveStreamTask( this, program, mLocationProfile, this );
		getLiveStreamTask.execute( liveStreamInfo.getId() );

		Log.v( TAG, "startUpdateStreamService : exit" );
	}
	
	private void notConnectedNotify() {
		
		Toast.makeText( this, getResources().getString( R.string.notification_not_connected ), Toast.LENGTH_SHORT ).show();
		
	}

	/* (non-Javadoc)
	 * @see org.mythtv.service.content.GetLiveStreamTask.TaskFinishedListener#onGetLiveStreamTaskStarted()
	 */
	@Override
	public void onGetLiveStreamTaskStarted() {
		Log.i( TAG, "onGetLiveStreamTaskStarted : enter" );

		Log.i( TAG, "onGetLiveStreamTaskStarted : exit" );
	}

	/* (non-Javadoc)
	 * @see org.mythtv.service.content.GetLiveStreamTask.TaskFinishedListener#onGetLiveStreamTaskFinished(org.mythtv.db.content.model.LiveStreamInfo)
	 */
	@Override
	public void onGetLiveStreamTaskFinished( LiveStreamInfo result ) {
		Log.i( TAG, "onGetLiveStreamTaskFinished : enter" );

		if( null != result ) {
			
			startUpdateStreamService();

		}
		
		Log.i( TAG, "onGetLiveStreamTaskFinished : exit" );
	}

}
