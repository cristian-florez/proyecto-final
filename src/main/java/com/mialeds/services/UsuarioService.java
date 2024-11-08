package com.mialeds.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mialeds.models.Role;
import com.mialeds.models.RoleEnum;
import com.mialeds.models.Usuario;
import com.mialeds.repositories.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    protected Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    public List<Usuario> listar() {
        try {
            return usuarioRepository.findAll();
        } catch (Exception e) {
            logger.error("Error al listar los usuarios: " + e.getMessage());
            return null;
        }
    }

    public Usuario buscarPorId(int id) {
        try {
            return usuarioRepository.findById(id).orElse(null);
        } catch (Exception e) {
            logger.error("Error al buscar el usuario: " + e.getMessage());
            return null;
        }
    }

    // Este método crea un usuario en la base de datos, encriptando la contraseña
    public Usuario crearUsuario(Usuario usuario) {
        try {
            usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
            return usuarioRepository.save(usuario);
        } catch (Exception e) {
            logger.error("Error al crear el usuario: " + e.getMessage());
            return null;
        }
    }

    // Este método retorna un usuario por su cédula, utilizado a cambiar contraseña
    public Usuario buscarUsuarioAutenticacion(String cedula) {
        try {
            Usuario usuario = usuarioRepository.findByCedulaQuery(cedula);
            return usuario;
        } catch (Exception e) {
            logger.error("Error al buscar el usuario: " + e.getMessage());
            return null;
        }
    }    

    // Este método retorna el id de un usuario por su cédula, utilizado en ventas
    public Integer obtenerIdPorCedula(String cedula) {
        return usuarioRepository.findIdByCedula(cedula)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con cédula: " + cedula));
    }

        //metodo que obtiene el id del usuario de la sesion, utilizando el metodo de SpringSecurity
    public Integer obtenerIdUsuarioSesion(){
        //obtenemos la autenticacion del usuario
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //obtenemos la cedula del usuario y la buscamos en la base de datos, como cada usuario tiene una cedula unica, obtenemos el id del usuario
        String cedula = authentication.getName();
        int id = obtenerIdPorCedula(cedula);
        return id;
    }

    //metodo que obtiene la cedula del usuario de la sesion
    public String obtenerCedulaUsuarioSesion(){
        //obtenemos la autenticacion del usuario
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //obtenemos la cedula del usuario y la buscamos en la base de datos, como cada usuario tiene una cedula unica, obtenemos el id del usuario
        String cedula = authentication.getName();
        return cedula;
    }

    //metodo que obtiene la informacion del usuario de la sesion
    public Usuario obtenerInformacionUsuario(){
        try{
            //obtenemos el id del usuario de la sesion
            Integer id = obtenerIdUsuarioSesion();
            Usuario usuario = buscarPorId(id);
            return usuario;
        }catch(Exception e){
            logger.error("Error al obtener la información del usuario: " + e.getMessage());
            return null;
        }
    }

    //metodo que obtiene el rol del usuario de la sesion
    public RoleEnum obtenerRolDelUsuario() {
        try {
            // Obtener la información del usuario
            Usuario usuario = obtenerInformacionUsuario();
            if (usuario != null && usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
                // Acceder al primer rol
                Role rol = usuario.getRoles().iterator().next(); // Usamos iterator para obtener el primer elemento
                return rol.getRoleEnum(); // O lo que necesites devolver del rol
            }
        } catch (Exception e) {
            logger.error("Error al obtener el rol del usuario: " + e.getMessage());
        }
        return null; // Devuelve null si no se encuentra el rol
    }

    //metodo para devolver verdadero si el usuario tiene un rol especifico
    public boolean esAdmin() {
        return obtenerRolDelUsuario() == RoleEnum.ADMIN;
    }
    

    //metodo que actualiza cierta informacion del usuario
    public Usuario actualizarUsuario(int id, String nombre, String cedula, String correo, String telefono) {
        try {
            Usuario usuario = buscarPorId(id);
            usuario.setNombre(nombre);
            usuario.setCedula(cedula);
            usuario.setCorreoElectronico(correo);
            usuario.setTelefono(telefono);
            return usuarioRepository.save(usuario);
        } catch (Exception e) {
            logger.error("Error al actualizar el usuario: " + e.getMessage());
            return null;
        }
    }

    //metodo que cambia la contraseña del usuario iniciado sesion
    public void cambiarContrasena(int id, String claveVieja, String claveNueva) throws IllegalArgumentException {
        try {
            Usuario usuario = buscarPorId(id);
            // Verificamos si la contraseña antigua coincide con la contraseña del usuario
            if (passwordEncoder.matches(claveVieja, usuario.getContrasena())) {
                usuario.setContrasena(passwordEncoder.encode(claveNueva));
                usuarioRepository.save(usuario);
            } else {
                // Lanzamos una excepción específica si la contraseña antigua no coincide
                throw new IllegalArgumentException("La contraseña antigua no coincide");
            }
        } catch (Exception e) {
            logger.error("Error al cambiar la contraseña: " + e.getMessage());
            throw e; // Lanza la excepción para que el controlador la maneje
        }
    }


    //metodo que cambia la contraseña del usuario sin haber iniciado sesion
    public void cambiarContrasena(String cedula, String claveNueva) {
        try {
            Usuario usuario = buscarUsuarioAutenticacion(cedula);
            usuario.setContrasena(passwordEncoder.encode(claveNueva));
            usuarioRepository.save(usuario);
        } catch (Exception e) {
            logger.error("Error al cambiar la contraseña: " + e.getMessage());
        }
    }

    //metodo que genera una contraseña aleatoria
    public String generarContraseña() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String contraseña = "";
        for (int i = 0; i < 8; i++) {
            int indice = (int) (caracteres.length() * Math.random());
            contraseña += caracteres.charAt(indice);
        }
        return contraseña;
    }

}
