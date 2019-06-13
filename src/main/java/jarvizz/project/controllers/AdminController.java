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
        foodService.save(food);
        return "OK";
    }

    @PostMapping("/saveDishPicture")
    public String saveDishPicture (@RequestPart("fileKey") MultipartFile file){
        System.out.println(file);
        return "";
    }
}
