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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.loggin.impl;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.*;



/**
 * Log4JImpl is an implementation of the IECCLogger class for the use with the
 * ECC APIs. Use this implementation for builds that are compatible with Apache's
 * Log4j environments.
 * 
 */
public class Log4JImpl extends ECCLoggerImpl
{
    private org.apache.log4j.Logger log4jLogger; // Instance of the Apache Log4j logger


    @Override
    public ECCLoggerImpl createLogger( Class c )
    {
      // Create a new instance of this implementation & set up the actual logger
      Log4JImpl impl = new Log4JImpl();
      impl.log4jLogger = org.apache.log4j.Logger.getLogger( c );

      return impl;
    }

    @Override
    public void info( Object msg )
    {
        // Echo this method to the actual implementation
        log4jLogger.info( msg );
    }

    @Override
    public void warn( Object msg )
    {
        // Echo this method to the actual implementation
        log4jLogger.warn( msg );
    }

    @Override
    public void debug( Object msg )
    {
        // Echo this method to the actual implementation
        log4jLogger.debug( msg );
    }

    @Override
    public void debug( Object msg, Throwable e )
    {
        // Echo this method to the actual implementation
        log4jLogger.debug( msg, e );  
    }

    @Override
    public void error( Object msg )
    {
        // Echo this method to the actual implementation
        log4jLogger.error( msg );
    }

    @Override
    public void error( Object msg, Throwable e )
    {
        // Echo this method to the actual implementation
        log4jLogger.error( msg, e );  
    }

    @Override
    public void fatal( Object msg )
    {
        // Echo this method to the actual implementation
        log4jLogger.fatal( msg );  
    }

    @Override
    public void fatal( Object msg, Throwable e )
    {
        // Echo this method to the actual implementation
        log4jLogger.fatal( msg, e );  
    }
}
