package jarvizz.project.dao;

import jarvizz.project.models.CardInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardInfoDao extends JpaRepository<CardInfo,Integer> {
}

