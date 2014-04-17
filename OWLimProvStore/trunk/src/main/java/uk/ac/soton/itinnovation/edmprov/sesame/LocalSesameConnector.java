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

/**
 * 
 * @author Vegard Engen
 */
public class LocalSesameConnector extends ASesameConnector
{
	
	public LocalSesameConnector() throws Exception
	{
		super();
	}
	
	public LocalSesameConnector(Properties props) throws Exception
	{
		super(props);
	}
	
	public void connectLocal() throws SesameException
	{
		// TODO: implement connectLocal() as required - some code reuse possibilities from the GettingStarted class?
	}

}
