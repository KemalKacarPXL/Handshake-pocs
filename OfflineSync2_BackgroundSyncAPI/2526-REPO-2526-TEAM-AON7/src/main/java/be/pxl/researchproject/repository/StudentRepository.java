package be.pxl.researchproject.repository;

import be.pxl.researchproject.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Student> findByJoinedEvent_Id(Long eventId);

    @Query("""
            select s.password as password
            from Student s
            where s.email = :email
            """)
    Optional<StudentAuthProjection> findAuthByEmail(@Param("email") String email);

    @Query("""
            select s.firstname as firstname,
                   s.lastname as lastname,
                   s.email as email,
                   s.education as education,
                   s.cvFileName as cvFileName,
                   s.cvFileSize as cvFileSize
            from Student s
            where s.email = :email
            """)
    Optional<StudentProfileProjection> findProfileByEmail(@Param("email") String email);

    @Modifying
    @Query("""
            update Student s
            set s.cvFileName = :cvFileName,
                s.cvContentType = :cvContentType,
                s.cvFileSize = :cvFileSize,
                s.cvFileContent = :cvFileContent
            where s.email = :email
            """)
    int updateCvByEmail(
            @Param("email") String email,
            @Param("cvFileName") String cvFileName,
            @Param("cvContentType") String cvContentType,
            @Param("cvFileSize") Long cvFileSize,
            @Param("cvFileContent") byte[] cvFileContent
    );

    @Modifying
    @Query("""
            update Student s
            set s.cvFileName = null,
                s.cvContentType = null,
                s.cvFileSize = null,
                s.cvFileContent = null
            where s.email = :email
            """)
    int deleteCvByEmail(@Param("email") String email);

    interface StudentAuthProjection {
        String getPassword();
    }

    interface StudentProfileProjection {
        String getFirstname();
        String getLastname();
        String getEmail();
        String getEducation();
        String getCvFileName();
        Long getCvFileSize();
    }

    @Query("""
        select s.cvFileContent as cvFileContent,
               s.cvContentType as cvContentType,
               s.cvFileName as cvFileName
        from Student s
        where s.email = :email
        """)
    Optional<StudentCvProjection> findCvByEmail(@Param("email") String email);

    interface StudentCvProjection {
        byte[] getCvFileContent();
        String getCvContentType();
        String getCvFileName();
    }
}
