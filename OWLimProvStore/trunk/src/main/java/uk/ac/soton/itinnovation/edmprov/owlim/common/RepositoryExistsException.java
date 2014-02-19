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
//      Created By :            Vegard Engen
//      Created Date :          2013-11-06
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.edmprov.owlim.common;

/**
 * An exception class to indicate that a repository already exists, which is used
 * in the createNewRepository method
 * @see ASesameConnector
 * @author Vegard Engen
 */
public class RepositoryExistsException extends Exception
{
    public RepositoryExistsException() {
        super();
    }
    
    public RepositoryExistsException(String msg) {
        super(msg);
    }
    
    public RepositoryExistsException(String msg, Exception ex) {
        super(msg, ex);
    }
}
