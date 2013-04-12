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
//      Created Date :          22-Mar-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec;

using System;

using log4net;
using log4net.Config;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.common.loggin.impl
{

/**
 * Log4NetImpl is an implementation of the IECCLogger class for the use with the
 * ECC APIs. Use this implementation for builds that are compatible with Apache's
 * Log4j environments.
 * 
 */
public class Log4NetImpl : ECCLoggerImpl
{
    private static bool isConfigured = false;

    private ILog log4jLogger; // Instance of the Apache Log4j logger

    public Log4NetImpl()
    {
        if (!Log4NetImpl.isConfigured)
        {
            XmlConfigurator.Configure();
            isConfigured = true;
        }
    }

    public override ECCLoggerImpl createLogger( Type t )
    {
        // Create a new instance of this implementation & set up the actual logger
        Log4NetImpl impl = new Log4NetImpl();
        impl.log4jLogger = LogManager.GetLogger(t);
        
        return impl;
    }

    public override void info( Object msg )
    {
        // Echo this method to the actual implementation
        log4jLogger.Info( msg );
    }

    public override void warn( Object msg )
    {
        // Echo this method to the actual implementation
        log4jLogger.Warn( msg );
    }

    public override void debug( Object msg )
    {
        // Echo this method to the actual implementation
        log4jLogger.Debug( msg );
    }

    public override void debug( Object msg, Exception e )
    {
        // Echo this method to the actual implementation
        log4jLogger.Debug( msg, e );  
    }

    public override void error( Object msg )
    {
        // Echo this method to the actual implementation
        log4jLogger.Error( msg );
    }

    public override void error(Object msg, Exception e)
    {
        // Echo this method to the actual implementation
        log4jLogger.Error( msg, e );  
    }

    public override void fatal( Object msg )
    {
        // Echo this method to the actual implementation
        log4jLogger.Fatal( msg );  
    }

    public override void fatal(Object msg, Exception e)
    {
        // Echo this method to the actual implementation
        log4jLogger.Fatal( msg, e );  
    }
}

} // namespace
