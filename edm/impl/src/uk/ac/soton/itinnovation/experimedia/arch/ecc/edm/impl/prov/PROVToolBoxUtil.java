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
//      Created By :            Stefanie Wiegand
//      Created Date :          17-Oct-2013
//      Created for Project :   experimedia-arch-ecc-edm-impl
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov;

import org.openprovenance.prov.model.Document;




public class PROVToolBoxUtil
{
    public PROVToolBoxUtil()
    {}
    
    /**
     * Creates a PROV ToolBox Document using the ECC PROV Model classes
     * 
     * @param eccPROVReport - Stefanie, please change Object type
     * @return              - PROV Toolbox Document (empty if eccPROVReport is erroneous)
     */
    public Document createPTBDocument( Object eccPROVReport )
    {
      return null;
    }
    
    /**
     * Combines two PROV ToolBox documents contents together. Does not include 
     * duplicate PROV elements.
     * 
     * @param lhs - First PROV ToolBox document
     * @param rhs - Second PROV ToolBox document
     * @return    - Combined ToolBox document
     */
    public Document combinePTBDocuments( Document lhs, Document rhs )
    {
      return null;
    }
    
    /**
     * Creates a PDF document based on a PROV ToolBox document.
     * 
     * @param target      - PROV ToolBox document
     * @param fileTarget  - Path and filename of expected PDF document
     * @throws Exception  - Throws on target data, path or file writing problems
     */
    public void createPDFVisualisation( Document target, String fileTarget ) throws Exception
    {
      
    }
}
