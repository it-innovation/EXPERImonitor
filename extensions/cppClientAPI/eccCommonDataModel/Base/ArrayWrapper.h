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

#pragma once

#include "ModelBase.h"



namespace ecc_commonDataModel
{

  class ArrayWrapper : public ModelBase
  {
  public:

    typedef boost::shared_ptr<ArrayWrapper> ptr_t;
    
    ArrayWrapper();
    
    virtual ~ArrayWrapper();

    void addModel( ModelBase::ptr_t model );

    const boost::container::vector<ModelBase::ptr_t>& getModels(); 

    // ModelBase
    virtual String toJSON();

    virtual void fromJSON( const JSONTree& jsonTree );

    virtual String toString();

  private:
    boost::container::vector<ModelBase::ptr_t> modelVector;

  };

} // namespace

