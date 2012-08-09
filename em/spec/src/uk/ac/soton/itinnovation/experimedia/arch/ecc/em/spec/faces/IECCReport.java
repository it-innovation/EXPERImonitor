/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2012
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
//      Created Date :          09-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces;




public interface IECCReport
{
  // Listeners -----------------------------------------------------------------
  
  
  // Provider methods ----------------------------------------------------------
  void requestReportSummary();
  
  void requestNextReportMetadata();
  
  void requestDataPart( /* data model */ );
  
  void notifyReportTimeOut( /* data model */ );
  
  // User methods --------------------------------------------------------------
  void notifyReadyToReport();
  
  void sendReportSummary( /* data model here */ );
  
  void sendReportMetaData( /* data model here */ );
  
  void sendDataPart( /* data model here */ );
}
