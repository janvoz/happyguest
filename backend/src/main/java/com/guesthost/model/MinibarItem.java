package com.guesthost.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "minibar_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MinibarItem {

    @Id
    private String id;

    private String propertyId;
    private String name;
    private BigDecimal price;
    private int stockCount;
    private String imageUrl;
}
