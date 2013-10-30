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
//      Created By :            Simon Crowle
//      Created Date :          27-Sep-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance;

import java.util.*;

public class EDMProvReport
{
  private UUID statementID;
  private Date statementCreationDate;
  
  private HashMap<String, EDMProvBaseElement> provElements;
  
  public EDMProvReport()
  {
    statementID           = UUID.randomUUID();
    statementCreationDate = new Date();
    provElements          = new HashMap<String, EDMProvBaseElement>();
  }
  
  public UUID getID()
  { return statementID; }
  
  public Date getCopyOfDate()
  { return new Date( statementCreationDate.getTime() ); }
  
  public EDMProvBaseElement getElement( String iriID )
  {
    return provElements.get( iriID );
  }
  
  public HashMap<String, EDMProvBaseElement> getProvElements()
  {
    return provElements;
  }
  
  // Protected methods ---------------------------------------------------------
  protected EDMProvReport( HashMap<String, EDMProvBaseElement> pElements )
  {
    this();
    
    if ( pElements != null && !pElements.isEmpty() )
      provElements.putAll( pElements );
  }
}
