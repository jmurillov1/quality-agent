package org.ups.citasalud.functional;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.ups.citasalud.adapter.out.persistence.*;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
abstract class FunctionalTestBase {

    static WireMockServer wireMock;

    @Autowired protected MockMvc mockMvc;
    @Autowired protected LocationJpaRepository locationRepo;
    @Autowired protected DoctorJpaRepository doctorRepo;
    @Autowired protected PatientJpaRepository patientRepo;
    @Autowired protected TimeSlotJpaRepository timeSlotRepo;
    @Autowired protected AppointmentJpaRepository appointmentRepo;
    @Autowired protected WhatsAppNotificationJpaRepository notificationRepo;

    @BeforeEach
    void clearDatabase() {
        notificationRepo.deleteAll();
        appointmentRepo.deleteAll();
        timeSlotRepo.deleteAll();
        doctorRepo.deleteAll();
        locationRepo.deleteAll();
        patientRepo.deleteAll();
    }

    protected DoctorJpaEntity persistDoctor(String name, String specialty) {
        var location = locationRepo.save(new LocationJpaEntity(UUID.randomUUID(), "Sede Test", "Calle 1"));
        return doctorRepo.save(new DoctorJpaEntity(UUID.randomUUID(), name, specialty, location));
    }

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
        wireMock.stubFor(post(urlPathMatching("/2010-04-01/Accounts/.*/Messages.json"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"sid\":\"SMtest\",\"status\":\"queued\"}")));
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMock != null) wireMock.stop();
    }

    @DynamicPropertySource
    static void twilioProperties(DynamicPropertyRegistry registry) {
        registry.add("twilio.account-sid", () -> "ACtest000000000000000000000000000000");
        registry.add("twilio.auth-token",  () -> "testtoken");
        registry.add("twilio.whatsapp-from", () -> "whatsapp:+14155238886");
    }
}
