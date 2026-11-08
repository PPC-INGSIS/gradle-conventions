// Un servicio que recibe pedidos con un token de Auth0 y lo valida
// (un "resource server" de OAuth2). Va aparte de ppc.kotlin-service porque
// no todos los servicios lo necesitan, y Spring Security, apenas está en el
// classpath, exige autenticación en todos los endpoints.
plugins {
    // Trae el BOM de Spring Boot, que fija las versiones de abajo
    id("ppc.kotlin-service")
}

dependencies {
    // Valida el JWT (firma, iss, aud, exp) con las claves públicas del emisor
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
    testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
    // jwt() en MockMvc: tokens falsos para que los tests no llamen a Auth0
    testImplementation("org.springframework.security:spring-security-test")
}