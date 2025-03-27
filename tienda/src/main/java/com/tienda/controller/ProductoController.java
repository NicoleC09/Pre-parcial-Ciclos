package com.tienda.controller;

import com.tienda.model.Producto;
import com.tienda.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {
    @Autowired
    private ProductoRepository repository;

    @PostMapping
    public Producto agregarProducto(@RequestBody Producto producto) {
        return repository.save(producto);
    }

    @GetMapping
    public List<Producto> listarProductos() {
        return repository.findAll();
    }

    @PutMapping("/{id}")
    public Producto actualizarProducto(@PathVariable String id, @RequestBody Producto producto) {
        producto.setId(id);
        return repository.save(producto);
    }

    @DeleteMapping("/{id}")
    public void eliminarProducto(@PathVariable String id) {
        repository.deleteById(id);
    }
}
