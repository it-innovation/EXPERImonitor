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
 * ECC wrapping interface for loggers. Provides a shim to common logging methods
 * used in other libraries. Software writers wishing to write new implementations
 * of ECC loggers must implement these basic methods.
 *
 */
public interface IECCLogger
{
    /**
     * Send info level message.
     * 
     * @param msg - Message to log.
     */
    void info( Object msg );

    /**
     * Send a warn level message.
     * 
     * @param msg - Message to log.
     */
    void warn( Object msg );

    /**
     * Send a debug level message.
     * 
     * @param msg - Message to log.
     */
    void debug( Object msg );

    /**
     * Send a debug level message with throwable info.
     * 
     * @param msg - Message to send
     * @param t   - Thrown object for this message
     */
    void debug( Object msg, Throwable t );

    /**
     * Send an error level message.
     * 
     * @param msg - Message to log.
     */
    void error( Object msg );

    /**
     * Send an error level message with throwable info.
     * 
     * @param msg - Message to log.
     * @param t   - Thrown object for this message.
     */
    void error( Object msg, Throwable t );

    /**
     * Send a fatal level message.
     * 
     * @param msg - Message to log.
     */
    void fatal( Object msg );

    /**
     * Send a fatal level message with throwable info.
     * 
     * @param msg - Message to log.
     * @param t   - Thrown object for this message.
     */
    void fatal( Object msg, Throwable t );
}
