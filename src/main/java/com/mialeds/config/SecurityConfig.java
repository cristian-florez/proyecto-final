package com.mialeds.config;

// Importamos las clases necesarias para configurar la seguridad de la aplicación
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.mialeds.services.UserDetailsServiceImpl;

// Esta clase se utiliza para configurar la seguridad de la aplicación
@Configuration
@EnableWebSecurity // Esta anotación habilita la seguridad web en nuestra aplicación
@EnableMethodSecurity // Esta anotación permite la seguridad a nivel de método
public class SecurityConfig {

    // Método que configura la cadena de filtros de seguridad
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        
        // Configuramos la cadena de seguridad
        return httpSecurity
                .csrf(csrf -> csrf.disable()) // Deshabilitamos la protección CSRF para facilitar el desarrollo
                .formLogin(form -> form
                    .loginPage("/login") // Especificamos la página de inicio de sesión personalizada
                    .defaultSuccessUrl("/principal", true) // Redirigimos al usuario a la página principal después de iniciar sesión
                    .failureUrl("/login?error=true") // Redirigimos al usuario a la página de inicio de sesión con un mensaje de error si falla la autenticación
                    .permitAll()) // Permitimos que todos accedan a la página de inicio de sesión
                .logout(logout -> logout
                    .logoutUrl("/logout") // Especificamos la URL para cerrar sesión
                    .logoutSuccessUrl("/login")) // Redirigimos al usuario a la página de inicio de sesión después de cerrar sesión
                
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) // Configuramos la gestión de sesiones
                
                .authorizeHttpRequests(http -> {
                    // Definimos qué solicitudes HTTP son públicas y cuáles requieren autenticación
                    http.requestMatchers("/login").permitAll(); 
                    http.requestMatchers("/restaurarClave").permitAll();
                    http.requestMatchers("/css/**").permitAll();
                    http.requestMatchers("/js/**").permitAll(); 
                    http.requestMatchers("/images/**").permitAll();
                    
                    // Aquí definimos las rutas que requieren autenticación
                    http.requestMatchers("/inventario/editar").hasRole("ADMIN");
                    http.requestMatchers("/inventario/nuevo").hasRole("ADMIN");
                    http.requestMatchers("/inventario/eliminar").hasRole("ADMIN");
                    http.requestMatchers("/inventario/movimiento").hasRole("ADMIN");
                    http.requestMatchers("/venta/editar").hasRole("ADMIN");
                    http.requestMatchers("/venta/eliminar").hasRole("ADMIN");
                    http.requestMatchers("/proveedor/**").hasRole("ADMIN");



                    http.anyRequest().authenticated(); // Cualquier otra solicitud debe estar autenticada
                })
                .build(); // Construimos la cadena de seguridad
    }

    // Método que proporciona un AuthenticationManager, que se usa para manejar la autenticación
    @Bean 
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager(); // Obtenemos el AuthenticationManager de la configuración
    }

    // Método que configura el proveedor de autenticación
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsServiceImpl userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(); // Creamos un proveedor de autenticación
        provider.setPasswordEncoder(passwordEncoder()); // Establecemos el codificador de contraseñas
        provider.setUserDetailsService(userDetailsService); // Establecemos el servicio de detalles del usuario
        return provider; // Retornamos el proveedor de autenticación
    }

    // Método que crea un codificador de contraseñas utilizando BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Retornamos una nueva instancia de BCryptPasswordEncoder
    }

}
