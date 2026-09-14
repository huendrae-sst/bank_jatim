package com.bankjatim.jims;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootVersion;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformUpgradeContractTest {

    @Test
    void requiresJava21OrNewer() {
        assertThat(Runtime.version().feature()).isGreaterThanOrEqualTo(21);
    }

    @Test
    void usesSpringBoot411() {
        assertThat(SpringBootVersion.getVersion()).isEqualTo("4.1.1");
    }
}
