package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.tuvarna.smartdeliveryplatform.shared.constants.ErrorMessages;

@Controller
public class ErrorPageController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null && Integer.parseInt(status.toString()) == 404) {
            return "exception/not-found";
        }

        model.addAttribute("errorTitle", ErrorMessages.SERVER_ERROR_TITLE);
        model.addAttribute("errorMessage", ErrorMessages.GENERIC_REQUEST_ERROR);
        return "exception/server-error";
    }
}
