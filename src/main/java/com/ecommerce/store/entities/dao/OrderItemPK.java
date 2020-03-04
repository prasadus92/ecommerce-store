package com.ecommerce.store.entities.dao;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import static java.util.Objects.nonNull;

@Embeddable
@Data
@NoArgsConstructor
@JsonIdentityInfo (generator = ObjectIdGenerators.PropertyGenerator.class, property = "order")
public class OrderItemPK implements Serializable {

    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "order_id")
    private Order order;

    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "product_id")
    private Product product;

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime * result + (!nonNull(order.getId())
                                   ? 0
                                   : order
                                       .getId()
                                       .hashCode());
        result = prime * result + (!nonNull(product.getId())
                                   ? 0
                                   : product
                                       .getId()
                                       .hashCode());

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OrderItemPK other = (OrderItemPK) obj;
        if (order == null) {
            if (other.order != null) {
                return false;
            }
        } else if (!order.equals(other.order)) {
            return false;
        }

        if (product == null) {
            if (other.product != null) {
                return false;
            }
        } else if (!product.equals(other.product)) {
            return false;
        }

        return true;
    }
}
