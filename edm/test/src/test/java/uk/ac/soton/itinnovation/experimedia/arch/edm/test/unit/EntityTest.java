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
//      Created Date :          2012-12-13
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.edm.test.unit;

import java.util.Set;
import java.util.UUID;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.edm.test.general.PopulateDB;

/**
 *
 * @author Vegard Engen
 */
@RunWith(JUnit4.class)
public class EntityTest extends TestCase
{
    IMonitoringEDM edm = null;
    IEntityDAO entityDAO = null;
    static Logger log = Logger.getLogger(EntityTest.class);
    
    @BeforeClass
    public static void beforeClass()
    {
        log.info("Entity tests");
    }
    
    @Before
    public void beforeEachTest()
    {
        try {
            edm = EDMInterfaceFactory.getMonitoringEDM();
            edm.clearMetricsDatabase();
            entityDAO = edm.getEntityDAO();
        } catch (Exception ex) {
            log.error("Failed to set up EDM and getting EntityDAO: " + ex.toString());
        }
    }

    @Test
    public void testSaveEntity_valid_full()
    {
        log.info(" - saving entity");
        
        if ((edm == null) || (entityDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        Entity entity = new Entity();
        entity.setUUID(PopulateDB.entity1UUID);
        entity.setEntityID("/locations/epcc/3321");
        entity.setName("VM");
        entity.setDescription("A Virtual Machine");
        
        try {
            entityDAO.saveEntity(entity);
        } catch (Exception ex) {
            fail("Unable to save entity: " + ex.getMessage());
        }
    }
    
    @Test
    public void testSaveEntity_valid_minimal()
    {
        log.info(" - saving entity with minimal data");
        
        if ((edm == null) || (entityDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        Entity entity = new Entity();
        entity.setUUID(PopulateDB.entity1UUID);
        entity.setName("VM");
        
        try {
            entityDAO.saveEntity(entity);
        } catch (Exception ex) {
            fail("Unable to save entity: " + ex.getMessage());
        }
    }
    
    @Test
    public void testSaveEntity_withAttributes1()
    {
        log.info(" - saving entity with attributes");
        
        if ((edm == null) || (entityDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        Entity entity = new Entity();
        entity.setUUID(PopulateDB.entity1UUID);
        entity.setEntityID("/locations/epcc/3321");
        entity.setName("VM");
        entity.setDescription("A Virtual Machine");
        entity.addAttribute(new Attribute(PopulateDB.entity1attribute1UUID, entity.getUUID(), "CPU", "CPU performance"));
        entity.addAttribute(new Attribute(PopulateDB.entity1attribute2UUID, entity.getUUID(), "Network", "Network performance"));
        entity.addAttribute(new Attribute(PopulateDB.entity1attribute3UUID, entity.getUUID(), "Disk", "Disk performance"));
        
        try {
            entityDAO.saveEntity(entity);
        } catch (Exception ex) {
            fail("Unable to save entity: " + ex.getMessage());
        }
    }
    
    @Test
    public void testSaveEntity_withAttributes2()
    {
        log.info(" - saving entity with attributes; and then an additional attribute");
        
        if ((edm == null) || (entityDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        Entity entity = new Entity();
        entity.setUUID(PopulateDB.entity1UUID);
        entity.setEntityID("/locations/epcc/3321");
        entity.setName("VM");
        entity.setDescription("A Virtual Machine");
        entity.addAttribute(new Attribute(PopulateDB.entity1attribute1UUID, entity.getUUID(), "CPU", "CPU performance"));
        entity.addAttribute(new Attribute(PopulateDB.entity1attribute2UUID, entity.getUUID(), "Network", "Network performance"));
        entity.addAttribute(new Attribute(PopulateDB.entity1attribute3UUID, entity.getUUID(), "Disk", "Disk performance"));
        
        try {
            entityDAO.saveEntity(entity);
        } catch (Exception ex) {
            fail("Unable to save entity: " + ex.getMessage());
        }
        
        try {
            Attribute attrib = new Attribute(UUID.randomUUID(), entity.getUUID(), "Another attribute", "A random attribute for debugging");
            entityDAO.saveAttribute(attrib);
        } catch (Exception ex) {
            fail("Unable to save attribute: " + ex.getMessage());
        }
    }
    
    @Test
    public void testSaveEntity_duplicateUUID()
    {
        log.info(" - saving duplicate entity");
        
        if ((edm == null) || (entityDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        Entity entity1 = new Entity();
        entity1.setUUID(PopulateDB.entity1UUID);
        entity1.setName("VM");
        
        try {
            entityDAO.saveEntity(entity1);
        } catch (Exception ex) {
            fail("Unable to save entity: " + ex.getMessage());
        }
        
        Entity entity2 = new Entity();
        entity2.setUUID(PopulateDB.entity1UUID);
        entity2.setName("VM");
        
        try {
            entityDAO.saveEntity(entity2);
            fail("Saving duplicate entity did not throw an exception");
        } catch (Exception ex) { }
    }
    
    @Test
    public void testSaveEntity_noName()
    {
        log.info(" - saving entity with no name");
        
        if ((edm == null) || (entityDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        Entity entity = new Entity();
        entity.setUUID(PopulateDB.entity1UUID);
        
        try {
            entityDAO.saveEntity(entity);
            fail("Entity saved even though it did not have a name");
        } catch (Exception ex) { }
    }
    
    @Test
    public void testSaveEntity_invalidAttributes1()
    {
        log.info(" - saving entity with invalid attribute: Attribute UUID NULL");
        
        if ((edm == null) || (entityDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        Entity entity = new Entity();
        entity.setUUID(PopulateDB.entity1UUID);
        entity.setEntityID("/locations/epcc/3321");
        entity.setName("VM");
        entity.setDescription("A Virtual Machine");
        entity.addAttribute(new Attribute(null, entity.getUUID(), "CPU", "CPU performance"));
        entity.addAttribute(new Attribute(PopulateDB.entity1attribute2UUID, entity.getUUID(), "Network", "Network performance"));
        entity.addAttribute(new Attribute(PopulateDB.entity1attribute3UUID, entity.getUUID(), "Disk", "Disk performance"));
        
        try {
            entityDAO.saveEntity(entity);
            fail("Entity saved despite an attribute having a NULL UUID");
        } catch (Exception ex) { }
    }
    
    @Test
    public void testSaveEntity_invalidAttributes2()
    {
        log.info(" - saving entity with invalid attribute: Entity UUID NULL");
        
        if ((edm == null) || (entityDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        Entity entity = new Entity();
        entity.setUUID(PopulateDB.entity1UUID);
        entity.setEntityID("/locations/epcc/3321");
        entity.setName("VM");
        entity.setDescription("A Virtual Machine");
        entity.addAttribute(new Attribute(PopulateDB.entity1attribute1UUID, null, "CPU", "CPU performance"));
        entity.addAttribute(new Attribute(PopulateDB.entity1attribute2UUID, entity.getUUID(), "Network", "Network performance"));
        entity.addAttribute(new Attribute(PopulateDB.entity1attribute3UUID, entity.getUUID(), "Disk", "Disk performance"));
        
        try {
            entityDAO.saveEntity(entity);
            fail("Entity saved despite an attribute having a NULL Entity UUID");
        } catch (Exception ex) { }
    }
    
    @Test
    public void testSaveEntity_invalidAttributes3()
    {
        log.info(" - saving entity with invalid attribute: Attribute name NULL");
        
        if ((edm == null) || (entityDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        Entity entity = new Entity();
        entity.setUUID(PopulateDB.entity1UUID);
        entity.setEntityID("/locations/epcc/3321");
        entity.setName("VM");
        entity.setDescription("A Virtual Machine");
        entity.addAttribute(new Attribute(PopulateDB.entity1attribute1UUID, entity.getUUID(), null, "CPU performance"));
        entity.addAttribute(new Attribute(PopulateDB.entity1attribute2UUID, entity.getUUID(), "Network", "Network performance"));
        entity.addAttribute(new Attribute(PopulateDB.entity1attribute3UUID, entity.getUUID(), "Disk", "Disk performance"));
        
        try {
            entityDAO.saveEntity(entity);
            fail("Entity saved despite an attribute having a NULL name");
        } catch (Exception ex) { }
    }
    
    @Test
    public void testGetEntityByUUID()
    {
        log.info(" - getting entity from the DB by UUID");
        
        if ((edm == null) || (entityDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        // first saving the entity
        Entity entity = new Entity();
        entity.setUUID(PopulateDB.entity1UUID);
        entity.setEntityID("/locations/epcc/3321");
        entity.setName("VM");
        entity.setDescription("A Virtual Machine");
        entity.addAttribute(new Attribute(PopulateDB.entity1attribute1UUID, entity.getUUID(), "CPU", "CPU performance"));
        entity.addAttribute(new Attribute(PopulateDB.entity1attribute2UUID, entity.getUUID(), "Network", "Network performance"));
        entity.addAttribute(new Attribute(PopulateDB.entity1attribute3UUID, entity.getUUID(), "Disk", "Disk performance"));
        
        try {
            entityDAO.saveEntity(entity);
        } catch (Exception ex) {
            fail("Unable to save entity: " + ex.getMessage());
        }
        
        // getting the entity from the DB
        Entity entityFromDB = null;
        try {
            entityFromDB = entityDAO.getEntity(PopulateDB.entity1UUID, true);
        } catch (Exception ex) {
            fail("Unable to get entity: " + ex.getMessage());
        }
        
        // validate entity details
        log.info("Entity details:");
        assertNotNull("Entity UUID is NULL", entityFromDB.getUUID());
        assertNotNull("Entity ID is NULL", entityFromDB.getEntityID());
        assertNotNull("Entity name is NULL", entityFromDB.getName());
        assertNotNull("Entity description is NULL", entityFromDB.getDescription());
        assertNotNull("Entity attribute set is NULL", entityFromDB.getAttributes());
        assertTrue("Attribute set returned from DB should have contained 3 entries, but contained " + entityFromDB.getAttributes().size() + " entries", entityFromDB.getAttributes().size() == 3);

        // validate the attributes
        for (Attribute attrib : entityFromDB.getAttributes())
        {
            assertNotNull("Attribute is NULL", attrib);
            assertNotNull("Attribute UUID is NULL", attrib.getUUID());
            assertNotNull("Attribute's Entity UUID is NULL", attrib.getEntityUUID());
            assertNotNull("Attribute name is NULL", attrib.getName());
            assertNotNull("Attribute description is NULL", attrib.getDescription());
        }
    }
    
    @Test
    public void testGetEntities()
    {
        log.info(" - getting entities");
        
        if ((edm == null) || (entityDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        // saving first entity
        Entity entity1 = new Entity();
        entity1.setUUID(PopulateDB.entity1UUID);
        entity1.setEntityID("/locations/epcc/3321");
        entity1.setName("VM");
        entity1.setDescription("A Virtual Machine");
        
        try {
            entityDAO.saveEntity(entity1);
        } catch (Exception ex) {
            fail("Unable to save entity 1 in preparation for testing getting all entities: " + ex.getMessage());
        }
        
        // saving second entity
        Entity entity2 = new Entity();
        entity2.setUUID(PopulateDB.entity2UUID);
        entity2.setEntityID("/locations/epcc/3322");
        entity2.setName("VM");
        entity2.setDescription("A Virtual Machine");
        
        try {
            entityDAO.saveEntity(entity2);
        } catch (Exception ex) {
            fail("Unable to save entity 2 in preparation for testing getting all entities: " + ex.getMessage());
        }
        
        // getting all entities from the DB
        Set<Entity> entities = null;
        try {
            entities = entityDAO.getEntities(false);
        } catch (Exception ex) {
            fail("Unable to get entities due to an exception: " + ex.getMessage());
        }
        
        assertNotNull("Entity set returned from DB is null", entities);
        assertTrue("Entity set returned from DB should have contained 2 entities, but contained " + entities.size() + " entities", entities.size() == 2);
    }
}
