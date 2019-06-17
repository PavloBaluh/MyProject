package jarvizz.project.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jarvizz.project.models.Food;
import jarvizz.project.models.Orders;
import jarvizz.project.sevices.FoodService;
import jarvizz.project.sevices.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
    public boolean deleteDish(HttpServletRequest request) {
        byte[] bytes = new byte[50];
        try {
            request.getInputStream().read(bytes);
            String name = new String(bytes);
            String substring = name.substring(name.indexOf("\"") + 1, name.lastIndexOf("\""));
            Food byName = foodService.findByName(substring);
            if (byName != null) {
                String pass = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Work" + File.separator + "Projects"
                        + File.separator + "FrontForProject" + File.separator + "src" + File.separator + "assets" + File.separator + "restourant" + File.separator;
                File file = new File(pass + byName.getPicture());
                file.delete();
                return foodService.deleteByName(substring);
            } else return false;
        } catch (IOException e) {
            return false;
        }
    }

    @GetMapping("/getOrders")
    public List<Orders> getOrders() {
        List<Orders> sortedOrders = new ArrayList<>();
        List<Orders> allOrders = orderService.getAllOrders();
        for (int i = allOrders.size() - 1; i >= 0; i--) {
             sortedOrders.add(allOrders.get(i));
        }
        return sortedOrders;
    }

    @GetMapping("/getSortedOrders/{sort}")
    public List<Orders> getSortedOrders(@PathVariable("sort") String sort) {
        List<Orders> allOrders = orderService.getAllOrders();
        if (sort.equals("date-old")){
            return allOrders;
        }
        else if(sort.equals("date-new") || sort.equals("All")){
            List<Orders> sortedOrders = new ArrayList<>();
            for (int i = allOrders.size() - 1; i >= 0; i--) {
                sortedOrders.add(allOrders.get(i));
            }
            return sortedOrders;
        }
        else if (sort.equals("name")){
            allOrders.sort((o1, o2) -> {
                if (o1.getName().equals(o2.getName())){
                    return o1.getSurname().compareTo(o2.getSurname());
                }
                return o1.getName().compareTo(o2.getName());
            });
            return allOrders;
        }
        else if (sort.equals("Done")){
            return allOrders.stream().filter(Orders::isDone).collect(Collectors.toList());
        }
        else if (sort.equals("Non-Done")){
            return allOrders.stream().filter(orders -> !orders.isDone() ).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }


    @GetMapping("/ApplyOrder/{id}")
    public String ApplyOrder (@PathVariable("id") Integer id) {
        Orders byId = orderService.findById(id);
        byId.setDone(true);
        orderService.save(byId);
        return "OK";
    }
}
