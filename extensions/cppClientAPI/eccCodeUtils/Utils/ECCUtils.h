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

#pragma once

// Common includes
#include <stddef.h>

#include <boost/shared_ptr.hpp>
#include <boost/locale.hpp>
#include <boost/uuid/uuid.hpp>
#include <boost/uuid/string_generator.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/container/list.hpp>
#include <boost/container/set.hpp>
#include <boost/container/map.hpp>
#include <boost/container/vector.hpp>
#include <boost/property_tree/ptree.hpp>

#define UTF_CHAR_SET "Latin1"

// Common types
typedef char                                      Byte;
typedef std::wstring                              String;
typedef boost::uuids::uuid                        UUID;
typedef boost::container::set<boost::uuids::uuid> UUIDSet;
typedef boost::posix_time::ptime                  TimeStamp;

typedef boost::property_tree::ptree                 JSONTree;
typedef boost::property_tree::ptree::const_iterator JSONTreeIt;
typedef boost::property_tree::ptree::value_type     JSONValue;

// Inline utility functions
extern inline String toWide( const std::string& narrow );

extern inline std::string toNarrow( const String& wide );

extern inline std::string uuidToNarrow( const UUID& id );

extern inline String uuidToWide( const UUID& id );

extern inline const Byte* toUnManagedByteArray( const String& wide ); // User must manage memory

extern inline std::string fromByteArray( const Byte* byteArray );

extern inline String intToString( const int& i );

extern inline String longToString( const long& l );

extern inline int stringToInt( const String& s );

extern inline UUID createRandomUUID();

extern inline UUID createUUID( const String& idValue );

extern inline TimeStamp getCurrentTime();

extern inline String timeStampToString( const TimeStamp& ts );

extern inline TimeStamp stringToTimeStamp( const String& tString );

extern inline String getJSON_String( const boost::property_tree::ptree::value_type& vt );

extern inline int getJSON_int( const boost::property_tree::ptree::value_type& vt );

extern inline bool getJSON_bool( const boost::property_tree::ptree::value_type& vt );

extern inline UUID getJSON_UUID( const boost::property_tree::ptree::value_type& vt );

extern inline TimeStamp getJSON_TimeStamp( const boost::property_tree::ptree::value_type& vt );

extern inline String createJSON_Prop( const String& prop, const String& val );

extern inline String createJSON_Prop( const String& prop, const long& val );

extern inline String createJSON_Prop_bool( const String& prop, const bool& val );