package eu.wegov.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

public class WegovAuthenticationProvider extends DaoAuthenticationProvider {
	
	@Autowired
	private transient WegovLoginService wegovLoginService;
	
    public WegovAuthenticationProvider() {
        super();
    }
    
    /**
     * Perform additional checks for the password comparison, once the user has
     * been retrieved.
     */
    @Override
    protected void additionalAuthenticationChecks(final UserDetails userDetails,
            final UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        
    	final String username = authentication.getName();
        final String password = String.valueOf(authentication.getCredentials());

        boolean pwdCheck = wegovLoginService.ifCredentialsMatch(username, password);

        if (!pwdCheck) {
            throw new BadCredentialsException("Failed to authenticate");
        }
    }    
}
