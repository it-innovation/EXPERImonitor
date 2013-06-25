#pragma once

#include "Utils.h"
#include <boost\shared_ptr.hpp>


// Windows includes ------------------------------------------------
#ifdef WIN32

#include "targetver.h"

#define WIN32_LEAN_AND_MEAN
#define UTF_CHAR_SET        "Latin1"

typedef unsigned char byte;

// Warnings disabled in VC 2010 project:

// 4996 Disable warnings for some BOOST libraries
// boost::uuids::uuid iterator overrun

#endif











