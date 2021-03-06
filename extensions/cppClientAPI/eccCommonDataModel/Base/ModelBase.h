/////////////////////////////////////////////////////////////////////////
//
// � University of Southampton IT Innovation Centre, 2012
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
//      Created Date :          20-June-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#pragma once

#include "ECCUtils.h"

#include <boost/property_tree/json_parser.hpp>




namespace ecc_commonDataModel
{

  class ModelBase
  {
  public:

    typedef boost::shared_ptr<ModelBase> ptr_t;

    typedef boost::property_tree::ptree JSONTree;

    virtual ~ModelBase();

    virtual String toJSON() =0;

    virtual void fromJSON( const JSONTree& jsonTree ) =0;

    virtual String toString() =0;

  protected:

    ModelBase();

  };

}