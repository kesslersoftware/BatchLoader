package com.boycottpro.batchloader;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.ai.openai.api-key=test-key",
    "boycottpro.exportDir=/tmp/test-exports",
    "boycottpro.bucket=test-bucket",
    "boycottpro.awsProfile=test-profile",
    "boycottpro.env=test"
})
class BatchLoaderApplicationTests {

	@Test
	void contextLoads() {
	}

}