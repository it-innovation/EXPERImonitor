package eu.wegov.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.wegov.web.security.WegovLoginService;



//@Controller

// DO NOT USE - TESTING ONLY
@RequestMapping("/home/123123123check/quick.html")
public class QuickLoginCheckController {
	@Autowired
	private transient WegovLoginService wegovLoginService;
	
    @RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public String checkCredentials(@RequestParam("username") String userName, @RequestParam("passwordhash") String passwordHash) {
		System.out.println("CHECKING USER CREDENTIALS for " + userName + " with passwordHash: " + passwordHash);
		
        if (wegovLoginService.ifCredentialsMatch(userName, passwordHash)) {
        	System.out.println("CREDENTIALS MATCH");
            return "success";
        } else {
        	System.out.println("NO MATCH");
            return "failed";
        }		
	}	
}
