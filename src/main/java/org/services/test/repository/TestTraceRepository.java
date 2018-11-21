package org.services.test.repository;

import org.services.test.entity.TestTrace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestTraceRepository extends JpaRepository<TestTrace, String> {
}
