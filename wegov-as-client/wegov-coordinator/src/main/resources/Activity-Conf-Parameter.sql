SELECT 
  "Activities"."ID" AS "Activity ID", 
  "ConfigurationSets"."ID" AS "Conf Set ID", 
  "Configurations"."ID" AS "Conf ID", 
  "Parameters"."ID" AS "Parameter ID", 
  --"Configurations_Parameters"."ParameterID", 
  "Parameters"."Name", 
  --"Parameters"."Description", 
  "Parameters"."Value"
FROM 
  "WeGovManagement"."Activities", 
  "WeGovManagement"."Activities_ConfigurationSets", 
  "WeGovManagement"."ConfigurationSets", 
  "WeGovManagement"."ConfigurationSets_Configurations", 
  "WeGovManagement"."Configurations", 
  "WeGovManagement"."Configurations_Parameters", 
  "WeGovManagement"."Parameters"
WHERE 
  "Activities"."ID" = "Activities_ConfigurationSets"."ActivityID" AND
  "ConfigurationSets"."ID" = "Activities_ConfigurationSets"."ConfigurationSetID" AND
  "ConfigurationSets_Configurations"."ConfigurationSetID" = "ConfigurationSets"."ID" AND
  "Configurations"."ID" = "ConfigurationSets_Configurations"."ConfigurationID" AND
  "Configurations_Parameters"."ConfigurationID" = "Configurations"."ID" AND
  "Parameters"."ID" = "Configurations_Parameters"."ParameterID"
 order by 
  "Activity ID", 
  "Conf Set ID", 
  "Conf ID", 
  "Parameter ID";
