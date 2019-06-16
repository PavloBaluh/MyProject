package jarvizz.project.dao;

import jarvizz.project.models.Food;
import jarvizz.project.models.Type;
import jarvizz.project.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodDao extends JpaRepository<Food, Integer> {
    List<Food> findAllByType(Type type);
    Food findById(int id);
    List<Food> findAll();
    void deleteByName(String name);
    Food findByName(String name);
}
