package com.ticketflow.customerservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.config.location=classpath:/application-test.properties")
class CustomerServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
