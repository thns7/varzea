package br.com.fiap.varzea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// O login usa os moradores do banco, então o usuário padrão em memória do Spring Security fica desligado.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class VarzeaApplication {

    public static void main(String[] args) {
        SpringApplication.run(VarzeaApplication.class, args);
    }
}
