package org.ups.citasalud.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class RootControllerTest {

    private final RootController controller = new RootController();

    @Test
    void dado_raiz_cuando_consulta_entonces_retorna_200_con_enlaces() {
        var response = controller.root();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("app", "CitaSalud API");
        assertThat(response.getBody()).containsKey("health");
    }
}
