package begin_a_gain.omokwang.user.repository;

import begin_a_gain.omokwang.user.dto.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySocialId(Long socialId);

    User findByRefreshToken(String refreshToken);

    boolean existsByNickname(String nickname);

    Optional<User> findBySocialIdAndPlatform(Long socialId, String platform);

    // 리프레시 토큰 업데이트 메서드
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.refreshToken = :refreshToken WHERE u.socialId = :socialId")
    void updateRefreshToken(@Param("socialId") Long socialId, @Param("refreshToken") String refreshToken);

    @Query("SELECT CASE WHEN u.nickname IS NULL THEN false ELSE true END FROM User u WHERE u.socialId = :socialId")
    boolean existsNicknameBySocialId(@Param("socialId") Long socialId);
}