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
//      Created Date :          2013-09-02
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.edmprov.owlim.common;

import java.io.Serializable;
import java.net.URL;

/**
 * A simple class to encapsulate some details about an ontology.
 *
 * @author Vegard Engen
 */
public class OntologyDetails implements Serializable
{
	private URL url;
	private String name;
	private String prefix;
	private String baseURI;

	/**
	 * Empty default constructor.
	 */
	public OntologyDetails()
	{
	}

	/**
	 * Constructor to set only the URL of the ontology.
	 *
	 * @param url The URL of the ontology.
	 */
	public OntologyDetails(URL url)
	{
		this.url = url;
	}

	/**
	 * Constructor to set all the ontology detail parameters.
	 *
	 * @param url The URL of the ontology.
	 * @param name The name of the ontology.
	 * @param baseURI The base URI of the ontology.
	 * @param prefix The prefix used for this ontology.
	 */
	public OntologyDetails(URL url, String name, String baseURI, String prefix)
	{
		this.url = url;
		this.name = name;
		this.baseURI = baseURI;
		this.prefix = prefix;
	}

	/**
	 * @return the url
	 */
	public URL getURL()
	{
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setURL(URL url)
	{
		this.url = url;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the prefix
	 */
	public String getPrefix()
	{
		return prefix;
	}

	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}

	/**
	 * @return the baseURI
	 */
	public String getBaseURI()
	{
		return baseURI;
	}

	/**
	 * @param baseURI the baseURI to set
	 */
	public void setBaseURI(String baseURI)
	{
		this.baseURI = baseURI;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}

		final OntologyDetails other = (OntologyDetails) obj;

		if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name))
		{
			return false;
		}
		if ((this.prefix == null) ? (other.prefix != null) : !this.prefix.equals(other.prefix))
		{
			return false;
		}
		if ((this.url == null) ? (other.url != null) : !this.url.equals(other.url))
		{
			return false;
		}
		if ((this.baseURI == null) ? (other.baseURI != null) : !this.baseURI.equals(other.baseURI))
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		int hash = 3;

		hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 53 * hash + (this.prefix != null ? this.prefix.hashCode() : 0);
		hash = 53 * hash + (this.url != null ? this.url.hashCode() : 0);
		hash = 53 * hash + (this.baseURI != null ? this.baseURI.hashCode() : 0);

		return hash;
	}
}
