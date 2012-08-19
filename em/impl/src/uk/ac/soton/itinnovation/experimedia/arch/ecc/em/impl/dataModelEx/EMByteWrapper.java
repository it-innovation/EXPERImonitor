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
//      Created Date :          19-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx;

import sun.misc.*;




public class EMByteWrapper
{
  private BASE64Encoder base64Encoder;
  private BASE64Decoder base64Decoder;
  
  public EMByteWrapper()
  {
    base64Encoder  = new BASE64Encoder();
    base64Decoder = new BASE64Decoder();
  }
  
  public String encode( byte[] data )
  {
    String code = null;
    
    if ( data != null && data.length > 0 )
      code = base64Encoder.encodeBuffer( data );
    
    return code;
  }
  
  public byte[] decode( String data )
  {
    byte[] dataBytes = null;
    
    if ( data != null && !data.isEmpty() )
      try { dataBytes = base64Decoder.decodeBuffer( data ); }
      catch (Exception e) { dataBytes = null; }
    
    return dataBytes;
  }
}
