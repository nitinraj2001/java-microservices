package com.nitin.productservice.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Table(name="product")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
}
