package jarvizz.project.controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import jarvizz.project.models.*;
import jarvizz.project.sevices.*;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    @GetMapping("/getPermissions")
    public String getPermissions (){
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        User byName = userService.findByName(authentication);
        return byName.getRoles().toString();
    }

    @PostMapping("/fileUpload")
    public String fileUpload(@RequestPart("fileKey") MultipartFile file) throws IOException {
        System.out.println(file.getOriginalFilename());
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        User byName = userService.findByName(authentication);
        String encode = Base64.encode(file.getBytes());
        System.out.println( System.getProperty("user.dir"));
       String pass =  System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "static" + File.separator + "assets" + File.separator + "usersImages";
        File folder = new File(pass);
        if (!folder.exists()) {
            boolean mkdir = folder.mkdir();
            System.out.println(mkdir);
            System.out.println(folder.exists());
        }
        if (byName.getUserInfo().getPicture() != null){
            String picture = byName.getUserInfo().getPicture();
             File file1 = new File(pass + File.separator + picture);
             file1.delete();
        }
        try {
            file.transferTo(new File(pass + File.separator + file.getOriginalFilename()));
            byName.getUserInfo().setPicture(file.getOriginalFilename());
            userInfoService.save(byName.getUserInfo());
        } catch (IOException e) {
           return null;
        }
        return encode;
    }
    @GetMapping("/getUserImage/{imgName}")
    public String getUserImage (@PathVariable("imgName") String img) throws IOException {
        String pass = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "static" + File.separator + "assets" + File.separator + "usersImages" + File.separator;
        File file = new File(pass + img);
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("fileItem",
                file.getName(), "image/png", IOUtils.toByteArray(input));
        try {
            String encode = Base64.encode(multipartFile.getBytes());
            input.close();
            return encode;
        } catch (IOException e) {
            input.close();
            return null;
        }
    }

    @GetMapping("/basket")
    public List<Food> basket() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
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
        if (user.getUserInfo() == null){
            return null;
        }
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
                UserInfo userInfo = new UserInfo(orders.getName(), orders.getSurname(), orders.getPhoneNumber(), orders.getAddress());
                userInfo.setUser(byName);
                userInfoService.save(userInfo);
                byName.setUserInfo(userInfo);
                userService.save(byName);
            } else {
                UserInfo userInfo = byName.getUserInfo();
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
