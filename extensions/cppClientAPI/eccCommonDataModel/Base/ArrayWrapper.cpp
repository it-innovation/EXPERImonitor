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
//      Created Date :          12-Jul-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "ArrayWrapper.h"

using namespace ecc_commonDataModel;

using namespace boost::container;


ArrayWrapper::ArrayWrapper()
{
}

ArrayWrapper::~ArrayWrapper()
{
}

void ArrayWrapper::addModel( ModelBase::ptr_t model )
{
  if ( model ) modelVector.push_back( model );
}

const vector<ModelBase::ptr_t>& ArrayWrapper::getModels()
{
  return modelVector;
}

// ModelBase -----------------------------------------------------------------
String ArrayWrapper::toJSON()
{
  String json = L"[";

  vector<ModelBase::ptr_t>::const_iterator mIt = modelVector.begin();
  while ( mIt != modelVector.end() )
  {
    ModelBase::ptr_t model = *mIt;

    json.append( model->toJSON() );

    ++mIt;
  }

  json.append( L"]" );

  return json;
}

void ArrayWrapper::fromJSON( const JSONTree& jsonTree )
{
  JSONTreeIt jtIt = jsonTree.begin();
  while ( jtIt != jsonTree.end() )
  {

    ++jtIt;
  }
}


String ArrayWrapper::toString()
{
  String serialised;

  vector<ModelBase::ptr_t>::const_iterator mIt = modelVector.begin();
  while ( mIt != modelVector.end() )
  {
    ModelBase::ptr_t model = *mIt;
    serialised.append( model->toString() );

    ++mIt;
  }

  return serialised;
}
