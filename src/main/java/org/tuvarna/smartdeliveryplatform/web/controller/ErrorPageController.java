package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.tuvarna.smartdeliveryplatform.exception.ExceptionMessages;

@Controller
public class ErrorPageController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null && Integer.parseInt(status.toString()) == 404) {
            return "exception/not-found";
        }

        model.addAttribute("errorTitle", ExceptionMessages.SERVER_ERROR_TITLE);
        model.addAttribute("errorMessage", "There was an error with your last request. Please try again later.");
        return "exception/server-error";
    }
}
