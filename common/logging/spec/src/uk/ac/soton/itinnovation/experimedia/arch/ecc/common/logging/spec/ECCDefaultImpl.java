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
//      Created for Project :   Logging foundation
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec;



/**
 * ECCDefaultImpl is a generic (silent) logging implementation of the IECCLogger
 * interface. It is strongly recommended that you supply another logger implementation
 * for the ECC API (such as the Log4Impl provided).
 * 
 */
class ECCDefaultImpl extends ECCLoggerImpl
{  
  @Override
  public ECCLoggerImpl createLogger( Class c )
  {
    return new ECCDefaultImpl();
  }
  
  @Override
  public void info( Object msg )
  {}
  
  @Override
  public void warn( Object msg )
  {}
  
  @Override
  public void debug( Object msg )
  {}
  
  @Override
  public void debug( Object msg, Throwable e )
  {}
  
  @Override
  public void error( Object msg )
  {}
  
  @Override
  public void error( Object msg, Throwable e )
  {}
  
  @Override
  public void fatal( Object msg )
  {}
  
  @Override
  public void fatal( Object msg, Throwable e )
  {}
}
