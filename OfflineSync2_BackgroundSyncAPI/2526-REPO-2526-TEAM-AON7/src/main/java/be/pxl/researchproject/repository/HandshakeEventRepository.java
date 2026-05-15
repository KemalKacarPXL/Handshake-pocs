package be.pxl.researchproject.repository;

import be.pxl.researchproject.model.HandshakeEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HandshakeEventRepository extends JpaRepository<HandshakeEvent, Long> {

    List<HandshakeEvent> findAllByOrderByDateDescStartTimeDesc();
}
