package jarvizz.project.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jarvizz.project.models.Food;
import jarvizz.project.models.Orders;
import jarvizz.project.sevices.FoodService;
import jarvizz.project.sevices.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
public class AdminController {
    FoodService foodService;
    OrderService orderService;

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
            Food updated = new Food(byName.getId(), food.getName(), food.getType(), food.getWeight(), food.getPrice(), food.getDescription(), food.getPicture());
            foodService.save(updated);
        } else {
            foodService.save(food);
        }
        return "OK";
    }

    @PostMapping("/saveDishPicture")
    public String saveDishPicture(@RequestPart("fileKey") MultipartFile file) {
        String pass = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Work" + File.separator + "Projects"
                + File.separator + "FrontForProject" + File.separator + "src" + File.separator + "assets" + File.separator + "restourant" + File.separator + file.getOriginalFilename();
        try {
            file.transferTo(new File(pass));
        } catch (IOException e) {
            return null;
        }
        return "OK";
    }

    @PostMapping("/deleteDish")
    public boolean deleteDish(HttpServletRequest request){
        byte[] bytes = new byte[50];
        try {
            request.getInputStream().read(bytes);
            String name = new String(bytes);
            String substring = name.substring(name.indexOf("\"") + 1, name.lastIndexOf("\""));
            Food byName = foodService.findByName(substring);
            if (byName != null) {
                String pass = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Work" + File.separator + "Projects"
                        + File.separator + "FrontForProject" + File.separator + "src" + File.separator + "assets" + File.separator + "restourant" + File.separator;
                File file = new File(pass +byName.getPicture());
                file.delete();
                return foodService.deleteByName(substring);
            }
            else return false;
        } catch (IOException e) {
            return false;
        }
    }
    @GetMapping("/getOrders")
    public List<Orders> getOrders (){
        List<Orders> allOrders = orderService.getAllOrders();
        return allOrders;
    }
}
