package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tuvarna.smartdeliveryplatform.exception.CartMerchantConflictException;
import org.tuvarna.smartdeliveryplatform.exception.CartOperationException;
import org.tuvarna.smartdeliveryplatform.exception.MerchantNotFoundException;
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

    @ExceptionHandler({
            CartOperationException.class
    })
    public String cartOperationFailed(RedirectAttributes redirectAttributes,
        CartOperationException e,
        HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        return "redirect:" + getRefererOrCart(request);
    }

    @ExceptionHandler({
            CartMerchantConflictException.class
    })
    public String cartMerchantConflict(RedirectAttributes redirectAttributes,
                                       CartMerchantConflictException e,
                                       HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("cartConflictMessage", e.getMessage());
        redirectAttributes.addFlashAttribute("pendingProductSlug", request.getParameter("productSlug"));
        redirectAttributes.addFlashAttribute("pendingQuantity", request.getParameter("quantity"));

        return "redirect:" + getRefererOrCart(request);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            MerchantNotFoundException.class
    })
    public String merchantNotFound() {
        return "exception/not-found";
    }

    private String getRefererOrCart(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return "/cart";
        }

        return referer;
    }
}
