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
//      Created Date :          08-Apr-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec;

using System;
using System.Collections.Generic;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp
{

public class AMQPMessageDispatch : IAMQPMessageDispatch
{
  private readonly IECCLogger dispatchLogger = Logger.getLogger( typeof(AMQPMessageDispatch) );  
  private readonly Object     dispatchLock   = new Object();
  
  private AMQPMessageDispatchPump            dispatchPump;
  private Queue<KeyValuePair<string,byte[]>> dispatchQueue;
  private IAMQPMessageDispatchListener       dispatchListener;
  
  
  public AMQPMessageDispatch()
  {
    dispatchQueue = new Queue<KeyValuePair<string,byte[]>>();
  }

  public bool addMessage(string queueName, byte[] data)
  {
      bool addResult = false;

      if (queueName != null && data != null)
      {
          lock (dispatchLock)
          {
              dispatchQueue.Enqueue(new KeyValuePair<string, byte[]>(queueName, data));
              dispatchPump.notifyDispatchWaiting();
              addResult = true;
          }
      }

      return addResult;
  }

  public bool hasOutstandingDispatches()
  {
      bool outstanding;

      lock (dispatchLock)
      { outstanding = (dispatchQueue.Count > 0); }

      return outstanding;
  }

  public void iterateDispatch()
  {
      KeyValuePair<string, byte[]> nextMessage = new KeyValuePair<string,byte[]>();
      bool gotMessage = false;

      lock (dispatchLock)
      {
          if (dispatchQueue.Count > 0)
          {
              nextMessage = dispatchQueue.Dequeue();
              gotMessage = true;
          }
      }

      if (gotMessage && dispatchListener != null)
          dispatchListener.onSimpleMessageDispatched( nextMessage.Key,
                                                      nextMessage.Value );
  }
  
  // IAMQPMessageDispatch ------------------------------------------------------
  public void setListener(IAMQPMessageDispatchListener listener)
  { dispatchListener = listener; }
  
  public IAMQPMessageDispatchListener getListener()
  { return dispatchListener; }
  
  public void setPump( AMQPMessageDispatchPump pump )
  { dispatchPump = pump; }
}

} // namespace