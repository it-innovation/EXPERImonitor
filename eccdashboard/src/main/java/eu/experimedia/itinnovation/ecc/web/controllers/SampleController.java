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
//	Created Date :			2012-08-17
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////
package eu.experimedia.itinnovation.ecc.web.controllers;

import eu.experimedia.itinnovation.ecc.web.data.SampleResponse;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/sample")
public class SampleController {

    Random rand = new Random();
    Calendar cal;
    int randomNum;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /*
     * Sleep between 2 and 7 seconds and then return random number between 100 and 1000
     */
    @RequestMapping(method = RequestMethod.GET, value = "/getdata/do.json")
    public @ResponseBody
    SampleResponse getData() throws Exception {
        
        try {
            randomNum = getRandomNum(2, 7);

            System.out.println("Sleeping for: " + randomNum + " seconds");

            Thread.sleep(randomNum * 1000);
            
            cal = Calendar.getInstance();
            return new SampleResponse(Integer.toString(getRandomNum(100, 1000)), sdf.format(cal.getTime()));
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }

    /*
     * Get random number in the range between min and max
     */
    private int getRandomNum(int min, int max) {
        return rand.nextInt(max - min + 1) + min;
    }
}
