package jarvizz.project.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jarvizz.project.models.Food;
import jarvizz.project.models.Type;
import jarvizz.project.models.User;
import jarvizz.project.sevices.FoodService;
import jarvizz.project.sevices.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
@AllArgsConstructor
public class RestaurantController {

    FoodService foodService;
    UserService userService;

    @GetMapping("/restaurant")
    public List<Food> restaurant() {
        return foodService.findAll();
    }

    @GetMapping("/restaurant/product-category/{category}")
    public List<Food> category(@PathVariable("category") String category) {
        Type type = Type.valueOf(category.toUpperCase());
        return foodService.findAllByType(type);
    }

    @PostMapping("/addFood")
    public void add(@RequestHeader("item") String item,
                    @RequestHeader("quantity") String quantity) {
        for (int i = 1; i <= Integer.parseInt(quantity); i++) {
            User user = getCurrentUser();
            Food food = foodService.findById(Integer.parseInt(item));
            List<User> users = food.getUser();
            users.add(user);
            food.setUser(users);
            foodService.save(food);
        }
    }
    @DeleteMapping("/deleteFood")
    public void delete(@RequestHeader("item") String item){
        User user = getCurrentUser();
        Food food = foodService.findById(Integer.parseInt(item));
        List<User> user1 = food.getUser();
        user1.removeIf((usr) -> usr.equals(user));
        food.setUser(user1);
        foodService.save(food);
    }

    private User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        return userService.findByName(name);
    }

}
