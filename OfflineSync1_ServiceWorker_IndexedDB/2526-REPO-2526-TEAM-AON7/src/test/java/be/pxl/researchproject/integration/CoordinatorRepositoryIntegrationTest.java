package be.pxl.researchproject.integration;

import be.pxl.researchproject.model.Coordinator;
import be.pxl.researchproject.repository.CoordinatorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CoordinatorRepositoryIntegrationTest {

    @Autowired
    private CoordinatorRepository coordinatorRepository;

    @Test
    void shouldFindCoordinatorByEmail() {
        coordinatorRepository.save(new Coordinator("coordinator@pxl.be", "password123"));

        Optional<Coordinator> found = coordinatorRepository.findByEmail("coordinator@pxl.be");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("coordinator@pxl.be");
    }

    @Test
    void shouldReturnFalseWhenCoordinatorEmailDoesNotExist() {
        coordinatorRepository.save(new Coordinator("coordinator@pxl.be", "password123"));

        boolean exists = coordinatorRepository.existsByEmail("unknown@pxl.be");

        assertThat(exists).isFalse();
    }
}
