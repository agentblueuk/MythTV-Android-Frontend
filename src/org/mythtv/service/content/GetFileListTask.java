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
/**
 * 
 */
package org.mythtv.service.content;

import java.util.List;

import org.mythtv.client.ui.preferences.LocationProfile;
import org.mythtv.service.content.v27.FileListHelperV27;
import org.mythtv.services.api.ApiVersion;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author Daniel Frey
 *
 */
public class GetFileListTask extends AsyncTask<String, Void, List<String>> {

	private static final String TAG = GetFileListTask.class.getSimpleName();
	
	private final Context mContext;
	private final LocationProfile mLocationProfile;
	private final TaskFinishedListener listener;
	
	public interface TaskFinishedListener {

		void onGetFileListTaskStarted();
		 
	    void onGetFileListTaskFinished( List<String> result );
	    
	}

	public GetFileListTask( Context context, LocationProfile locationProfile, TaskFinishedListener listener ) {
		this.mContext = context;
		this.mLocationProfile = locationProfile;
		this.listener = listener;
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
    protected void onPreExecute() {
		Log.d( TAG, "onPreExecute : enter" );
		
        listener.onGetFileListTaskStarted();

        Log.d( TAG, "onPreExecute : exit" );
    }

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected List<String> doInBackground( String... params ) {
		Log.d( TAG, "doInBackground : enter" );

		if( null == mLocationProfile ) {
			throw new IllegalArgumentException( "LocationProfile is required" );
		}

		if( null == listener ) {
			throw new IllegalArgumentException( "TaskFinishedListener is required" );
		}

		if( null == params || params.length != 1 ) {
			throw new IllegalArgumentException( "Param is required" );
		}

		List<String> files = null;
		
		String storageGroupName = params[ 0 ];
		
		ApiVersion apiVersion = ApiVersion.valueOf( mLocationProfile.getVersion() );
		switch( apiVersion ) {
			case v027 :

				files = FileListHelperV27.getInstance().process( mContext, mLocationProfile, storageGroupName );
				
				break;
				
			default :
				
				files = FileListHelperV27.getInstance().process( mContext, mLocationProfile, storageGroupName );

				break;
		}

		Log.d( TAG, "doInBackground : exit" );
		return files;
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute( List<String> result ) {
		Log.d( TAG, "onPostExecute : enter" );
		super.onPostExecute( result );

		listener.onGetFileListTaskFinished( result );
		
		Log.d( TAG, "onPostExecute : exit" );
	}

}
