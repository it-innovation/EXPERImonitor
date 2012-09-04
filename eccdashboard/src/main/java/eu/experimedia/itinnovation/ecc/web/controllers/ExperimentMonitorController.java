/////////////////////////////////////////////////////////////////////////
//
// ¬© University of Southampton IT Innovation Centre, 2012
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
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
//	Created By :			Maxim Bashevoy
//	Created Date :			2012-09-03
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////

package eu.experimedia.itinnovation.ecc.web.controllers;

import eu.experimedia.itinnovation.ecc.web.data.EMClientAsJson;
import eu.experimedia.itinnovation.ecc.web.services.ExperimentMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;

@Controller
@RequestMapping("/em")
public class ExperimentMonitorController {
    @Autowired
    @Qualifier("experimentMonitorService")
    ExperimentMonitorService emService;

    @RequestMapping(method = RequestMethod.GET, value = "/getclients/do.json")
    public @ResponseBody EMClientAsJson[] getConnectedClients() throws Throwable {
        try {
            
            EMClient[] clients = emService.getAllConnectedClients();
            int numClients = clients.length;
            
            EMClientAsJson tempClient = new EMClientAsJson();
            EMClientAsJson[] resultingListOfClients = new EMClientAsJson[numClients];
            
            int i = 0;
            for (EMClient e : clients) {
                tempClient.setUuid(e.getID().toString());
                tempClient.setName(e.getName());
                resultingListOfClients[i] = tempClient;
                
                tempClient = new EMClientAsJson();
                i++;
            }
            
            return resultingListOfClients;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }    
    
}
