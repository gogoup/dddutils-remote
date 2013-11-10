package org.gogoup.dddutils.appsession.spi;


/**
 * This interface class specifies the behaviors of how upper layers access the persistent data.
 * 
 * Generalization class needs to provide persistent functions for session states.
 * 
 * @author ruisun
 *
 */
		
public interface AppSessionSPI {
	
	/**
	 * Check if the giving session key has been taken under the same application id.
	 * 
	 * @param applicationId String
	 * @param sessionKey String
	 * @return boolean Return true if the key has been taken, otherwise, return false.
	 */
	public boolean hasSession(String applicationId, String sessionKey);
	
	/**
	 * Save the giving session object segment to persistent media.
	 * 
	 * @param os AppSessionOS
	 */
	public void insert(AppSessionOS os);
	
	/**
	 * Update the exiting session object segment with the specified one.
	 * 
	 * @param os AppSessionOS
	 */
	public void update(AppSessionOS os);
	
	/**
	 * Delete the specified session object segment from persistent media
	 * 
	 * @param os AppSessionOS
	 */
	public void delete(AppSessionOS os);
	
	/**
	 * Return a session object segment with the giving session key under the specified application id.
	 * 
	 * @param applicationId String
	 * @param sessionKey String
	 * @return AppSessionOS Return null if there is no such session exits.
	 */
	public AppSessionOS selectByKey(String applicationId, String sessionKey);

}
