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
//      Created By :            Vegard Engen
//      Created Date :          2012-08-22
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.metrics.mon.dao;

/**
 * A very simple class to encapsulate whether a function call evaluated to
 * true or false, with details of the exception that may have been thrown.
 * 
 * @author Vegard Engen
 */
public class ValidationReturnObject
{
    public boolean valid;
    public Exception exception;
    
    public ValidationReturnObject(){}
    
    public ValidationReturnObject(boolean valid)
    {
        this.valid = valid;
    }
    
    public ValidationReturnObject(boolean valid, Exception ex)
    {
        this (valid);
        this.exception = ex;
    }
}
