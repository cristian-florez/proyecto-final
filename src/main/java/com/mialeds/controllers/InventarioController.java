package com.mialeds.controllers;

// Importar las clases de Spring Framework para la inyección de dependencias y controladores
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mialeds.services.KardexService;
import java.time.LocalDate;
// Importar la clase ProductoService para realizar operaciones relacionadas con productos
import com.mialeds.services.ProductoService;
import com.mialeds.services.UsuarioService;

// Importar la clase Model de Spring Framework para pasar datos a la vista
import org.springframework.ui.Model;

@Controller // Anotación que indica que esta clase es un controlador
@RequestMapping("/inventario") // Establece la ruta base para todas las solicitudes de este controlador
public class InventarioController extends UsuarioDatosController {

    // Inyectar ProductoService para manejar operaciones de productos
    @Autowired
    private ProductoService productoService;

    // Inyectar KardexService para manejar operaciones del kardex
    @Autowired
    private KardexService kardexService;

    @Autowired
    private UsuarioService usuarioService;

    // Método para pasar la lista de productos escasos a la vista
    private void productosEscasos(Model model) {
        model.addAttribute("productosEsc", productoService.productosEscasos());
    }

    // Método para pasar los movimientos de entrada y salida del kardex a la vista
    private void kardexE(Model model) {
        model.addAttribute("kardexE", kardexService.listarPorMovimiento("entrada"));
        model.addAttribute("kardexS", kardexService.listarPorMovimiento("salida"));
    }

    // Método para listar todos los productos y mostrar la vista de inventario
    @GetMapping("/listar")
    public String listar(Model model) {
        try {
            productosEscasos(model); // Añadir productos escasos al modelo
            kardexE(model); // Añadir movimientos de kardex al modelo
            model.addAttribute("productos", productoService.listar()); // Listar todos los productos
        } catch (Exception e) {
            model.addAttribute("error", "Error al listar los productos: " + e.getMessage());
        }
        return "inventario"; // Retorna la vista inventario
    }

    // Método para buscar un producto por nombre
    @GetMapping("/buscar")
    public String buscarProducto(@RequestParam("producto") String nombre, Model model) {
        try {
            if (nombre == null || nombre.isEmpty()) {
                return "redirect:/inventario/listar"; // Redirigir si el nombre está vacío
            } else {
                model.addAttribute("productos", productoService.listarPorNombre(nombre)); // Buscar productos por nombre
                productosEscasos(model); // Añadir productos escasos al modelo
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al buscar el producto: " + e.getMessage());
        }
        return "inventario"; // Retorna la vista inventario
    }

    // Método para editar un producto existente
    @PutMapping("/editar")
    public String editarProducto(@RequestParam("id_editar") int id, 
                                @RequestParam("nombre_editar") String nombre,
                                @RequestParam("presentacion_editar") String presentacion,
                                @RequestParam("precio_compra_editar") int precioCompra,
                                @RequestParam("precio_venta_editar") int precioVenta,
                                @RequestParam("cantidad_editar") int cantidad,
                                Model model) {
        try {
            productoService.actualizar(id, nombre, presentacion, precioCompra, precioVenta, cantidad); // Actualizar el producto
        } catch (Exception e) {
            model.addAttribute("error", "Error al editar el producto: " + e.getMessage());
        }
        return "redirect:/inventario/listar"; // Redirigir a la lista de productos
    }

    // Método para crear un nuevo producto
    @PostMapping("/nuevo")
    public String crearProducto(
            @RequestParam("nombre_producto_nuevo") String nombre, 
            @RequestParam("presentacion_nuevo") String presentacion, 
            @RequestParam("precio_compra_nuevo") int precioCompra, 
            @RequestParam("precio_venta_nuevo") int precioVenta,
            @RequestParam("cantidad_nuevo") int cantidad,
            Model model) {
        try {
            productoService.crear(nombre, presentacion, precioCompra, precioVenta, cantidad); // Crear un nuevo producto
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear el producto: " + e.getMessage());
        }
        return "redirect:/inventario/listar"; // Redirigir a la lista de productos
    }

    // Método para eliminar un producto
    @DeleteMapping("/eliminar")
    public String eliminarProducto(@RequestParam("id_eliminar") int id, Model model) {
        try {
            productoService.eliminar(id); // Eliminar el producto por ID
        } catch (Exception e) {
            model.addAttribute("error", "Error al eliminar el producto: " + e.getMessage());
        }
        return "redirect:/inventario/listar"; // Redirigir a la lista de productos
    }

    // Método para registrar un movimiento en el kardex (entrada o salida)
    @PostMapping("/movimiento")
    public String movimientoProducto(
            @RequestParam("id_movimiento") int id, 
            @RequestParam("cantidad_movimiento") int cantidad, 
            @RequestParam("movimiento") String movimiento,
            @RequestParam("fecha_movimiento") String fecha,
            Model model) {
        try {
            productoService.movimiento(id, cantidad, movimiento); // Actualizar el producto según el movimiento
            int idUsuario = usuarioService.obtenerIdUsuarioSesion(); // Obtener el ID del usuario en sesión
            kardexService.crear(id, idUsuario, movimiento, LocalDate.parse(fecha), cantidad); // Registrar el movimiento en el kardex
        } catch (Exception e) {
            model.addAttribute("error", "Error al hacer el movimiento del producto: " + e.getMessage());
        }
        return "redirect:/inventario/listar"; // Redirigir a la lista de productos
    }
}
