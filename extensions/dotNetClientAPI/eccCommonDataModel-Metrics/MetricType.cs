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
//      Created Date :          09-Apr-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

using Newtonsoft.Json;
using Newtonsoft.Json.Converters;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics
{

/**
 * An enum specifying the metric type as one of the following:
 *   NOMINAL: no formal means of comparison; no ordering; e.g., a text-based state.
 *   ORDINAL: partial ordering implied; e.g., medals gold, silver, bronze
 *   INTERVAL: numeric; counting possible; e.g., the number of users.
 *   RATIO: ability to add, subtract, compare and normalise; e.g., CPU load.
 * @author Vegard Engen
 */
[JsonConverter(typeof(StringEnumConverter))]
public enum MetricType
{
    NOMINAL,
    ORDINAL,
    INTERVAL,
    RATIO
};

} // namespace