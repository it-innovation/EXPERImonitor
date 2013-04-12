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
//      Created Date :          12-Apr-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Newtonsoft.Json.Converters;
using Newtonsoft.Json;

namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics
{

public class ECCDateTimeJSONConverter : DateTimeConverterBase
{
    public override object ReadJson( JsonReader reader, 
                                     Type objectType, 
                                     object existingValue, 
                                     JsonSerializer serializer )
    {

        return new DateTime();
    }

    public override void WriteJson( JsonWriter writer,
                                    object value, 
                                    JsonSerializer serializer )
    {
        if (value is DateTime)
        {
            DateTime dt = (DateTime)value;
            
            String jsonData = dt.ToString("yyyy-MM-dd HH:mm:ss.fff");
            writer.WriteValue(jsonData);
        }
    }
}

}
