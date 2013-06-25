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

#pragma once

#include "MetricGenerator.h"

#include <boost/uuid/uuid.hpp>
#include <boost/date_time/posix_time/posix_time_types.hpp>




namespace ecc_commonDataModel
{

/**
 * An experiment is the top-level class that all metric generators (and entities)
 * are part of.
 * 
 * An experiment can consist of many different entities that are monitored, some
 * of which could be system components, virtual resources or human beings. Each of
 * which have attributes that can be monitored, which are specified for each entity.
 * 
 * @author Vegard Engen
 */
class Experiment : ModelBase
{
public:

  typedef boost::shared_ptr<Experiment> ptr_t;

  /**
    * Default constructor which sets a random UUID for the object instance.
    */
  Experiment();
    
  /**
    * Copy constructor; does a deep copy of the sets of entities and metric generators.
    * @param ex Experiment object a copy should be made from.
    */
  Experiment( Experiment::ptr_t ex );
    
  /**
    * Constructor to set the basic information about the experiment likely known
    * at the start.
    * @param uuid UUID used to uniquely identify an experiment in this framework.
    * @param experimentID An experiment ID, as per the facility the experiment is/was running in.
    * @param name Name of the experiment.
    * @param description A description of the experiment.
    * @param creationTime The time stamp when the experiment was started.
    */
  Experiment( const boost::uuids::uuid        uuid, 
              const std::wstring&             experimentID, 
              const std::wstring&             name, 
              const std::wstring&             description,
              const boost::posix_time::ptime& creationTime );
    
  /**
    * Constructor to set all the information about the experiment.
    * @param uuid UUID used to uniquely identify an experiment in this framework.
    * @param experimentID An experiment ID, as per the facility the experiment is/was running in.
    * @param name Name of the experiment.
    * @param description A description of the experiment.
    * @param creationTime The time stamp when the experiment was started.
    * @param endTime The time stamp when the experiment ended.
    */
  Experiment( const boost::uuids::uuid        uuid, 
              const std::wstring&             experimentID, 
              const std::wstring&             name, 
              const std::wstring&             description, 
              const boost::posix_time::ptime& creationTime, 
              const boost::posix_time::ptime& endTime );

  /**
    * Getter/Setter for the experiment's unique ID
    */
  boost::uuids::uuid getUUID();

  void setUUID( const boost::uuids::uuid& ID );

  /**
    * Getter/Setter for the experiment's human readable ID
    */
  std::wstring getExperimentID();

  void setExperimentID( const std::wstring& ID );

  /**
    * Getter/Setter for the experiment's name
    */
  std::wstring getName();

  void setName( const std::wstring& name );

  /**
    * Getter/Setter for the experiment description
    */
  std::wstring getDescription();

  void setDescription( const std::wstring& desc );

  /**
    * Getter/Setter for the experiment's start time
    */
  boost::posix_time::ptime getStartTime();

  void setStartTime( const boost::posix_time::ptime& time );

  /**
    * Getter/Setter for the experiment's end time
    */
  boost::posix_time::ptime getEndTime();

  void setEndTime( const boost::posix_time::ptime& time );

  /**
    * Getter/Setter for the metric generators associated with this experiment
    */
  std::hash_set<MetricGenerator::ptr_t> getMetricGenerators();

  void setMetricGenerators( const std::hash_set<MetricGenerator::ptr_t>& generators );
    
  /**
    * @param metricGenerator the metric generator to add
    */
  void addMetricGenerator( MetricGenerator::ptr_t metricGenerator );
    
  /**
    * @param metricGenerators the metric generators to add
    */
  void addMetricGenerators( const std::hash_set<MetricGenerator::ptr_t>& generators );

  // ModelBase -----------------------------------------------------------------
  virtual void toJSON( std::wstring& jsonStrOUT );

  virtual void fromJSON( const std::wstring& jsonStr );

  virtual std::wstring toString();

private:
  boost::uuids::uuid uuid;

};

} // namespace
