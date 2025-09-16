package com.healthcaremngnt;

public class SpringVersionLogger {

	public static void main(String[] args) {
		printVersion("Spring Boot", org.springframework.boot.SpringBootVersion.getVersion());
		printVersion("Spring Security", org.springframework.security.core.context.SecurityContext.class);
		printVersion("Spring MVC", org.springframework.web.servlet.DispatcherServlet.class);
		printVersion("Spring Batch", org.springframework.batch.core.Job.class);
		printVersion("Spring Data JPA", org.springframework.data.jpa.repository.JpaRepository.class);
	}

	private static void printVersion(String name, Class<?> clazz) {
		Package pkg = clazz.getPackage();
		String version = (pkg != null) ? pkg.getImplementationVersion() : "Unknown";
		System.out.println(name + " Version: " + version);
	}

	private static void printVersion(String name, String version) {
		System.out.println(name + " Version: " + version);
	}
}