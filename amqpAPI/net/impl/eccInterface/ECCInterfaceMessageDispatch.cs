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
//      Created By :            sgc
//      Created Date :          11-Jul-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.commsAPI.impl.eccInterface
{
  public class ECCInterfaceMessageDispatch
  {
    private Object dispatchLock = new Object();

    private Thread dispatchThread;
    private LinkedList<KeyValuePair<String, byte[]>> dispatchList;
    private Boolean dispatchRunning;
    private IMessageDispatchListener dispatchListener;


    public ECCInterfaceMessageDispatch()
    {
      dispatchList = new LinkedList<KeyValuePair<String, byte[]>>();
      dispatchThread = new Thread(new ThreadStart(this.dispatchMessages));
      dispatchThread.Priority = ThreadPriority.BelowNormal;
    }

    public Boolean start(IMessageDispatchListener listener)
    {
      if (listener == null) return false;

      dispatchListener = listener;
      dispatchRunning = true;
      dispatchThread.Start();

      return true;
    }

    public void stop()
    {
      lock (dispatchLock)
      { dispatchRunning = false; }
    }

    public Boolean isRunning()
    { return dispatchRunning; }

    public Boolean addMessage(String queueName, byte[] data)
    {
      Boolean addResult = false;

      if (queueName != null && data != null)
      {
        lock (dispatchLock)
        {
          if (dispatchRunning)
          {
            dispatchList.AddLast(
              new KeyValuePair<String, byte[]>(queueName, data));

            addResult = true;
          }
        }
      }

      return addResult;
    }

    // Runnable ------------------------------------------------------------------
    protected void dispatchMessages()
    {
      KeyValuePair<String, byte[]> amqpMessage = new KeyValuePair<String, byte[]>();

      while (dispatchRunning)
      {  
        bool gotMessage = false;

        lock (dispatchLock)
        {
          if (dispatchList.Count > 0)
          {
            amqpMessage = dispatchList.First();
            dispatchList.RemoveFirst();
            gotMessage = true;
          }
        }

        // Dispatch next message
        if (gotMessage)
          dispatchListener.onSimpleMessageDispatched( amqpMessage.Key,
                                                      amqpMessage.Value);
      }
    }
  }
}
