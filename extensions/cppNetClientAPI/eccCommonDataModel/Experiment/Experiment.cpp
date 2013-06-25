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
//      Created Date :          21-June-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "Experiment.h"

using namespace boost::uuids;
using namespace std;




namespace ecc_commonDataModel
{

Experiment::Experiment()
  : ModelBase()
{
    //uuid = Guid.NewGuid();
    //metricGenerators = new HashSet<MetricGenerator>();
}
    
/**
  * Copy constructor; does a deep copy of the sets of entities and metric generators.
  * @param ex Experiment object a copy should be made from.
  */
Experiment::Experiment( Experiment::ptr_t ex ) 
  : ModelBase()
{
    /*if (ex == null)
        return;
        
    if (ex.uuid != null)
        this.uuid = new Guid( ex.uuid.ToString() );
        
    experimentID = ex.experimentID;
    name         = ex.name;
    description  = ex.description;

    if (ex.startTime != null)
        startTime = new DateTime(ex.startTime.Ticks);

    if (ex.endTime != null)
        endTime = new DateTime(ex.endTime.Ticks);

    metricGenerators = new HashSet<MetricGenerator>();
        
    if (ex.metricGenerators != null)
    {
        foreach (MetricGenerator mg in ex.metricGenerators)
        {
            if (mg != null)
                metricGenerators.Add(new MetricGenerator(mg));
        }
    }*/
}
    
Experiment::Experiment( const boost::uuids::uuid        uuid, 
                        const std::wstring&             experimentID, 
                        const std::wstring&             name, 
                        const std::wstring&             description,
                        const boost::posix_time::ptime& creationTime )
  : ModelBase()
{
  //TODO
    //this.uuid         = uuid;
    //this.experimentID = experimentID;
    //this.name         = name;
    //this.description  = description;
    //this.startTime    = creationTime;
}
    
Experiment::Experiment( const boost::uuids::uuid        uuid, 
                        const std::wstring&             experimentID, 
                        const std::wstring&             name, 
                        const std::wstring&             description, 
                        const boost::posix_time::ptime& creationTime, 
                        const boost::posix_time::ptime& endTime )
  : ModelBase()
{
  //TODO
    //this.endTime = endTime;
}

// ModelBase -----------------------------------------------------------------
void Experiment::toJSON( wstring& jsonStrOUT )
{
}

void Experiment::fromJSON( const wstring& jsonStr )
{
}

wstring Experiment::toString()
{
  wstring ts;

  return ts;
}

} // namespace
