package jarvizz.project.sevices;

import jarvizz.project.dao.FoodDao;
import jarvizz.project.models.Food;
import jarvizz.project.models.Type;
import jarvizz.project.models.User;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class FoodService {
    @Autowired
    FoodDao foodDao;
    public List<Food> findAll(){
       return foodDao.findAll();
    }
    public void save(Food food){
        if (food != null){
            foodDao.save(food);
        }
    }

    public List<Food> findAllByType(Type type){
        if (type != null) {
        return foodDao.findAllByType(type);
    }
        else return new ArrayList<Food>();
}
    public Food findById(int id){
        return foodDao.findById(id);
    }

    public boolean  deleteByName(String name){
        foodDao.deleteByName(name);
        return foodDao.findByName(name) == null;
    }
    public Food  findByName(String name){
        return foodDao.findByName(name);
    }
}
