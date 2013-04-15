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
using System.Runtime.InteropServices;




namespace SimpleHeadlessECCClient
{
    class Program
    {
        private static IECCLogger          clientLogger;
        private static ECCClientController eccController;

        // Entry point
        static void Main(string[] args)
        {
            // Set up loggin for this application
            Logger.setLoggerImpl(new Log4NetImpl());
            clientLogger = Logger.getLogger(typeof(Program));
            clientLogger.info("Starting Simple Headless ECC Client");

            // Set up exit handler for this application
            SetConsoleCtrlHandler(new CtrlHandlerRoutine(CtrlHandler), true);

            // Create an ECC client controller and try connecting to a local RabbitMQ
            eccController = new ECCClientController();
            try
            {
                string rabbitServerIP = "127.0.0.1";                // Default (local) address of RabbitMQ server
                if (args.Length == 1) rabbitServerIP = args[0];

                eccController.start( rabbitServerIP,
                                     new Guid("00000000-0000-0000-0000-000000000000"), // ECC instance ID
                                     Guid.NewGuid());                                  // ID of this client (random)
            }
            catch (Exception e)
            { clientLogger.error("Had a problem connecting to the EM:\n" + e.Message); }
        }

        // Exit point
        private static bool CtrlHandler(CtrlTypes ctrlTypes)
        {
            // Issue good-bye notification (if possible)
            if (clientLogger != null) clientLogger.info("Closing down client");
            if (eccController != null) eccController.stop();

            clientLogger = null;
            eccController = null;

            return true;
        }

        // Native application event handling --------------------------------------------------
        #region unmanaged

        [DllImport("Kernel32")]
        public static extern bool SetConsoleCtrlHandler(CtrlHandlerRoutine Handler, bool Add);

        public delegate bool CtrlHandlerRoutine(CtrlTypes CtrlType);

        public enum CtrlTypes
        {
            CTRL_C_EVENT = 0,
            CTRL_BREAK_EVENT,
            CTRL_CLOSE_EVENT,
            CTRL_LOGOFF_EVENT = 5,
            CTRL_SHUTDOWN_EVENT
        }

        #endregion
        // Native application event handling --------------------------------------------------
    }
}
