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
#include <boost/uuid/uuid_io.hpp>

#include <string>

using namespace boost::uuids;
using namespace boost::locale::conv;
using namespace std;

// Inline utility functions

String toWide( const std::string& narrow )
{ return to_utf<wchar_t>( narrow, UTF_CHAR_SET ); }

std::string toNarrow( const String& wide )
{ return from_utf( wide, UTF_CHAR_SET ); }

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

const Byte* toUnManagedByteArray( const String& wide )
{
  const Byte* byteArray = NULL;

  if ( wide.size() > 0 )
  {
    const string narrow = toNarrow( wide );

    byteArray = narrow.c_str();
  }

  return byteArray;
}

std::string fromByteArray( const Byte* byteArray )
{
  return to_utf<char>( byteArray, UTF_CHAR_SET );
}

String intToString( const int& i )
{
  String result;

  wchar_t sValue[64];
  _itow( i, sValue, 10 );

  return String( sValue );
}

static boost::uuids::random_generator uuidGenerator;

UUID createRandomUUID()
{ return uuidGenerator(); }

TimeStamp getCurrentTime()
{ return boost::posix_time::second_clock::local_time(); }