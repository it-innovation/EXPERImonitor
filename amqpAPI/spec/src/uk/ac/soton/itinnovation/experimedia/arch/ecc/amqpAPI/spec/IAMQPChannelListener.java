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
//      Created Date :          09-Dec-2013
//      Created for Project :   experimedia-arch-ecc-amqpAPI-spec
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec;




public interface IAMQPChannelListener
{
  /**
   * Notifies listener of connectivity closure. If the connection supporting the
   * current channel is still open, the boolean flag will be set to true
   * 
   * @param connectionOK - Indicates whether the AMQP connection is still OK
   * @param reason       - RabbitMQ information on reason for channel closure
   */
  void onChannelShutdown( boolean connectionOK, String reason );
}
