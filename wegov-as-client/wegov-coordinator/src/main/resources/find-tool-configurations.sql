SELECT 
  "ConfigurationSets"."ID" AS "Conf Set ID", 
  "ConfigurationSets"."Name" AS "Conf Set Name", 
  "Configurations"."ID" AS "Conf ID", 
  "Parameters"."ID" AS "Parameter ID", 
  "Parameters"."Name", 
  "Parameters"."Value"
FROM 
  "WeGovManagement"."ConfigurationSets", 
  "WeGovManagement"."ConfigurationSets_Configurations", 
  "WeGovManagement"."Configurations", 
  "WeGovManagement"."Configurations_Parameters", 
  "WeGovManagement"."Parameters"
WHERE 
  "ConfigurationSets_Configurations"."ConfigurationSetID" = "ConfigurationSets"."ID" AND
  "Configurations"."ID" = "ConfigurationSets_Configurations"."ConfigurationID" AND
  "Configurations_Parameters"."ConfigurationID" = "Configurations"."ID" AND
  "Parameters"."ID" = "Configurations_Parameters"."ParameterID"
	and "ConfigurationSets"."ID" not in (select "Activities_ConfigurationSets"."ConfigurationSetID" from "WeGovManagement"."Activities_ConfigurationSets")
 order by 
  "Conf Set ID", 
  "Conf ID", 
  "Parameter ID";
