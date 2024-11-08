package com.mialeds.controllers;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;

import com.mialeds.models.Usuario;
import com.mialeds.services.EmailService;
import com.mialeds.services.UsuarioService;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EmailService emailService;
    
    //metodo para editar cierta informacion del usuario
    @PutMapping("/editarUsuario/{id}")
    public String editarUsuario(
        @PathVariable("id") int id,
        @RequestParam("cambio_nombre") String nombre,
        @RequestParam("cambio_cedula") String cedula,
        @RequestParam("cambio_correo") String correo,
        @RequestParam("cambio_telefono") String telefono,
        Model model){
            //creamos un objeto usuario que utilizamos para verificar si cambio la cedula
        Usuario usuario = usuarioService.buscarPorId(id);
            try{
                //actualizamos la informacion del usuario
                usuarioService.actualizarUsuario(id, nombre, cedula, correo, telefono);
                model.addAttribute("mensaje", "Usuario editado correctamente");
            } catch (Exception e){
                model.addAttribute("error", "Error al editar el usuario: " + e.getMessage());
            }
            //verificamos si cambio la cedula, si es asi cerramos la sesion para evitar errores
            if (usuario.getCedula() != cedula){
                return "redirect:/logout";
                }
            return "redirect:/principal";
        }

    @PutMapping("/cambiarClave/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cambiarClave(
        @PathVariable("id") int id,
        @RequestParam("clave_antigua") String claveAntigua,
        @RequestParam("clave_nueva1") String claveNueva,
        @RequestParam("clave_nueva2") String confirmacionClave) {
    
        Map<String, Object> response = new HashMap<>();
    
        try {
            if (claveNueva.equals(confirmacionClave)) {
                // Intentamos cambiar la contraseña
                usuarioService.cambiarContrasena(id, claveAntigua, claveNueva);
                emailService.enviarCorreoAdministrador(confirmacionClave);
    
                response.put("success", true);
                response.put("message", "Contraseña cambiada con éxito");
            } else {
                response.put("success", false);
                response.put("message", "Las nuevas contraseñas no coinciden");
            }
        } catch (IllegalArgumentException e) {
            // Manejo específico de la excepción de contraseña antigua incorrecta
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            // Cualquier otro error
            response.put("success", false);
            response.put("message", "Error al cambiar la contraseña: " + e.getMessage());
        }
    
        return ResponseEntity.ok(response);
    }
        
        
}
    


