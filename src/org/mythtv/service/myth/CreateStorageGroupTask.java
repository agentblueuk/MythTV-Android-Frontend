/**
 * 
 */
package org.mythtv.service.myth;

import org.mythtv.client.ui.preferences.LocationProfile;
import org.mythtv.services.api.ApiVersion;
import org.mythtv.services.api.connect.MythAccessFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import android.os.AsyncTask;
import android.util.Log;

/**
 * @author Daniel Frey
 *
 */
public class CreateStorageGroupTask extends AsyncTask<String, Void, Boolean> {

	private static final String TAG = CreateStorageGroupTask.class.getSimpleName();
	
	private final LocationProfile mLocationProfile;
	private final TaskFinishedListener listener;
	
	public interface TaskFinishedListener {

		void onCreateStorageGroupTaskStarted();
		 
	    void onCreateStorageGroupTaskFinished( boolean result );
	    
	}

	public CreateStorageGroupTask( LocationProfile locationProfile, TaskFinishedListener listener ) {
		this.mLocationProfile = locationProfile;
		this.listener = listener;
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
    protected void onPreExecute() {
		Log.d( TAG, "onPreExecute : enter" );
		
        listener.onCreateStorageGroupTaskStarted();

        Log.d( TAG, "onPreExecute : exit" );
    }
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Boolean doInBackground( String... params ) {
		Log.d( TAG, "doInBackground : enter" );

		if( null == mLocationProfile ) {
			throw new IllegalArgumentException( "LocationProfile is required" );
		}

		if( null == listener ) {
			throw new IllegalArgumentException( "TaskFinishedListener is required" );
		}

		if( null == params ) {
			throw new IllegalArgumentException( "String params are required" );
		}

		boolean created = false;
		
		if( !MythAccessFactory.isServerReachable( mLocationProfile.getUrl() ) ) {
			Log.w( TAG, "process : Master Backend '" + mLocationProfile.getHostname() + "' is unreachable" );
			
			return false;
		}

		String groupName = params[ 0 ];
		String directory = params[ 1 ];
		
		ApiVersion apiVersion = ApiVersion.valueOf( mLocationProfile.getVersion() );
		switch( apiVersion ) {
			case v026 :
				
				org.mythtv.services.api.v026.MythServicesTemplate mythServicesTemplateV26 = (org.mythtv.services.api.v026.MythServicesTemplate) MythAccessFactory.getServiceTemplateApiByVersion( apiVersion, mLocationProfile.getUrl() );

				ResponseEntity<org.mythtv.services.api.v026.Bool> responseV26 = mythServicesTemplateV26.mythOperations().addStorageGroupDir( groupName, directory, mLocationProfile.getHostname() );
				if( responseV26.getStatusCode().equals( HttpStatus.OK ) ) {
					
					if( null != responseV26.getBody() ) {
					
						created = responseV26.getBody().getBool().booleanValue();
					
					}
					
				}
				
				break;
			case v027 :

				org.mythtv.services.api.v027.MythServicesTemplate mythServicesTemplateV27 = (org.mythtv.services.api.v027.MythServicesTemplate) MythAccessFactory.getServiceTemplateApiByVersion( apiVersion, mLocationProfile.getUrl() );

				ResponseEntity<org.mythtv.services.api.Bool> responseV27 = mythServicesTemplateV27.mythOperations().addStorageGroupDir( groupName, directory, mLocationProfile.getHostname() );
				if( responseV27.getStatusCode().equals( HttpStatus.OK ) ) {
					
					if( null != responseV27.getBody() ) {
					
						created = responseV27.getBody().getValue();
					
					}
					
				}
				
				break;
				
			default :
				
				created = false;

				break;
		}

		Log.d( TAG, "doInBackground : exit" );
		return created;
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute( Boolean result ) {
		Log.d( TAG, "onPostExecute : enter" );
		super.onPostExecute( result );

		Log.d( TAG, "onPostExecute : result=" + result );
		listener.onCreateStorageGroupTaskFinished( result );
		
		Log.d( TAG, "onPostExecute : exit" );
	}

}