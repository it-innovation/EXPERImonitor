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
#include <boost/date_time/posix_time/posix_time_types.hpp>
#include <boost/container/list.hpp>
#include <boost/container/set.hpp>
#include <boost/container/map.hpp>

#define UTF_CHAR_SET "Latin1"

// Common types
typedef char                                      Byte;
typedef std::wstring                              String;
typedef boost::uuids::uuid                        UUID;
typedef boost::container::set<boost::uuids::uuid> UUIDSet;
typedef boost::posix_time::ptime                  TimeStamp;

// Inline utility functions
inline String toWide( const std::string& narrow );

inline std::string toNarrow( const String& wide );

inline std::string uuidToNarrow( const UUID& id );

inline String uuidToWide( const UUID& id );

inline const Byte* toUnManagedByteArray( const String& wide ); // User must manage memory

inline std::string fromByteArray( const Byte* byteArray );

inline String intToString( const int& i );

inline UUID createRandomUUID();

inline TimeStamp getCurrentTime();