package be.pxl.researchproject.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import be.pxl.researchproject.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import be.pxl.researchproject.model.Bedrijf;
import be.pxl.researchproject.model.Coordinator;
import be.pxl.researchproject.model.HandshakeEvent;
import be.pxl.researchproject.model.Scan;
import be.pxl.researchproject.model.Student;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CoordinatorRepository coordinatorRepository;
    private final StudentRepository studentRepository;
    private final HandshakeEventRepository handshakeEventRepository;
    private final BedrijfRepository bedrijfRepository;
    private final ScanRepository scanRepository;

    public DataSeeder(CoordinatorRepository coordinatorRepository, StudentRepository studentRepository, HandshakeEventRepository handshakeEventRepository, BedrijfRepository bedrijfRepository, ScanRepository scanRepository) {
        this.coordinatorRepository = coordinatorRepository;
        this.studentRepository = studentRepository;
        this.handshakeEventRepository = handshakeEventRepository;
        this.bedrijfRepository = bedrijfRepository;
        this.scanRepository = scanRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!coordinatorRepository.existsByEmail("coordinator@voorbeeld.be")) {
            Coordinator coordinator = new Coordinator("coordinator@voorbeeld.be", "password123");
            coordinatorRepository.save(coordinator);
            System.out.println("Default coordinator created: coordinator@voorbeeld.be / password123");
        }

        // Alle evenementen er even uithalen zodat we bij de demo makkelijk kunnen demonstreren dat de
        // student en bedrijf kan deelnemen aan een evenement.
        for (Student student : studentRepository.findAll()) {
            student.setJoinedEvent(null);
            studentRepository.save(student);
        }

        for (Bedrijf bedrijf : bedrijfRepository.findAll()) {
            bedrijf.setJoinedEvent(null);
            bedrijfRepository.save(bedrijf);
        }

        scanRepository.deleteAll();
        handshakeEventRepository.deleteAll();
        bedrijfRepository.deleteAll();

        HandshakeEvent handshakeEvent = new HandshakeEvent("HandshakeEvent", LocalDate.now(), LocalTime.now(), LocalTime.now(), "Vilderslaan 24, Genk");
        handshakeEventRepository.save(handshakeEvent);
        System.out.println("Default event created!");

        seedStudents(handshakeEvent);
        seedBedrijven(handshakeEvent);
        seedScans();
    }

    private void seedStudents(HandshakeEvent event) {
        String[][] studenten = {
            {"Arda", "Güler", "arda.guler@student.pxl.be", "password123", "Professionele Bachelor Toegepaste Informatica"},
            {"Emma", "Janssen", "emma.janssen@student.pxl.be", "password123", "Professionele Bachelor Toegepaste Informatica"},
            {"Noah", "Peeters", "noah.peeters@student.pxl.be", "password123", "Professionele Bachelor Elektronica-ICT"},
            {"Lotte", "Mertens", "lotte.mertens@student.pxl.be", "password123", "Professionele Bachelor Toegepaste Informatica"},
            {"Lucas", "Willems", "lucas.willems@student.pxl.be", "password123", "Professionele Bachelor Elektronica-ICT"},
            {"Sara", "Claes", "sara.claes@student.pxl.be", "password123", "Professionele Bachelor Toegepaste Informatica"},
            {"Daan", "Wouters", "daan.wouters@student.pxl.be", "password123", "Professionele Bachelor Systemen en Netwerken"},
            {"Julie", "Maes", "julie.maes@student.pxl.be", "password123", "Professionele Bachelor Toegepaste Informatica"},
        };

        for (int i = 0; i < studenten.length; i++) {
            String[] s = studenten[i];
            boolean joinEvent = i < 5;
            if (studentRepository.existsByEmail(s[2])) {
                Optional<Student> existing = studentRepository.findByEmail(s[2]);
                if (existing.isPresent() && joinEvent) {
                    Student student = existing.get();
                    student.setJoinedEvent(event);
                    studentRepository.save(student);
                }
            } else {
                Student student = new Student(s[0], s[1], s[2], s[3], s[4]);
                if (joinEvent) {
                    student.setJoinedEvent(event);
                }
                studentRepository.save(student);
            }
        }
        System.out.println("Seeded " + studenten.length + " students (first 5 joined to event).");
    }

    private void seedBedrijven(HandshakeEvent event) {
        seedBedrijf("Jan", "De Smedt", "jan.desmedt@cegeka.be", "password123", "Cegeka",
                "Innovatief IT-bedrijf gespecialiseerd in software development, cloud oplossingen en digitale transformatie.",
                "Software Development", "https://www.cegeka.com", "+32 89 56 91 00", "Hasselt", event);

        seedBedrijf("Sophie", "Van Damme", "sophie.vandamme@mobly.be", "password123", "Mobly",
                "Toonaangevend e-commerce platform voor meubelen en interieur met focus op gebruiksvriendelijke technologie.",
                "E-commerce", "https://www.mobly.be", "+32 2 588 72 00", "Antwerpen", event);

        seedBedrijf("Pieter", "Hendrickx", "pieter.hendrickx@cronos.be", "password123", "Cronos",
                "Technologiegroep die bedrijven helpt met IT-consultancy, software engineering en data-driven oplossingen.",
                "IT-consultancy", "https://www.cronos.be", "+32 3 451 25 25", "Kontich");

        seedBedrijf("Lisa", "Jacobs", "lisa.jacobs@ordina.be", "password123", "Ordina",
                "Digitale accelerator die organisaties ondersteunt bij het realiseren van slimme IT-oplossingen en innovatie.",
                "Digitale Transformatie", "https://www.ordina.be", "+32 2 266 15 00", "Mechelen");

        seedBedrijf("Tom", "Lenaerts", "tom.lenaerts@inetum-realdolmen.be", "password123", "Inetum-Realdolmen",
                "Europese IT-dienstverlener met expertise in applicatieontwikkeling, infrastructuur en business consulting.",
                "IT-dienstverlening", "https://www.inetum-realdolmen.world", "+32 2 801 55 55", "Brussel");

        System.out.println("Seeded 5 bedrijven.");
    }

    private void seedBedrijf(String firstname, String lastname, String email, String password,
                             String companyName, String description, String sector,
                             String website, String phoneNumber, String location, HandshakeEvent event) {
        if (!bedrijfRepository.existsByEmail(email)) {
            Bedrijf nieuwBedrijf = new Bedrijf(firstname, lastname, email, password, description, sector, website, phoneNumber, location);
            nieuwBedrijf.setCompanyName(companyName);
            nieuwBedrijf.setJoinedEvent(event);
            bedrijfRepository.save(nieuwBedrijf);
        }
    }

    private void seedBedrijf(String firstname, String lastname, String email, String password,
                             String companyName, String description, String sector,
                             String website, String phoneNumber, String location) {
        if (!bedrijfRepository.existsByEmail(email)) {
            Bedrijf nieuwBedrijf = new Bedrijf(firstname, lastname, email, password, description, sector, website, phoneNumber, location);
            nieuwBedrijf.setCompanyName(companyName);
            bedrijfRepository.save(nieuwBedrijf);
        }
    }

    private void seedScans() {
        Bedrijf cegeka = bedrijfRepository.findByEmail("jan.desmedt@cegeka.be").orElse(null);
        Bedrijf mobly  = bedrijfRepository.findByEmail("sophie.vandamme@mobly.be").orElse(null);

        if (cegeka != null) {
            createScan(cegeka, "arda.guler@student.pxl.be", "Arda", "Güler",
                    "Professionele Bachelor Toegepaste Informatica",
                    LocalDateTime.now().minusHours(3));
            createScan(cegeka, "emma.janssen@student.pxl.be", "Emma", "Janssen",
                    "Professionele Bachelor Toegepaste Informatica",
                    LocalDateTime.now().minusHours(2).minusMinutes(15));
            createScan(cegeka, "noah.peeters@student.pxl.be", "Noah", "Peeters",
                    "Professionele Bachelor Elektronica-ICT",
                    LocalDateTime.now().minusHours(1).minusMinutes(30));
        }

        if (mobly != null) {
            createScan(mobly, "lotte.mertens@student.pxl.be", "Lotte", "Mertens",
                    "Professionele Bachelor Toegepaste Informatica",
                    LocalDateTime.now().minusHours(2).minusMinutes(45));
            createScan(mobly, "lucas.willems@student.pxl.be", "Lucas", "Willems",
                    "Professionele Bachelor Elektronica-ICT",
                    LocalDateTime.now().minusMinutes(50));
        }

        System.out.println("Seeded demo scans for Cegeka (3) and Mobly (2).");
    }

    private void createScan(Bedrijf bedrijf, String studentEmail, String firstname, String lastname,
                            String education, LocalDateTime scannedAt) {
        boolean alreadyExists = scanRepository.findByBedrijfEmail(bedrijf.getEmail()).stream()
                .anyMatch(s -> s.getStudentEmail().equals(studentEmail));
        if (!alreadyExists) {
            Scan scan = new Scan();
            scan.setStudentFirstname(firstname);
            scan.setStudentLastname(lastname);
            scan.setStudentEmail(studentEmail);
            scan.setStudentEducation(education);
            scan.setScannedAt(scannedAt);
            scan.setBedrijf(bedrijf);
            scanRepository.save(scan);
        }
    }
}
