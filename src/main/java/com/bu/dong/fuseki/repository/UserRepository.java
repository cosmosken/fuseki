package com.bu.dong.fuseki.repository;

import com.bu.dong.fuseki.model.user.UserPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserPO, Long> {
}
