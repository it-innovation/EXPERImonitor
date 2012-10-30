/////////////////////////////////////////////////////////////////////////
//
// ¬© University of Southampton IT Innovation Centre, 2011
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
//	Created Date :			2011-08-24
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator.utils;

/**
 *
 * @author Maxim Bashevoy
 */
public class Quad<T, U, V, X> {
    private T name;
    private U description;
    private V value;    
    private X roles;

    public Quad(T name, U description, V value, X roles) {
        this.name = name;
        this.description = description;
        this.value = value;
        this.roles = roles;
    }

    public U getDescription() {
        return description;
    }

    public void setDescription(U description) {
        this.description = description;
    }

    public T getName() {
        return name;
    }

    public void setName(T name) {
        this.name = name;
    }

    public X getRoles() {
        return roles;
    }

    public void setRoles(X roles) {
        this.roles = roles;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
        
}
