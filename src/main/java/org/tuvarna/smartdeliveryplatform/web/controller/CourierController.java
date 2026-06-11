package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/courier")
public class CourierController {

    @GetMapping("/orders")
    public ModelAndView getMyShopPage() {
        return new ModelAndView("courier/courier-orders");
    }
}
