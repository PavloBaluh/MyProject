package jarvizz.project.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jarvizz.project.models.Food;
import jarvizz.project.models.Orders;
import jarvizz.project.models.User;
import jarvizz.project.models.UserInfo;
import jarvizz.project.sevices.FoodService;
import jarvizz.project.sevices.OrderService;
import jarvizz.project.sevices.UserInfoService;
import jarvizz.project.sevices.UserService;
import lombok.AllArgsConstructor;
import org.json.JSONException;
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
    UserInfoService userInfoService;
    UserService userService;

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
        String pass = System.getProperty("user.dir") + File.separator + "target" + File.separator + "classes" + File.separator
                + "static" + File.separator + "assets" + File.separator + "restourant" + File.separator + file.getOriginalFilename();
        try {
            System.out.println(file.getOriginalFilename());
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
                String pass = System.getProperty("user.dir") + File.separator + "target" + File.separator + "classes" + File.separator
                        + "static" + File.separator + "assets" + File.separator + "restourant" + File.separator;
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
        if (sort.equals("date-old")) {
            return allOrders;
        } else if (sort.equals("date-new") || sort.equals("All")) {
            List<Orders> sortedOrders = new ArrayList<>();
            for (int i = allOrders.size() - 1; i >= 0; i--) {
                sortedOrders.add(allOrders.get(i));
            }
            return sortedOrders;
        } else if (sort.equals("name")) {
            allOrders.sort((o1, o2) -> {
                if (o1.getName().equals(o2.getName())) {
                    return o1.getSurname().compareTo(o2.getSurname());
                }
                return o1.getName().compareTo(o2.getName());
            });
            return allOrders;
        } else if (sort.equals("Done")) {
            return allOrders.stream().filter(Orders::isDone).collect(Collectors.toList());
        } else if (sort.equals("Non-Done")) {
            return allOrders.stream().filter(orders -> !orders.isDone()).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }


    @GetMapping("/ApplyOrder/{id}")
    public String ApplyOrder(@PathVariable("id") Integer id) {
        Orders byId = orderService.findById(id);
        byId.setDone(true);
        orderService.save(byId);
        User user = byId.getUser();
        if (user != null) {
            if (user.getUserInfo() == null) {
                UserInfo userInfo = new UserInfo(byId.getName(), byId.getSurname(), byId.getPhoneNumber(), byId.getAddress(), byId.getBonus());
                userInfo.setUser(user);
                userInfoService.save(userInfo);
                user.setUserInfo(userInfo);
                userService.save(user);
            } else {
                UserInfo userInfo = user.getUserInfo();
                double bonus = userInfo.getBonus() + byId.getBonus();
                userInfo.setBonus(bonus);
                userInfoService.save(userInfo);
                user.setUserInfo(userInfo);
                userService.save(user);
            }
        }
        return "OK";
    }

    @GetMapping("/deleteOrder/{id}")
    public String deleteOrder(@PathVariable("id") Integer id) {
        Orders byId = orderService.findById(id);
        User user = byId.getUser();
        user.getOrders().removeIf((orders -> orders.getId() == id));
        List<Food> foods = byId.getFoods();
        foods.forEach((food -> {
            List<Orders> orders = food.getOrders();
            orders.removeIf(orders1 -> orders1.getId() == byId.getId());
            food.setOrders(orders);
            foodService.save(food);
        }));
        userService.save(user);
        byId.setUser(null);
        byId.setFoods(null);
        orderService.save(byId);
        orderService.delete(byId);
        return "OK";
    }

    @GetMapping("/getInfoForDiagram")
    public Map<Integer, List<Orders>> getInfoForDiagram() throws JSONException {
        Map<Integer, List<Orders>> quarters = new HashMap<>();
        quarters.put(1, new ArrayList<Orders>());
        quarters.put(2, new ArrayList<Orders>());
        quarters.put(3, new ArrayList<Orders>());
        quarters.put(4, new ArrayList<Orders>());
        List<Orders> allOrders = orderService.getAllOrders();
        allOrders.forEach((order) -> {
            int date = Integer.parseInt(order.getDate().substring(5, 7));
            if (date >= 1 && date <= 3) {
                List<Orders> orders = quarters.get(1);
                orders.add(order);
                quarters.put(1, orders);
            }
            if (date >= 4 && date <= 6) {
                List<Orders> orders = quarters.get(2);
                orders.add(order);
                quarters.put(2, orders);
            }
            if (date >= 7 && date <= 9) {
                List<Orders> orders = quarters.get(3);
                orders.add(order);
                quarters.put(3, orders);
            }
            if (date >= 10 && date <= 12) {
                List<Orders> orders = quarters.get(4);
                orders.add(order);
                quarters.put(4, orders);
            }
        });
        return quarters;
    }

    @GetMapping("/getYears")
    public TreeSet<Integer> GetYears() {
        TreeSet<Integer> years = new TreeSet<>();
        List<Orders> allOrders = orderService.getAllOrders();
        allOrders.forEach((order) -> {
            years.add(Integer.parseInt(order.getDate().substring(0, 4)));
        });
        return years;
    }

    @GetMapping("/GetOrdersByMounce")
    public HashMap<Integer, List<Orders>> GetOrdersByMounce() {
        List<Orders> allOrders = orderService.getAllOrders();
        HashMap<Integer, List<Orders>> map = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            map.put(i, new ArrayList<>());
        }
        for (Orders order : allOrders) {
            int date = Integer.parseInt(order.getDate().substring(5, 7));
            for (Map.Entry<Integer, List<Orders>> entry : map.entrySet()) {
                if (entry.getKey() == date) {
                    List<Orders> value = entry.getValue();
                    value.add(order);
                    entry.setValue(value);
                }
            }
        }
        return map;
    }

    @PostMapping("/getForRelationsDiagram")
    public HashMap<String, Integer> getForRelationsDiagram(@RequestHeader("values") String val) {
        HashMap<String, Integer> res = new HashMap<>();
        String[] split = val.split(",");
        if (split[0].equals("quarter")) {
            int neededQuartel = Integer.parseInt(split[1]);
            if (split[2].equals("all")) {
                List<Orders> allOrders = orderService.getAllOrders();
                res = CalcMethod(allOrders, neededQuartel);
            }
            else {
                ArrayList<Orders> OrdersByYear = new ArrayList<>();
                List<Orders> allOrders = orderService.getAllOrders();
                allOrders.forEach((order -> {
                    int i = Integer.parseInt(order.getDate().substring(0, 4));
                    if (i == Integer.parseInt(split[2])){
                        OrdersByYear.add(order);
                    }
                }));
                res  = CalcMethod(OrdersByYear, neededQuartel);
            }
        }
        return res ;
    }

    public HashMap<String, Integer> CalcMethod(List<Orders> allOrders, int neededQuartal){
        int currentQuartal = 0;
        List<Orders> ordersByQuartal = new ArrayList<>();
        for (Orders order : allOrders) {
            int date = Integer.parseInt(order.getDate().substring(5, 7));
            if (date >= 1 && date <= 3) {
                currentQuartal = 1;
            }
            if (date >= 4 && date <= 6) {
                currentQuartal = 2;
            }
            if (date >= 7 && date <= 9) {
                currentQuartal = 3;
            }
            if (date >= 10 && date <= 12) {
                currentQuartal = 4;
            }
            if (neededQuartal == currentQuartal) {
                ordersByQuartal.add(order);
            }
        }
        HashMap<String, Integer> res = new HashMap<>();
        for (Orders order : ordersByQuartal) {
            for (Food food : order.getFoods()) {
                if (!res.containsKey(food.getName())) {
                    res.put(food.getName(), 0);
                }
                if (res.containsKey(food.getName())) {
                    Set<Map.Entry<String, Integer>> entries = res.entrySet();
                    for (Map.Entry<String, Integer> entry : entries) {
                        if (entry.getKey().equals(food.getName())) {
                            entry.setValue(entry.getValue() + 1);
                        }
                    }
                }
            }
        }
        return res;
    }
}
