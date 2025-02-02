/**
 * Copyright (c) 2014 Center for eHalsa i samverkan (CeHis).
 * 								<http://cehis.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.cooperation;

import org.h2.server.web.WebServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

// cooperation-config-override.properties file is for tomcat production deployment
// should contain 
// spring.datasource.url=jdbc:mysql://localhost/cooperation
// spring.datasource.username=root
// spring.datasource.password=
// these properties are left out of the application.yml in the prod profile section
// 
// tomcat bin directory should contain a setenv.sh containing
// export CATALINA_OPTS="$CATALINA_OPTS -Dspring.profiles.active=prod"
// export CATALINA_OPTS="$CATALINA_OPTS -Dapp.conf.dir=/Users/jan/conf"
// the cooperation-config-override.properties should be placed in the directory pointed out in the last row above

@SpringBootApplication
@PropertySources({
    @PropertySource(value = "file:${app.conf.dir}/cooperation-config-override.properties", ignoreResourceNotFound = true)
})
public class Application extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }    
		
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	@Profile("dev")
	public ServletRegistrationBean h2servletRegistration() {
		ServletRegistrationBean registration = new ServletRegistrationBean(new WebServlet());
		registration.addUrlMappings("/console/*");
		return registration;
	}
}
