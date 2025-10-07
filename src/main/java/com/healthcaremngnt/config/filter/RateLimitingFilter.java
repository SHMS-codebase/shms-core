package com.healthcaremngnt.config.filter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

	private static final Logger logger = LogManager.getLogger(RateLimitingFilter.class);
	private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

	private Bucket createNewBucket() {

		logger.info("Creating a new rate limiting bucket");
		Bandwidth limit = Bandwidth.classic(5, // capacity: max 5 tokens
				Refill.intervally(5, Duration.ofMinutes(1)) // refill 5 tokens every 1 minute
		);

		// Define the rate limiting configuration here
		return Bucket4j.builder().addLimit(limit).build();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		logger.info("RateLimitingFilter: Processing request for {}", request.getRequestURI());

		String clientIp = request.getRemoteAddr();
		Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createNewBucket());

		if (bucket.tryConsume(1)) {
			filterChain.doFilter(request, response);
		} else {
			logger.warn("Rate limit exceeded for IP: {}", clientIp);
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			response.setContentType("application/json");
			response.getWriter().write("{\"error\":\"Rate limit exceeded. Try again in 1 minute.\"}");

		}

	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return !request.getRequestURI().equals("/api/v1/auth/login");
	}

}