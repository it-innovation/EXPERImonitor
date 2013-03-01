/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2011
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
//      Created By :            sgc
//      Created Date :          26 Aug 2011
//      Created for Project :   ROBUST
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.mvc;

import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.types.UFException;

import java.util.UUID;


public interface IUFController
{
  /**
   * Retrieve a read-only model from the controller
   * 
   * @param id - the unique identifier of the model
   * @return   - a read-only view of the model
   */
  IUFModelRO getModel( UUID id ) throws UFException;
  
  /**
   * Retrieve a view by search of its model
   * 
   * @param model - model search term
   * @return      - returns this controller's view of the model
   */
  IUFView getView( IUFModelRO model ) throws UFException;
  
  /**
   * Retrieve a view based on its ID (it may not have any models associated with it)
   * 
   * @param ID - String ID value
   * @return   - returns this controller's view by the ID
   */
  IUFView getView( String ID );
}