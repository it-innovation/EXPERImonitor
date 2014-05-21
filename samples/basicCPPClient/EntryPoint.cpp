/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2013
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
//      Created Date :          08-Jul-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#include "stdafx.h"

#include "ECCClientController.h"
#include "ECCUtils.h"

#include <tchar.h>
#include <iostream>

using namespace std;


int _tmain( int argc, _TCHAR* argv[] )
{
  wcout << L"Starting Simple Headless CPP client" << endl;

  // Create client controller and try to connect to the Rabbit
  ECCClientController::ptr_t clientController = ECCClientController::ptr_t( new ECCClientController() );

  // Default IP or use command line supplied
  String rabbitServerIP = L"127.0.0.1";

  if ( argc == 2 ) 
    rabbitServerIP = String( argv[1] );

  try
  {
    clientController->start( rabbitServerIP,
                             createUUID( L"00000000-0000-0000-0000-000000000000" ), // ECC instance ID
                             createRandomUUID() );                                  // ID of this client (random)

    wcout << L"Press any key to quit" << endl;                       
  }
  catch( const String e )
  { wcout << "Had problems starting client: " << e << endl; }

  wcout << "Hit ENTER to quit demo" << endl;
  wcin.get();

  clientController->stop();

  wcout << "Goodbye" << endl;

	return 0;
}

