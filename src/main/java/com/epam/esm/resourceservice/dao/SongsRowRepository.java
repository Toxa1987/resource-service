package com.epam.esm.resourceservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epam.esm.resourceservice.entity.SongRow;

@Repository
public interface SongsRowRepository extends JpaRepository<SongRow, Long> {
}
