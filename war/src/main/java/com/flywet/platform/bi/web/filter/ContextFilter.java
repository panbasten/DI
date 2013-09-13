package com.flywet.platform.bi.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.flywet.platform.bi.core.ContextHolder;

public class ContextFilter implements Filter {
	private static final String REPOSITORYNAME = "repository";
	private static final String REPOSITORYTYPE = "repositoryType";
	private static final String USERNAME = "username";
	private static final String USER = "user";

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			fc.doFilter(req, res);
			return;
		}

		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(REPOSITORYNAME)) {
				ContextHolder.setRepositoryName(cookie.getValue());
				continue;
			}

			if (cookie.getName().equals(REPOSITORYTYPE)) {
				ContextHolder.setRepositoryType(cookie.getValue());
				continue;
			}

			if (cookie.getName().equals(USERNAME)) {
				ContextHolder.setUserName(cookie.getValue());
				continue;
			}

			if (cookie.getName().equals(USER)) {
				ContextHolder.setUser(cookie.getValue());
				continue;
			}
		}
		fc.doFilter(req, res);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

}
