package com.tienda.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
@Document(collection = "productos")
public class Producto {
    @Id
    private String id;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @Positive(message = "El precio debe ser mayor que 0")
    private double precio;

    @PositiveOrZero(message = "La cantidad no puede ser negativa")
    private int cantidad;
}

