//esta clase es un controlador que se encarga de manejar las peticiones del usuario
package com.mialeds.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
//importamos domain sort para ordenar los datos de la base de datos en una lista
//importamos la anotacion service para indicar que es un servicio
import org.springframework.stereotype.Service;

//importamos la clase Producto del paquete models para poder usar sus metodos y atributos
import com.mialeds.models.Producto;

//importamos la clase ProductoRepository del paquete repositories, aunque con la anotacion autowired no es necesario pero es buena practica
import com.mialeds.repositories.ProductoRepository;

//importamos la clase Logger y LoggerFactory de slf4j para poder hacer logs de errores
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

@Service
public class ProductoService {

    /*se crea una instancia de ProductoRepository para poder usar sus metodos,
    se usa final para que no se pueda modificar*/
    @Autowired
    private ProductoRepository productoRepository;

    //se crea una instancia de Logger para poder hacer logs de errores
    protected Logger logger = LoggerFactory.getLogger(ProductoService.class);

    //este metodo retorna una lista de productos con cantidad existente menor a 4
    public List<Producto> productosEscasos() {
        try {
        return productoRepository.findByCantidadExistenteMenorQue(4);
        } catch (Exception e) {
            logger.error("Error al buscar productos escasos: " + e.getMessage());
            return null;
        }
    }


    //este metodo retorna una lista de productos ordenados por nombre
    public List<Producto> listar() {
        /*se llama el metodo findAllByOrderByNombreAsc de ProductoRepository 
        para obtener la lista de productos ordenada por nombre*/
        try {
            return productoRepository.findAllByOrderByNombreAsc();
        } catch (Exception e) {
            logger.error("Error al listar los productos: " + e.getMessage());
            return null;
        }
    }

    /*este metodo retorna una lista de productos que contengan el nombre indicado, 
    util para buscar productos y mostrarlos en la tabla de inventario*/
    public List<Producto> listarPorNombre(String nombre) {
        /*containing es una palabra clave de spring data jpa 
        para buscar por un atributo que contenga una cadena*/
        try {
            return productoRepository.findByNombreContaining(nombre);
        } catch (Exception e) {
            logger.error("Error al buscar productos por nombre: " + e.getMessage());
            return null;
        }
    }

    //este metodo retorna un producto por su id, util para utilizar en otros metodos de la clase
    protected Producto buscarPorId(int id) {
        //orElse(null) es para retornar null si no se encuentra el producto
        try {
            return productoRepository.findById(id).orElse(null);
        } catch (Exception e) {
            logger.error("Error al buscar producto por id: " + e.getMessage());
            return null;
        }
    }

    /*este metodo guarda un producto en la base de datos, se utiliza
     en otros metodos de la clase para guardar o actualizar productos, 
     se decide crear el metodo para encapsular la logica de guardar un producto*/
    protected Producto guardar(Producto producto) {
        try {
            return productoRepository.save(producto);
        } catch (Exception e) {
            logger.error("Error al guardar el producto: " + e.getMessage());
            return null;
        }
    }

    //este metodo actualiza un producto por su id y retorna el producto actualizado
    public Producto actualizar(int id, String nombre, String presentacion, int precioCompra, int precioVenta, int cantidad) {
       //usamos el metodo buscarPorId de esta misma para obtener el producto por su id
       try {
        Producto p = buscarPorId(id);
        //campos del producto que se van a actualizar
        p.setNombre(nombre);
        p.setPresentacion(presentacion);
        p.setPrecioCompra(precioCompra);
        p.setPrecioVenta(precioVenta);
        p.setCantidadExistente(cantidad);

        //usamos el metodo guardar de esta misma clase para guardar el producto actualizado
        guardar(p);
        //retornamos el producto actualizado
        return p;
         } catch (Exception e) {
              logger.error("Error al actualizar el producto");
        return null;
        }
    }

    //este metodo crea un producto y lo guarda en la base de datos
    public Producto crear(String nombre, String presentacion, int precioCompra, int precioVenta, int cantidad) {
        //creamos una instancia de Producto usando el constructor que no necesita id
        try {
        Producto p = new Producto(nombre, presentacion, cantidad, precioCompra, precioVenta);
        productoRepository.save(p);
        return p;
        } catch (Exception e) {
            logger.error("Error al crear el producto: " + e.getMessage());
            return null;
        }
    }

    //este metodo elimina un producto por su id
    public void eliminar(int id) {
        /*usamos el metodo buscarPorId de esta misma para obtener el producto por su id,
         lo hacemos asi y no directamente en el deleteById para evitar errores*/
        try {
        Producto p = buscarPorId(id);
        productoRepository.deleteById(p.getIdProducto());
        } catch (Exception e) {
            logger.error("Error al eliminar el producto: " + e.getMessage());
        }
    }

    //este metodo realiza un movimiento de entrada o salida de un producto por su id y cantidad
    public Producto movimiento(int id, int cantidad, String movimiento) {
        //usamos el metodo buscarPorId de esta misma para obtener el producto por su id
        try {
        Producto p = this.buscarPorId(id);
        //segun el movimiento se resta o se suma la cantidad al producto
        if (movimiento.equals("salida")) {
            p.setCantidadExistente(p.getCantidadExistente() - cantidad);
        } else if (movimiento.equals("entrada")) {
            p.setCantidadExistente(p.getCantidadExistente() + cantidad);
        } else {
            return null;
        }
        //usamos el metodo guardar de esta misma clase para guardar el producto actualizado
        productoRepository.save(p);
        return p;
        } catch (Exception e) {
            logger.error("Error al realizar el movimiento: " + e.getMessage());
            return null;
        }
    }

    /*este metodo retorna una lista de productos con nombre y presentacion
    utilizado en la apirest para los productos de la venta*/
    public List<Object[]> listarIdNombrePresentacion() {
        try {
            return productoRepository.findIdNombreAndPresentacion();
        } catch (Exception e) {
            logger.error("Error al buscar nombre y presentacion: " + e.getMessage());
            return null;
        }
    }
}
