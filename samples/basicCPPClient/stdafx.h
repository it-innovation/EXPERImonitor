#pragma once

#include "ECCUtils.h"


// Windows includes ------------------------------------------------
#ifdef WIN32

#include "targetver.h"

#define WIN32_LEAN_AND_MEAN

// 4996 Disable warnings for some BOOST libraries
// boost::uuids::uuid iterator overrun

#endif