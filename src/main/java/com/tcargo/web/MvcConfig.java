package com.tcargo.web;

import com.tcargo.web.filtros.JWTValidationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final JWTValidationFilter jwtValidationFilter;

	@Autowired
	public MvcConfig(JWTValidationFilter jwtValidationFilter) {
		this.jwtValidationFilter = jwtValidationFilter;
	}

	@Override
	public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
		WebMvcConfigurer.super.addResourceHandlers(registry);
		String resourcePath = Paths.get("uploads").toAbsolutePath().toUri().toString();
		registry.addResourceHandler("/uploads/**").addResourceLocations(resourcePath);
	}

	@Bean
	public FilterRegistrationBean<JWTValidationFilter> jwtValidationFilter() {
		FilterRegistrationBean<JWTValidationFilter> registrationBean = new FilterRegistrationBean<>();

		registrationBean.setFilter(jwtValidationFilter);
		registrationBean.setOrder(1);
		registrationBean.addUrlPatterns("/api/*");

		return registrationBean;
	}

	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver localeResolver = new SessionLocaleResolver();
		localeResolver.setDefaultLocale(new Locale("es", "ES"));
		return localeResolver;
	}

	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor localeInterceptor = new LocaleChangeInterceptor();
		localeInterceptor.setParamName("lang");
		return localeInterceptor;
	}

	@Bean
	public InterceptorNotificaciones notificacionesIntercerptor() {
		return new InterceptorNotificaciones();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
		registry.addInterceptor(notificacionesIntercerptor()).excludePathPatterns(Arrays.asList("/app-assets/**", "/assets/**", "/app/*", "/api/**", "/css/*", "/js/*", "/img/*", "/recuperar", "/cambiar", "/login", "/registroCompleto*", "/basesycondiciones"));
	}

}
