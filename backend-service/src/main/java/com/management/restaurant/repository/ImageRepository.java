package com.management.restaurant.repository;

import com.management.restaurant.model.Image;
import com.management.restaurant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByUrl(String url);

    List<Image> findImageByUser(User user);

    @Modifying
    @Query("UPDATE Image i SET i.isAvatar = false WHERE i.user.id = :userId AND i.id <> :avatarId")
    void unsetOtherAvatars(@Param("userId") Long userId, @Param("avatarId") Long avatarId);

    @Modifying
    @Query("UPDATE Image i SET i.isAvatar = false WHERE i.user.id = :userId AND i.id <> :avatarId")
    void unsetAllOtherAvatars(@Param("userId") Long userId, @Param("avatarId") Long avatarId);
}
