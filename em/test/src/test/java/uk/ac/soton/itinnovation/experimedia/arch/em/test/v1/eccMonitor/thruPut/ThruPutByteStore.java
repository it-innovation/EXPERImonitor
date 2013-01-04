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
//      Created Date :          04-Jan-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccMonitor.thruPut;

import java.util.HashMap;
import java.util.Random;




public class ThruPutByteStore
{
  private final int[]  streamSizes;
  private final int[]  streamPushes;
  private final HashMap<Integer, byte[]> testByteStreams;
  
  
  public ThruPutByteStore( int[] sSizes, int[] sPushes )
  {
    streamSizes  = sSizes;
    streamPushes = sPushes;
    
    testByteStreams = new HashMap<Integer, byte[]>();
    Random rand     = new Random();
    
    for ( int size : streamSizes )
    {
      byte[] stream = new byte[size];
      rand.nextBytes( stream );
      testByteStreams.put( size, stream );
    }
  }
  
  public int[] getStreamSizes()
  { return streamSizes; }
  
  public int[] getStreamPushes()
  { return streamPushes; }
  
  public byte[] getByteStreamOfSize( int size )
  { return testByteStreams.get( size ); }
  
  public boolean validateByteData( int sSize, byte[] dataBody )
  {
    boolean countFound  = false;
    boolean arraySizeOK = false;
    boolean bytesOK     = true;
    
    // First check to see if size is one that is expected
    for ( int size : streamSizes )
      if ( sSize == size )
      {
        countFound = true;
        break;
      }
    
    // Then check claimed size against actual array
    if ( countFound && (dataBody.length == sSize) )
      arraySizeOK = true;
    
    // Finally check contents
    if ( arraySizeOK )
    {
      byte[] srcBytes = testByteStreams.get( sSize );
      
      // Run through bytes
      for ( int i = 0; i < sSize; i++ )
        if ( srcBytes[i] != dataBody[i] )
        {
          bytesOK = false;
          break;
        }
    }
    
    return ( countFound == arraySizeOK == bytesOK == true );
  }
}
