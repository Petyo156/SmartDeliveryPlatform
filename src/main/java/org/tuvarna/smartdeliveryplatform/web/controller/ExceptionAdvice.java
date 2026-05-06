package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tuvarna.smartdeliveryplatform.exception.PasswordsDoNotMatchException;
import org.tuvarna.smartdeliveryplatform.exception.UserWithEmailAlreadyExistsException;

@ControllerAdvice
public class ExceptionAdvice {
    @ExceptionHandler({
            PasswordsDoNotMatchException.class
    })
    public String passwordsDoNotMatch(RedirectAttributes redirectAttributes, PasswordsDoNotMatchException e) {
        String errorMessage = e.getMessage();
        redirectAttributes.addFlashAttribute("errorMessage", errorMessage);

        return "redirect:/register";
    }

    @ExceptionHandler({
            UserWithEmailAlreadyExistsException.class
    })
    public String userWithThisEmailAlreadyExists(RedirectAttributes redirectAttributes, UserWithEmailAlreadyExistsException e) {
        String errorMessage = e.getMessage();
        redirectAttributes.addFlashAttribute("errorMessage", errorMessage);

        return "redirect:/register";
    }
}
