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
//      Created Date :          08-Jul-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;

using RabbitMQ.Client;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp
{
  public class AMQPConnectionFactory
  {
    private IPAddress amqpHostIP;
    private IConnection amqpConnection;

    private HashSet<AMQPBasicChannel> amqpChannels;

    public AMQPConnectionFactory()
    {
      amqpChannels = new HashSet<AMQPBasicChannel>();
    }

    public Boolean setAMQPHostIPAddress(String addr)
    {
      Boolean ipSuccess = false;

      try
      {
        amqpHostIP = IPAddress.Parse(addr);
        ipSuccess = true;
      }
      catch (ArgumentException ane) { amqpHostIP = null; }
      catch (FormatException fe) { amqpHostIP = null; }

      return ipSuccess;
    }

    // TODO: Re-do this - it's AWFUL
    public String getLocalIP()
    {
      String localIPValue = null;

      try
      {
        IPHostEntry entry = Dns.GetHostEntry(Dns.GetHostName());
        IPAddress[] addresses = entry.AddressList;

        foreach (IPAddress addr in addresses)
        {
          // Get first 4 Quad address
          String ipString = addr.ToString();
          if (ipString.Split('.').Count() == 4)
          {
            localIPValue = ipString;
            break;
          }
        }
      }
      catch (System.Net.Sockets.SocketException se) {}

      return localIPValue;
    }

    public void connectToAMQPHost()
    {
      // Safety first
      if (amqpHostIP == null) throw new Exception("AMQP Host IP not correct");
      if (amqpConnection != null) throw new Exception("Already connected to host");

      ConnectionFactory amqpFactory = new ConnectionFactory();
      amqpFactory.HostName = amqpHostIP.ToString();

      amqpConnection = amqpFactory.CreateConnection();
      if (amqpConnection == null) throw new Exception("Could not create AMQP connection");
    }

    public Boolean isConnectionValid()
    { return (amqpConnection != null); }

    public void disconnectAMSQPHost()
    {
      foreach (AMQPBasicChannel channel in amqpChannels)
        channel.close();      

      amqpChannels.Clear();
      if (amqpConnection != null) amqpConnection.Close();
    }

    public AMQPBasicChannel createNewChannel()
    {
      if (amqpConnection == null) throw new Exception("No AMSQP connection available");

      AMQPBasicChannel bc = new AMQPBasicChannel(amqpConnection.CreateModel());
      amqpChannels.Add(bc);

      return bc;
    }
  }
}
