package com.bankjatim.jims.dto;

import com.bankjatim.jims.common.HibernateUtils;
import com.bankjatim.jims.domain.*;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OrderResponseTest {

    @Test
    void fromReturnsNullWhenOrderIsNull() {
        assertThat(OrderResponse.from(null)).isNull();
    }

    @Test
    void handlesUninitializedHibernateProxyWithoutThrowing() {
        Order order = new Order();
        order.setOrderNumber("ORD-12345");
        order.setStatus("SUBMITTED");
        order.setPriority("NORMAL");
        order.setRequiredDate(LocalDate.now().plusDays(3));
        order.setItems(Collections.emptyList());

        // Mock an uninitialized User HibernateProxy
        User userProxy = mock(User.class, withSettings().extraInterfaces(HibernateProxy.class));
        LazyInitializer userLi = mock(LazyInitializer.class);
        when(((HibernateProxy) userProxy).getHibernateLazyInitializer()).thenReturn(userLi);
        when(userLi.isUninitialized()).thenReturn(true);
        when(userLi.getIdentifier()).thenReturn(17L);
        when(userProxy.getName()).thenThrow(new org.hibernate.LazyInitializationException("Could not initialize proxy [com.bankjatim.jims.domain.User#17] - no session"));

        order.setCreatedByUser(userProxy);

        OrderResponse response = OrderResponse.from(order);

        assertThat(response).isNotNull();
        assertThat(response.orderNumber()).isEqualTo("ORD-12345");
        assertThat(response.createdByUser()).isNotNull();
        assertThat(response.createdByUser().id()).isEqualTo(17L);
        assertThat(response.createdByUser().name()).isNull();
    }

    @Test
    void hibernateUtilsExtractsIdFromProxyAndEntity() {
        User user = new User();
        user.setId(99L);
        assertThat(HibernateUtils.getId(user)).isEqualTo(99L);

        User proxy = mock(User.class, withSettings().extraInterfaces(HibernateProxy.class));
        LazyInitializer li = mock(LazyInitializer.class);
        when(((HibernateProxy) proxy).getHibernateLazyInitializer()).thenReturn(li);
        when(li.getIdentifier()).thenReturn(17L);
        assertThat(HibernateUtils.getId(proxy)).isEqualTo(17L);
    }
}
