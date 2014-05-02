/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2013
//
// Copyright in this software belongs to University of Southampton
// IT Innovation Centre of Gamma House, Enterprise Road, 
// Chilworth Science Park, Southampton, SO16 7NS, UK.
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
//      Created By :            Vegard Engen
//      Created Date :          2013-11-05
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.edmprov.sesame;

import java.util.Properties;
import uk.ac.soton.itinnovation.edmprov.owlim.common.SesameException;
import org.openrdf.repository.manager.RemoteRepositoryManager;

/**
 * 
 * @author Vegard Engen
 */
public class RemoteSesameConnector extends ASesameConnector
{
	/**
     * Connect to a remote Sesame repository service.
     * Code from org.openrdf.console.Console
     * @param url
	 * @throws uk.ac.soton.itinnovation.edmprov.owlim.common.SesameException 
     */
    public RemoteSesameConnector(String url) throws SesameException 
    {
		super();
        connect(url, null, null);
    }
	
	/**
     * Connect to a remote Sesame repository service.
     * Code from org.openrdf.console.Console
	 * @param props
     * @param url
	 * @throws uk.ac.soton.itinnovation.edmprov.owlim.common.SesameException 
     */
    public RemoteSesameConnector(Properties props, String url) throws SesameException 
    {
		super(props);
        connect(url, null, null);
    }
	
	/**
     * Connect to a remote Sesame repository service.
     * Code from org.openrdf.console.Console
     * @param url
     * @param user
     * @param pass
	 * @throws uk.ac.soton.itinnovation.edmprov.owlim.common.SesameException 
     */
    public RemoteSesameConnector(final String url, final String user, String pass) throws SesameException 
    {
		super();
		connect(url, user, pass);
	}
	
	/**
     * Connect to a remote Sesame repository service.
     * Code from org.openrdf.console.Console
	 * @param props
     * @param url
     * @param user
     * @param pass
	 * @throws uk.ac.soton.itinnovation.edmprov.owlim.common.SesameException 
     */
    public RemoteSesameConnector(Properties props, final String url, final String user, String pass) throws SesameException 
    {
		super(props);
		connect(url, user, pass);
	}

    /**
     * Connect to a remote Sesame repository service.
     * Code from org.openrdf.console.Console
     * @param url
     * @param user
     * @param pass
     * @return 
     */
    private void connect(final String url, final String user, String pass) throws SesameException
    {
        if (pass == null) {
            pass = "";
        }
/*
        try {
            
            HTTPClient httpClient = new HTTPClient();
            try {
                httpClient.setServerURL(url);

                if (user != null) {
                    httpClient.setUsernameAndPassword(user, pass);
                }

                // Ping the server
                httpClient.getServerProtocol();
            } finally {
                httpClient.shutDown();
            }
			
            
        } catch (UnauthorizedException e) {
            if ((user == null) || pass.isEmpty()) {
                log.error("Failed to connect to repository server - username or password is NULL");
            }
            if (user != null) {
                log.error("Failed to connect to repository server - authentication for user '" + user + "' failed", e);
            }
            throw new SesameException("Failed to connect to the server", e);
        } catch (IOException e) {
            log.error("Failed to access the server", e);
            throw new SesameException("Failed to access the server", e);
        } catch (RepositoryException e) {
            log.error("Failed to access the server", e);
            throw new SesameException("Failed to access the server", e);
        }
*/		
		RemoteRepositoryManager newManager = new RemoteRepositoryManager(url);
		newManager.setUsernameAndPassword(user, pass);
		installNewManager(newManager, url);
    }
}
