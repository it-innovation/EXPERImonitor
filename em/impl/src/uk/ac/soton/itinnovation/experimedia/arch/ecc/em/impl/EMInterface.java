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
//      Created By :            sgc
//      Created Date :          29-Jul-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.eccInterface.*;

import org.yaml.snakeyaml.Yaml;
import java.util.*;




public class EMInterface implements IEMInterface,
                                    MessageDispatchListener
{
  private String  interfaceName;
  private String  interfaceVersion;
  private boolean isProvider;
  private Yaml    yamlUtil;
  
  private AbstractECCInterface amqpInterface;
  
  private HashMap<String, EMMethod>  methodMap;
  private HashMap<Integer, EMMethod> eventMap;
  
  IEMEventListener eventListener;
  
  
  public EMInterface( String iName,
                      String iVersion,
                      boolean asProvider,
                      Yaml yaml )
  {
    methodMap     = new HashMap<String, EMMethod>();
    eventMap      = new HashMap<Integer, EMMethod>();
    isProvider    = asProvider;
    yamlUtil      = yaml;
    
    // AMQP Interface is initialised by setter
  }
  
  public EMInterface( EMInterface iFace )
  {
    methodMap = new HashMap<String, EMMethod>();
    methodMap.putAll( iFace.methodMap );
    
    eventMap = new HashMap<Integer, EMMethod>();
    eventMap.putAll( iFace.eventMap );
    
    isProvider = iFace.isProvider;
    yamlUtil   = iFace.yamlUtil;
    
    // AMQP Interface is initialised by setter
  }
  
  public void initialiseAMQP( AbstractECCInterface eccIFace )
  {
    amqpInterface = eccIFace;
    
    if ( amqpInterface != null )
    {
      ECCInterfaceMessageDispatch dispatch = new ECCInterfaceMessageDispatch();
      dispatch.start( this );
      amqpInterface.setMessageDispatch( dispatch );
    }
  }
  
  public void addMethod( EMMethod method ) throws Exception
  {
    if ( method == null ) throw new Exception( "Supplied method is NULL" );
    if ( methodMap.containsKey( method.getMethodName() )) throw new Exception( "Method's name already exists" );
    if ( method.getIndex() == null ) throw new Exception( "Methods's index is NULL" );
    
    methodMap.put( method.getMethodName(), method );
  }
  
  public void addEventMethod( EMMethod method ) throws Exception
  {
    if ( method == null ) throw new Exception( "Supplied event method is NULL" );
    if ( eventMap.containsKey( method.getIndex() )) throw new Exception( "Event method's index already exists" );
    if ( method.getIndex() == null ) throw new Exception( "Event methods's index is NULL" );
    
    eventMap.put( method.getIndex(), method );
  }
  
  // IEMInterface --------------------------------------------------------------
  @Override
  public String getName()
  { return interfaceName; }
  
  @Override
  public String getVersion()
  { return interfaceVersion; }
  
  @Override
  public boolean isProvider()
  { return isProvider; }
  
  @Override
  public List<String> getAvailableMethods()
  { return enumMethodNames( methodMap.values() ); }
  
  @Override
  public List<String> getAvailableEvents()
  { return enumMethodNames( eventMap.values() ); }
  
  @Override
  public IEMMethod createMethod( String methodName ) throws Exception
  {
    if ( !methodMap.containsKey(methodName) ) throw new Exception( "Method does not exist" );
    
    // Copy method
    return new EMMethod( methodMap.get(methodName), amqpInterface );
  }
  
  @Override
  public void setEventListener( IEMEventListener listener ) throws Exception
  {
    if ( listener == null ) throw new Exception( "Listener is NULL" );
    
    eventListener = listener;
  }
  
  // MessageDispatchListener ---------------------------------------------------
  @Override
  public void onSimpleMessageDispatched( String queueName, byte[] data )
  {
    if ( eventListener != null )
    {
      EMMethodPayload empl = (EMMethodPayload) yamlUtil.load( new String(data) );

      if ( empl != null )
        eventListener.onEvent( empl.getIndex(), empl.getParameters() );
    }
  }
  
  // Private methods -----------------------------------------------------------
  private List<String> enumMethodNames( Collection<EMMethod> methodCol )
  {
    ArrayList<String>  methodNames = new ArrayList();
    Iterator<EMMethod> methodIt    = methodCol.iterator();
    
    while ( methodIt.hasNext() )
      methodNames.add( methodIt.next().getEventName() );
    
    return methodNames;
  }
}
