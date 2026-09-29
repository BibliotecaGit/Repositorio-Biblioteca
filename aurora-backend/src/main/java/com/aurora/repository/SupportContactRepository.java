package com.aurora.repository;

import com.aurora.entity.SupportContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportContactRepository extends JpaRepository<SupportContact, Long> {
}
