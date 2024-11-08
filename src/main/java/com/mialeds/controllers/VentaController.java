package com.mialeds.controllers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mialeds.models.Venta;
import com.mialeds.services.VentaService;

import org.springframework.ui.Model;



@Controller
@RequestMapping("/venta")
public class VentaController extends UsuarioDatosController {

    @Autowired
    private VentaService ventaService;

    //metodo para listar las ventas en la tabla de la vista
    @GetMapping("/listar")
    public String listar(Model model) {
        try {   //se obtienen las ventas, se suman las ventas y se formatea el total de ventas
                List<Venta> ventas = ventaService.listar();
                String totalVentasFormateado = ventaService.formatearTotalVentas(ventas);

            model.addAttribute("ventas", ventas);
            model.addAttribute("totalVentas", totalVentasFormateado);
        } catch (Exception e) {
            model.addAttribute("error", "error a listar las ventas: " + e.getMessage());
        }
        return "venta";
    }

    @GetMapping("/buscar")
    public String buscarVenta(
            @RequestParam(value = "producto_nombre", required = false) String nombre,
            @RequestParam(value = "fecha_busqueda", required = false) LocalDate fecha,
            Model model) {
        try {
            //utlizamos el metodo obtenerVentas para verificar si se ingreso un nombre o una fecha, ambos o ninguno
            List<Venta> ventas = ventaService.obtenerVentas(nombre, fecha);
            //obtenemos el total de ventas y lo formateamos
            String totalVentasFormateado = ventaService.formatearTotalVentas(ventas);
    
            model.addAttribute("ventas", ventas);
            model.addAttribute("totalVentas", totalVentasFormateado);
        } catch (Exception e) {
            model.addAttribute("error", "Error al buscar las ventas: " + e.getMessage());
        }
        return "venta";
    }
    

    //los parametros que no se pasan como id del usuario se obtiene mediante otra manera mediante el metodo de ventaService.guardar
    @PostMapping("/nuevo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearVenta(
            @RequestParam("id_producto_venta") int idProducto, 
            @RequestParam("cantidad_entrada_venta") int cantidad, 
            @RequestParam("fecha_venta") LocalDate fecha) {
    
        // Crear un mapa para almacenar la respuesta (success/failure y mensaje)
        Map<String, Object> response = new HashMap<>();
        try {
            //guardar la nueva venta
            Venta nuevaVenta = ventaService.guardar(idProducto, cantidad, fecha);
            
            // Comprobar si la venta se guardó correctamente
            if (nuevaVenta == null) {
                // Si hay error (cantidad insuficiente o producto no encontrado)
                response.put("success", false);
                response.put("message", "Error al realizar la venta: cantidad insuficiente o producto no encontrado.");
            } else {
                // Si la venta se realizó con éxito
                response.put("success", true);
                response.put("message", "Venta realizada con éxito.");
            }
        } catch (Exception e) {
            // Capturar cualquier excepción y devolver un mensaje de error
            response.put("success", false);
            response.put("message", "Error al realizar la venta: " + e.getMessage());
        }
        
        // Retornar la respuesta como JSON
        return ResponseEntity.ok(response);
    }
    

   //metodo para editar la venta
   @PutMapping("/editar")
   public String editarVenta(@RequestParam("id_venta_editar") int id, 
                               @RequestParam("id_producto_venta") int idProducto,
                               @RequestParam("cantidad_editar_venta") int cantidad,
                               @RequestParam("cantidad_editar_total_venta") int totalVenta,
                               @RequestParam("fecha_editar_venta") LocalDate fecha,
                               Model model) {
        try {
            ventaService.actualizar(id, idProducto, cantidad, totalVenta, fecha);
        } catch (Exception e) {
            model.addAttribute("error", "error al editar la venta: " + e.getMessage());
        }
        return "redirect:/venta/listar";
    }


    
    

    @DeleteMapping("/eliminar")
    public String eliminarVenta(@RequestParam("id_eliminar_venta") int id, Model model) {
        try {
            ventaService.eliminar(id);
        } catch (Exception e) {
            model.addAttribute("error", "error al eliminar la venta: " + e.getMessage());
        }
        return "redirect:/venta/listar";
    }
    
 }
    


