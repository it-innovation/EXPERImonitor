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
//      Created Date :          17-June-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#include "stdafx.h"

#include "ECCUtils.h"

#include <boost/locale.hpp>
#include <boost/uuid/random_generator.hpp>
#include <boost/uuid/string_generator.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/property_tree/json_parser.hpp>

#include <string>

using namespace boost::uuids;
using namespace boost::locale::conv;
using namespace std;

// Inline utility functions

String toWide( const std::string& narrow )
{ 
  String target;
  
  target.assign( narrow.begin(), narrow.end() );

  return target;
}

std::string toNarrow( const String& wide )
{ 
  std::string target;

  target.assign( wide.begin(), wide.end() );

  return target; 
}

string uuidToNarrow( const UUID& id )
{
  stringstream ss;
  ss << id;

  return ss.str();
}

String uuidToWide( const UUID& id )
{
  wstringstream ws;
  ws << id;

  return ws.str();
}

String intToString( const int& i )
{
  String result;

  wstringstream ws;
  ws << i ;

  return ws.str();
}

String longToString( const long& l )
{
  String result;

  wstringstream ws;
  ws << l ;

  return ws.str();
}

int stringToInt( const String& s )
{ return stoi( s ); }

UUID createRandomUUID()
{
  boost::uuids::random_generator uuidRandomGenerator;

  return uuidRandomGenerator(); 
}

UUID createUUID( const String& idValue )
{ 
  boost::uuids::string_generator uuidStringGenerator;

  return uuidStringGenerator( idValue );
}

TimeStamp getCurrentTime()
{
  return boost::posix_time::microsec_clock::local_time(); 
}

String timeStampToString( const TimeStamp& ts )
{
  // Must fiddle around with this format for correct ECC JSON
  string timeVal = to_iso_extended_string( ts );
  boost::replace_all( timeVal, "T", " ");

  std::size_t fracIndex = timeVal.find( '.' );
  if ( fracIndex == string::npos )
    timeVal.append( ".000" );
  else
    timeVal = timeVal.substr( 0, fracIndex + 4 );

  return toWide( timeVal );
}

TimeStamp stringToTimeStamp( const String& tString )
{
  return boost::posix_time::time_from_string( toNarrow(tString) );
}

String getJSON_String( const boost::property_tree::ptree::value_type& vt )
{
  string jv = vt.second.get_value<std::string>();

  return toWide( jv );
}

int getJSON_int( const boost::property_tree::ptree::value_type& vt )
{
  string jv = vt.second.get_value<std::string>();

  return stoi( jv );
}

bool getJSON_bool( const boost::property_tree::ptree::value_type& vt )
{
  return vt.second.get_value<bool>();
}

UUID getJSON_UUID( const boost::property_tree::ptree::value_type& vt )
{
  return vt.second.get_value<UUID>();
}

TimeStamp getJSON_TimeStamp( const boost::property_tree::ptree::value_type& vt )
{
  String timeVal = getJSON_String( vt );

  return stringToTimeStamp( timeVal );
}

String createJSON_Prop( const String& prop, const String& val )
{
  String json( L"\"" + prop + L"\":" );

  json.append( L"\"" + val + L"\"" );

  return json;
}

String createJSON_Prop( const String& prop, const long& val )
{
  String json( L"\"" + prop + L"\":" );

  json.append( longToString(val) );

  return json;
}

String createJSON_Prop_bool( const String& prop, const bool& val )
{
  String json( L"\"" + prop + L"\":" );

  json.append( val ? L"true" : L"false" );

  return json;
}
