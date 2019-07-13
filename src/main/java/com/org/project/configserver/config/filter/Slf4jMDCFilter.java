package com.org.project.configserver.config.filter;

import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter to intercept all incoming requests to add necessary tracing
 * information
 * 
 * @author Sriram Tanikella <sriram18981@gmail.com>
 *
 */
public class Slf4jMDCFilter extends OncePerRequestFilter {

	private final String responseHeader;
	private final String mdcTokenKey;
	private final String requestHeader;

	public Slf4jMDCFilter(String responseHeader, String mdcTokenKey, String requestHeader) {
		super();
		this.responseHeader = responseHeader;
		this.mdcTokenKey = mdcTokenKey;
		this.requestHeader = requestHeader;
	}

	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
			final FilterChain chain) throws java.io.IOException, ServletException {
		
		try {
			final String token;
		
			if (!StringUtils.isEmpty(requestHeader) && !StringUtils.isEmpty(request.getHeader(requestHeader))) {
				token = request.getHeader(requestHeader);
			} else {
				token = UUID.randomUUID().toString().toUpperCase().replace("-", "");
			}
			
			MDC.put(mdcTokenKey, token);
			
			if (!StringUtils.isEmpty(responseHeader)) {
				response.addHeader(responseHeader, token);
			}
			
			chain.doFilter(request, response);
		} finally {
			MDC.remove(mdcTokenKey);
		}
	}
}
