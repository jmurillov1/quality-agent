package org.ups.citasalud.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LocationTest {

    @Test
    void dado_datos_de_sede_cuando_crea_location_entonces_expone_atributos() {
        var id = UUID.randomUUID();
        var location = new Location(id, "Sede Norte", "Calle 100 #10-20");

        assertThat(location.id()).isEqualTo(id);
        assertThat(location.name()).isEqualTo("Sede Norte");
        assertThat(location.address()).isEqualTo("Calle 100 #10-20");
        assertThat(location.toString()).contains("Sede Norte");
    }

    @Test
    void dado_dos_locations_con_mismos_datos_cuando_compara_entonces_son_iguales() {
        var id = UUID.randomUUID();
        var a = new Location(id, "Sede Norte", "Calle 100");
        var b = new Location(id, "Sede Norte", "Calle 100");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
