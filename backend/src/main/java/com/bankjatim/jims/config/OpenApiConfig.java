package com.bankjatim.jims.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Bank Jatim - JIMS (Jatim Inventory Management System) API")
                        .description("RESTful Enterprise OpenAPI Documentation untuk Pengadaan, Logistik, Gudang, Personalisasi Kartu, dan Settlement Finansial Antarunit PT Bank Pembangunan Daerah Jawa Timur Tbk.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Divisi Teknologi Informasi & Divisi Umum Bank Jatim")
                                .email("logistik@bankjatim.co.id")
                                .url("https://www.bankjatim.co.id"))
                        .license(new License().name("Proprietary - PT Bank Pembangunan Daerah Jawa Timur Tbk.")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
