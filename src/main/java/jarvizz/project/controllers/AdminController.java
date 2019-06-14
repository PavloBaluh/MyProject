package jarvizz.project.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jarvizz.project.models.Food;
import jarvizz.project.sevices.FoodService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.File;
import java.io.IOException;

@RestController
@AllArgsConstructor
public class AdminController {
    FoodService foodService;

    @PostMapping("/addDish")
    public String addDish(HttpServletRequest request) {
        Food food = null;
        try {
            food = new ObjectMapper().readValue(request.getInputStream(), Food.class);
        } catch (IOException e) {
            return null;
        }
        Food byName = foodService.findByName(food.getName());
        if (byName != null) {
            Food updated = new Food(byName.getId(),food.getName(),food.getType(),food.getWeight(),food.getPrice(),food.getDescription(),food.getPicture());
            foodService.save(updated);
        }
        else {foodService.save(food);}
        return "OK";
    }

    @PostMapping("/saveDishPicture")
    public String saveDishPicture(@RequestPart("fileKey") MultipartFile file) {
        String pass = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Work" + File.separator + "Projects"
                + File.separator + "FrontForProject" + File.separator + "src" + File.separator + "assets" + File.separator + "restourant" + File.separator + file.getOriginalFilename();
        try {
            System.out.println(pass);
            file.transferTo(new File(pass));
        } catch (IOException e) {
            return null;
        }
        return "OK";
    }
}
