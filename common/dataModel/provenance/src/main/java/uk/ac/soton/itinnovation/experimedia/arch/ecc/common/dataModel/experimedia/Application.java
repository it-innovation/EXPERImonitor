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
//      Created By :            Stefanie Wiegand
//      Created Date :          2014-07-30
//      Created for Project :   Experimedia
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experimedia;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMActivity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMAgent;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMEntity;

/**
 * An application which is connected to the ECC as a client.
 */
public class Application {

	//the prov agent representation of the application
	public EDMAgent agent;
	//the prov entity representation of the same application
	public EDMEntity entity;
	//the activity of running the application. Ususally started on application startup and ended on application shutdown.
	public EDMActivity activity;

}
