package eu.wegov.helper;

import org.springframework.stereotype.Service;

import eu.wegov.coordinator.Coordinator;

@Service
public class CoordinatorHelper {
	
	private static Coordinator coordinator;
	
	public synchronized Coordinator getCoordinator() throws Exception {
		return getStaticCoordinator();
	}
	
    public Coordinator getStaticCoordinator() throws Exception  {

        if (coordinator == null) {
            coordinator = new Coordinator("coordinator.properties");
            coordinator.setupWegovDatabase();

        }

        return coordinator;
    }	

}
