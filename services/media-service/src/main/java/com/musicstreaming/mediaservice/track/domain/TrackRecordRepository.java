package com.musicstreaming.mediaservice.track.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRecordRepository extends JpaRepository<TrackRecord, String> {
    List<TrackRecord> findAllByOrderByCreatedAtDesc();
}
