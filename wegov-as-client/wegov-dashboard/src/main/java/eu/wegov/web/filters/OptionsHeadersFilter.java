package eu.wegov.web.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


// NOT USED BUT MIGHT GET USEFUL AT SOME POINT
public class OptionsHeadersFilter implements Filter {

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;

//        response.setHeader("Access-Control-Allow-Origin", "http://" + req.getServerName());
//        response.setHeader("Access-Control-Allow-Methods", "GET,POST");
//        response.setHeader("Access-Control-Max-Age", "360");
//        response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
//        response.setHeader("Access-Control-Allow-Credentials", "true");

        // This should be added in response to both the preflight and the actual request
        response.addHeader("Access-Control-Allow-Origin", "*");

//        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            response.addHeader("Access-Control-Allow-Credentials", "true");
//        }

//        System.out.println("FILTER IS DOING ACCESS CONTROL");

        chain.doFilter(req, res);
    }

    public void init(FilterConfig filterConfig) {
    }

    public void destroy() {
    }
}
