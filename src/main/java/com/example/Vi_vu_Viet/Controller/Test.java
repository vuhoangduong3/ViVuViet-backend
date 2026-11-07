package com.example.Vi_vu_Viet.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Test {
    @GetMapping("/Hello")
    public String Hello (){
        return " hello";
    }
}
