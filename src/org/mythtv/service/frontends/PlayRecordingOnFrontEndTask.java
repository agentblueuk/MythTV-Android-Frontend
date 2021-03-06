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
package org.mythtv.service.frontends;

import org.mythtv.client.ui.preferences.LocationProfile;
import org.mythtv.services.api.v027.beans.Program;
import org.mythtv.service.util.NetworkHelper;
import org.mythtv.services.api.ApiVersion;
import org.mythtv.services.api.ETagInfo;
import org.mythtv.services.api.connect.MythAccessFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author Daniel Frey
 *
 */
public class PlayRecordingOnFrontEndTask extends AsyncTask<String, Void, Boolean> {

	private static final String TAG = PlayRecordingOnFrontEndTask.class.getSimpleName();

	private final Context mContext;
	private final LocationProfile mLocationProfile;
	private final Program mProgram;
	
	public PlayRecordingOnFrontEndTask( Context context, LocationProfile locationProfile, Program program ) {
		mContext = context;
		mLocationProfile = locationProfile;
		mProgram = program;
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Boolean doInBackground( String... params ) {
		Log.d( TAG, "doInBackground : enter" );
		
		if( null == mContext ) {
			throw new IllegalArgumentException( "Context is required" );
		}
		
		if( null == mLocationProfile ) {
			throw new IllegalArgumentException( "LocationProfile is required" );
		}

		if( null == mProgram ) {
			throw new IllegalArgumentException( "Program is required" );
		}
		
		boolean started = false;
		
		ApiVersion apiVersion = ApiVersion.valueOf( mLocationProfile.getVersion() );
		switch( apiVersion ) {
			case v025 :
				
				started = false;
				
				break;
			case v026 :
				
				started = false;
				
				break;
			case v027 :

				String url = params[ 0 ];
				
				if( !NetworkHelper.getInstance().isFrontendConnected( mContext, mLocationProfile, url ) ) {
					Log.w( TAG, "process : Frontend @ '" + url + "' is unreachable" );
					
					return false;
				}

				org.mythtv.services.api.v027.MythServicesTemplate mythServicesTemplateV27 = (org.mythtv.services.api.v027.MythServicesTemplate) MythAccessFactory.getServiceTemplateApiByVersion( apiVersion, url );

				if( null != mythServicesTemplateV27 ) {
					ResponseEntity<org.mythtv.services.api.Bool> responseV27 = mythServicesTemplateV27.frontendOperations().playRecording( mProgram.getChannel().getChanId(), mProgram.getRecording().getStartTs(), ETagInfo.createEmptyETag() );
					if( responseV27.getStatusCode().equals( HttpStatus.OK ) ) {

						if( null != responseV27.getBody() ) {

							started = responseV27.getBody().getValue();

						}

					}
				}
				
				break;
				
			default :
				
				started = false;

				break;
		}

		Log.d( TAG, "doInBackground : exit" );
		return started;
	}

}
