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
//      Created Date :          20-Mar-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec;



/**
 * ECCLoggerImpl is an abstract class from which logging implementations for the
 * ECC API derive. For examples see ECCDefaultImpl and also the Log4JImpl classes.
 * 
 */
public abstract class ECCLoggerImpl implements IECCLogger
{
    /**
     * Send info level message.
     * 
     * @param msg - Message to log.
     */
    @Override
    public abstract void info( Object msg );

    /**
     * Send a warn level message.
     * 
     * @param msg - Message to log.
     */
    @Override
    public abstract void warn( Object msg );

    /**
     * Send a debug level message.
     * 
     * @param msg - Message to log.
     */
    @Override
    public abstract void debug( Object msg );

    /**
     * Send a debug level message with throwable info.
     * 
     * @param msg - Message to send
     * @param t   - Thrown object for this message
     */
    @Override
    public abstract void debug( Object msg, Throwable e );

    /**
     * Send an error level message.
     * 
     * @param msg - Message to log.
     */
    @Override
    public abstract void error( Object msg );

    /**
     * Send an error level message with throwable info.
     * 
     * @param msg - Message to log.
     * @param t   - Thrown object for this message.
     */
    @Override
    public abstract void error( Object msg, Throwable e );

    /**
     * Send a fatal level message.
     * 
     * @param msg - Message to log.
     */
    @Override
    public abstract void fatal( Object msg );

    /**
     * Send a fatal level message with throwable info.
     * 
     * @param msg - Message to log.
     * @param t   - Thrown object for this message.
     */
    @Override
    public abstract void fatal( Object msg, Throwable e );
    
    /**
     * This method returns an instance of the implemented logger wrapper with
     * a reference to the class to log against.
     * 
     * @param c - Class which is the source of the logging message.
     * @return  - An instance of the implemented logger (implementing IECClogger)
     */
    protected abstract ECCLoggerImpl createLogger( Class c );
}
