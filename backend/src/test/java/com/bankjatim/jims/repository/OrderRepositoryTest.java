package com.bankjatim.jims.repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRepositoryTest {

    @Test
    void pagedOrderListQueryDoesNotFetchOrderItemsCollection() throws Exception {
        Method method = OrderRepository.class.getMethod("findAllWithDetails", Long.class, org.springframework.data.domain.Pageable.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(query.value()).doesNotContainIgnoringCase("FETCH o.items");
    }
}
