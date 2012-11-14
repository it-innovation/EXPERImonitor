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
//	Created Date :			2012-11-14
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////

package eu.experimedia.itinnovation.ecc.web.data;


public class EccPropertiesAsJson {
    String nagios_fullurl;

    public EccPropertiesAsJson() {
    }

    public EccPropertiesAsJson(String nagios_fullurl) {
        this.nagios_fullurl = nagios_fullurl;
    }

    public String getNagios_fullurl() {
        return nagios_fullurl;
    }

    public void setNagios_fullurl(String nagios_fullurl) {
        this.nagios_fullurl = nagios_fullurl;
    }

 
}
