package com.foodbridge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootTest(properties = {
    "spring.profiles.active=local"
})
class FoodBridgeApplicationTests {

    @Test
    void contextLoads() {
    }

}
