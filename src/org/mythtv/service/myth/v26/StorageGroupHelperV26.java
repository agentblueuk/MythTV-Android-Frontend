/**
 * 
 */
package org.mythtv.service.myth.v26;

import java.util.ArrayList;
import java.util.List;

import org.mythtv.client.ui.preferences.LocationProfile;
import org.mythtv.db.AbstractBaseHelper;
import org.mythtv.db.myth.model.StorageGroupDirectory;
import org.mythtv.services.api.ApiVersion;
import org.mythtv.services.api.ETagInfo;
import org.mythtv.services.api.connect.MythAccessFactory;
import org.mythtv.services.api.v026.MythServicesTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import android.util.Log;

/**
 * @author Daniel Frey
 *
 */
public class StorageGroupHelperV26 extends AbstractBaseHelper {

	private static final String TAG = StorageGroupHelperV26.class.getSimpleName();
	
	private static final ApiVersion mApiVersion = ApiVersion.v026;
	
	private static MythServicesTemplate mMythServicesTemplate;

	public static List<StorageGroupDirectory> process( final LocationProfile locationProfile, String storageGroupName ) {
		Log.v( TAG, "process : enter" );
		
		if( !MythAccessFactory.isServerReachable( locationProfile.getUrl() ) ) {
			Log.w( TAG, "process : Master Backend '" + locationProfile.getHostname() + "' is unreachable" );
			
			return null;
		}
		
		mMythServicesTemplate = (MythServicesTemplate) MythAccessFactory.getServiceTemplateApiByVersion( mApiVersion, locationProfile.getUrl() );
		
		List<StorageGroupDirectory> storageGroupDirectories = null;

		try {

			storageGroupDirectories = downloadStorageGroups( locationProfile, storageGroupName );
			
		} catch( Exception e ) {
			Log.e( TAG, "process : error", e );
		
			storageGroupDirectories = null;
		}

		Log.v( TAG, "process : exit" );
		return storageGroupDirectories;
	}

	// internal helpers
	
	private static List<StorageGroupDirectory> downloadStorageGroups( final LocationProfile locationProfile, final String storageGroupName ) {
		Log.v( TAG, "downloadStorageGroups : enter" );
	
		List<StorageGroupDirectory> storageGroupDirectories = null;

		ResponseEntity<org.mythtv.services.api.v026.beans.StorageGroupDirectoryList> responseEntity = mMythServicesTemplate.mythOperations().getStorageGroupDirectories( storageGroupName, locationProfile.getHostname(), ETagInfo.createEmptyETag() );

		if( responseEntity.getStatusCode().equals( HttpStatus.OK ) ) {

			org.mythtv.services.api.v026.beans.StorageGroupDirectoryList storageGroupDirectoryList = responseEntity.getBody();

			if( null != storageGroupDirectoryList.getStorageGroupDirectories() ) {
			
				if( null != storageGroupDirectoryList.getStorageGroupDirectories().getStorageGroupDirectories() && !storageGroupDirectoryList.getStorageGroupDirectories().getStorageGroupDirectories().isEmpty() ) {
					storageGroupDirectories = load( storageGroupDirectoryList.getStorageGroupDirectories().getStorageGroupDirectories() );	
				}

			}

		}

		Log.v( TAG, "downloadStorageGroups : exit" );
		return storageGroupDirectories;
	}
	
	private static List<StorageGroupDirectory> load( List<org.mythtv.services.api.v026.beans.StorageGroupDirectory> versionStorageGroupDirectories ) {
		Log.v( TAG, "load : enter" );
		
		List<StorageGroupDirectory> storageGroupDirectories = new ArrayList<StorageGroupDirectory>();
		
		if( null != versionStorageGroupDirectories && !versionStorageGroupDirectories.isEmpty() ) {
			
			for( org.mythtv.services.api.v026.beans.StorageGroupDirectory versionStorageGroupDirectory : versionStorageGroupDirectories ) {
				
				StorageGroupDirectory storageGroupDirectory = new StorageGroupDirectory();
				storageGroupDirectory.setId( versionStorageGroupDirectory.getId() );
				storageGroupDirectory.setGroupName( versionStorageGroupDirectory.getGroupName() );
				storageGroupDirectory.setDirectoryName( versionStorageGroupDirectory.getDirectoryName() );
				storageGroupDirectory.setHostname( versionStorageGroupDirectory.getHostname() );

				storageGroupDirectories.add( storageGroupDirectory );
			}
			
		}
		
		Log.v( TAG, "load : exit" );
		return storageGroupDirectories;
	}

}