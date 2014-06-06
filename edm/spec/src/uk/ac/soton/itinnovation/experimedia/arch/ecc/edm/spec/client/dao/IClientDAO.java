/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2014
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
//      Created By :            Simon Crowle
//      Created Date :          2014-02-25
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.client.dao;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;

import java.util.*;




public interface IClientDAO
{
	void clearAllClients() throws Exception;
	
	void addClientConnected( EMClient client ) throws Exception;
	
	Set<EMClient> getConnectedClients() throws Exception;
	
	boolean isClientConnected( UUID clientID ) throws Exception;
	
	void removeClientConnected( EMClient client ) throws Exception;
}