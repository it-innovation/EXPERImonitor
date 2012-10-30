package eu.wegov.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.wegov.coordinator.web.PolicymakerLocation;
import eu.wegov.web.security.WegovLoginService;

@Controller
@RequestMapping("/home/locations")
public class LocationsController {
	@Autowired
	@Qualifier("wegovLoginService")
	WegovLoginService loginService;

	@RequestMapping(method = RequestMethod.GET, value = "/getLocationsForPM/do.json")
	public @ResponseBody PolicymakerLocation[] getLocationsForPM() {
		
		PolicymakerLocation[] locations = null;
		try {
			locations = loginService.getLocationsForPM();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return locations;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/addNewLocation/do.json")
	public @ResponseBody void addNewLocation(
			@RequestParam("locationName") String locationName,
			@RequestParam("locationAddress") String locationAddress,
			@RequestParam("lat") String lat,
			@RequestParam("lon") String lon) {

		System.out.println("Saving location: " + locationName + " | " + locationAddress + " | " + lat + " | " + lon);
		try {
			loginService.addNewLocation(locationName, locationAddress, lat, lon);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/removeLocation/do.json")
	public @ResponseBody void removeLocation(@RequestParam("locationId") String locationId) {
		
		System.out.println("Removing location with id: " + locationId);
		try {
			loginService.removeLocation(locationId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

