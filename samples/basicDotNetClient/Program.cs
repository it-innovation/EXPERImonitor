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
//      Created Date :          10-Apr-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.loggin.impl;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.basicDotNetClient;

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;




namespace SimpleHeadlessECCClient
{
    class Program
    {
        static void Main(string[] args)
        {
            Logger.setLoggerImpl(new Log4NetImpl());
            IECCLogger clientLogger = Logger.getLogger(typeof(Program));

            clientLogger.info("Starting Simple Headless ECC Client");

            ECCClientController ctrl = new ECCClientController();
            try
            {
                string rabbitServerIP = "127.0.0.1";
                if (args.Length == 1) rabbitServerIP = args[0];

                ctrl.start(rabbitServerIP,
                            new Guid("00000000-0000-0000-0000-000000000000"), // EM ID
                            Guid.NewGuid());                                 // ID of this client (random)
            }
            catch (Exception e)
            { clientLogger.error("Had a problem connecting to the EM:\n" + e.Message); }
        }
    }
}
