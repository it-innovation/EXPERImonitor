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

using System;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec
{

    /**
     * Logger is a wrapper class that offers ECC writers functions similar to common
     * Java logging libraries. Users of this class should use their own specific
     * logger implementation (Log4J, for example) in their particular software build.
     * 
     */
    public class Logger
    {
        private static ECCLoggerImpl loggerImpl = new ECCDefaultImpl();

        /**
         * Sets the logger implementation for the logger. Call this method before using
         * 'getLogger(..)' to retrieve actual loggers. For pre-existing implementations
         * provided in the ECC, see the impls package for the logging foundation.
         * 
         * @param impl - Wrapping implementation of a logger (derived from ECCLoggerImpl)
         */
        public static void setLoggerImpl(ECCLoggerImpl impl)
        {
            if (impl != null) loggerImpl = impl;
        }

        /**
         * Use this method to get a logger for the class specified.
         * 
         * @param c - Class to be logged.
         * @return 
         */
        public static IECCLogger getLogger(Type t)
        {
            return loggerImpl.createLogger(t);
        }
    }

} // namespace
