package jarvizz.project.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.JSONPObject;
import jarvizz.project.models.*;
import jarvizz.project.sevices.*;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@RestController
@AllArgsConstructor
public class MainController {
    UserService userService;
    FoodService foodService;
    PasswordEncoder passwordEncoder;
    MailService mailService;
    UserInfoService userInfoService;
    CardInfoService cardInfoService;
    OrderService orderService;

    @PostMapping("/register")
    public String register(@RequestHeader("username") String name,
                           @RequestHeader("password") String pass,
                           @RequestHeader("email") String mail) throws MessagingException, IOException {
        String good = "На вашу поштову адресу був відправлений лист з підтвердженням";
        String bad = "Користувач з такою поштовою адресю або логіном уже зареєстрований";
        User user = new User(name, passwordEncoder.encode(pass), mail);
        System.out.println(user);
        if (userService.findByEmail(mail) == null && userService.findByName(name) == null) {
            userService.save(user);
            mailService.send(mail, userService.findByEmail(mail));
            return good;
        }

        return bad;
    }

    @PostMapping("/fileUpload")
    public String fileUpload (@RequestParam("fileKey") MultipartFile file){

        String pass = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Work" + File.separator + "Projects"
                + File.separator + "FrontForProject" + File.separator + "src" + File.separator + "assets" + File.separator + "UsersImages";
        System.out.println( System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Work" + File.separator + "Projects" + File.separator + "images" + File.separator + "userIcons");
        File file1 = new File(pass);
        if (!file1.exists()) {
            file1.mkdir();
        }
        try {
            file.transferTo(new File(pass + File.separator +  file.getOriginalFilename()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @GetMapping("/basket")
    public List<Food> basket() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        System.out.println(name);
        User user = userService.findByName(name);
        return user.getBasket();
    }

    @PostMapping("/updateUserInfo")
    public void updateUserInfo(HttpServletRequest request) throws IOException {
        UserInfo userInfo = new ObjectMapper().readValue(request.getInputStream(), UserInfo.class);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        User user = userService.findByName(name);
        if (user.getUserInfo() != null) {
            UserInfo userInfo1 = userInfoService.get(user.getUserInfo().getId());
            userInfo1.setAddress(userInfo.getAddress());
            userInfo1.setName(userInfo.getName());
            userInfo1.setPhoneNumber(userInfo.getPhoneNumber());
            userInfo1.setSurname(userInfo.getSurname());
            userInfoService.save(userInfo1);
        } else {
            userInfo.setUser(user);
            userInfoService.save(userInfo);
            user.setUserInfo(userInfo);
            userService.save(user);
        }
    }


    @GetMapping("/getUserInfo")
    public UserInfo getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        User user = userService.findByName(name);
        return user.getUserInfo();
    }

    @PostMapping("/makeOrder/basket/{foods}")
    public void makeOrder(HttpServletRequest request, @PathVariable("foods") String foodInp) throws IOException {
        String[] split = foodInp.split(",");
        List<Food> foods = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<Food> list = new ArrayList<>();
        for (String s : split) {
            list.add(foodService.findByName(s));
        }
        Orders orders = objectMapper.readValue(request.getInputStream(), Orders.class);
        orders.setId(0);
        System.out.println(orders);
        for (Food food : list) {
            Food byName = foodService.findByName(food.getName());
            List<Orders> orders1 = byName.getOrders();
            orders1.add(orders);
            byName.setOrders(orders1);
            foods.add(byName);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !authentication.getName().equals("anonymousUser")) {
            String name = authentication.getName();
            User byName = userService.findByName(name);
            orders.setUser(byName);
            try {
                List<Orders> orders1 = byName.getOrders();
                orders1.add(orders);
                byName.setOrders(orders1);
            } catch (NullPointerException e) {
                List<Orders> orders1 = new ArrayList<>();
                orders1.add(orders);
                byName.setOrders(orders1);
            }
            orders.setFoods(foods);
            orderService.save(orders);
            userService.save(byName);
            if (byName.getUserInfo() == null) {
                UserInfo userInfo = new UserInfo(orders.getName(), orders.getSurname(), orders.getPhoneNumber(), orders.getAddress(), orders.getBonus());
                userInfo.setUser(byName);
                userInfoService.save(userInfo);
                byName.setUserInfo(userInfo);
                userService.save(byName);
            } else {
                UserInfo userInfo = byName.getUserInfo();
                double bonus = userInfo.getBonus() + orders.getBonus();
                userInfo.setBonus(bonus);
                userInfoService.save(userInfo);
                byName.setUserInfo(userInfo);
                userService.save(byName);
            }
            bonusMethod(byName, list, orders.getSum()); // calculate spent bonuses if they exist


        } else {
            orders.setFoods(foods);
            orderService.save(orders);
        }
    }

    @GetMapping("/history")
    public List<Orders> history() {
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        User byName = userService.findByName(authentication);
        return byName.getOrders();
    }


    private void bonusMethod(User user, List<Food> foods, double sum) {
        double foodsum = foods.stream().mapToDouble(Food::getPrice).sum();
        if (sum < foodsum && sum != 0) {
            double spentBonuses = foodsum - sum;
            UserInfo userInfo = user.getUserInfo();
            userInfo.setBonus(userInfo.getBonus() - spentBonuses);
            userInfoService.save(userInfo);
        }
        if (sum == 0) {
            UserInfo userInfo = user.getUserInfo();
            userInfo.setBonus(userInfo.getBonus() - foodsum);
            userInfoService.save(userInfo);
        }
    }
}
