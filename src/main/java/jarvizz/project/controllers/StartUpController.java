package jarvizz.project.controllers;

import jarvizz.project.models.User;
import jarvizz.project.sevices.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@AllArgsConstructor
public class StartUpController {
    UserService userService;
    @GetMapping("/")
    public String home(){
        return "forward:/index.html";
    }


    @GetMapping("/register/confirm/{name}")
    public String confirm(@PathVariable("name") String name) {
        User byName = userService.findByName(name);
        byName.setEnabled(true);
        userService.save(byName);
        return "confirmed";
    }

}
