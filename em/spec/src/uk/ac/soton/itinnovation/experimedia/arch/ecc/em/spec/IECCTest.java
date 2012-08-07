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
//      Created Date :          05-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec;



/**
 * This is test interface is not properly part of the EM monitoring life-cycle.
 * It has been developed for the purposes of supporting the testing unit associated
 * with the EM. Both producer and users of this interface can use this test
 * interface to send the other test data.
 * 
 * @author sgc
 */
public interface IECCTest
{
  /**
   * Sets a listener to the interface that allows data to be received by either
   * producer or user.
   * 
   * @param listener - IECCTest_Listener instance providing notification of test
   * events.
   */
  void setListener( IECCTest_Listener listener );
  
  /**
   * Use this method to send test data to either the user or producer instance of
   * this interface.
   * 
   * @param byteCount - Number of bytes expected to be sent in this method
   * @param dataBody  - Array of byte test data being sent
   */
  void sendData( int byteCount, byte[] dataBody );
}
