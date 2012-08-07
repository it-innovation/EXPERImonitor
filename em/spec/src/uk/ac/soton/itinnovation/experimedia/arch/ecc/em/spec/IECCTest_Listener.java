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
 * The is a symmetric listener (for either producer or user to use) that notifies
 * when the producer/user has sent some data. This listener is only intended to be
 * used with the IECCTest interface and is not a 'proper' part of the EM monitoring
 * life-cycle.
 * 
 * @author sgc
 */
public interface IECCTest_Listener
{
  /**
   * Notification of some data received.
   * 
   * @param dataSize - Number of bytes sent in this event
   * @param dataBody - Array of byte data sent
   */
  void onReceivedData( int byteCount, byte[] dataBody );
}
