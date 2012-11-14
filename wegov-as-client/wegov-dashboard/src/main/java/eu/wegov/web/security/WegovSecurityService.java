package eu.wegov.web.security;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.restlet.security.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

public class WegovSecurityService implements UserDetailsService {

//    @Autowired
//    private transient CoordinatorService coordService;


    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException, DataAccessException {
//        Policymaker user = null;
//
//        if (StringUtils.isEmpty(username)) {
//            throw new BadCredentialsException("Wrong Username or Password");
//        }
//
//        try {
//            user = coordService.getPolicymakerByUsername(username);
//        } catch (final Exception e) {
//            throw new UsernameNotFoundException("Wrong Username or Password");
//        }
//
//        if (user == null) {
//            throw new UsernameNotFoundException("Wrong Username or Password");
//        }
//
//        List < Role > roles = null;
//        try {
//            roles = user.getRoles();
//        } catch (final SQLException e) {
//            throw new UsernameNotFoundException("Wrong Username or Password");
//        }

        return new User(username, "", true, true, true, true, getAuthorities());

    }

    /**
     * Builds the Granted authorities pre-appending the ROLE_ value to the role
     * name.
     * 
     * @param roles
     * @return Collection or granted authorities
     */
    private Collection < GrantedAuthority > getAuthorities() {
        final Set < GrantedAuthority > authList = new HashSet < GrantedAuthority >();
        authList.add(new GrantedAuthorityImpl("ROLE_" + "ADMIN"));
        return authList;
    }
//    private Collection < GrantedAuthority > getAuthorities(final List < Role > roles) {
//    	final Set < GrantedAuthority > authList = new HashSet < GrantedAuthority >();
//    	for (final Role role : roles) {
//    		authList.add(new GrantedAuthorityImpl("ROLE_" + role.getName().toUpperCase().trim()));
//    	}
//    	return authList;
//    }


}
