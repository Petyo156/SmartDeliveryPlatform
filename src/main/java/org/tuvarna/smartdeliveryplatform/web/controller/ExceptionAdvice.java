package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tuvarna.smartdeliveryplatform.exception.CartMerchantConflictException;
import org.tuvarna.smartdeliveryplatform.exception.CartOperationException;
import org.tuvarna.smartdeliveryplatform.exception.CourierAssignmentException;
import org.tuvarna.smartdeliveryplatform.exception.CourierOrderWorkflowException;
import org.tuvarna.smartdeliveryplatform.exception.InactiveMerchantAccessException;
import org.tuvarna.smartdeliveryplatform.exception.MerchantNotFoundException;
import org.tuvarna.smartdeliveryplatform.exception.MerchantOrderWorkflowException;
import org.tuvarna.smartdeliveryplatform.exception.OrderOperationException;
import org.tuvarna.smartdeliveryplatform.exception.OrderNotFoundException;
import org.tuvarna.smartdeliveryplatform.exception.PasswordsDoNotMatchException;
import org.tuvarna.smartdeliveryplatform.exception.UserWithEmailAlreadyExistsException;
import org.tuvarna.smartdeliveryplatform.web.util.RedirectUrlResolver;

@ControllerAdvice
public class ExceptionAdvice {
    private final RedirectUrlResolver redirectUrlResolver;

    public ExceptionAdvice(RedirectUrlResolver redirectUrlResolver) {
        this.redirectUrlResolver = redirectUrlResolver;
    }

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

        return "redirect:" + redirectUrlResolver.resolveRefererOrDefault(request, "/cart");
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

        return "redirect:" + redirectUrlResolver.resolveRefererOrDefault(request, "/cart");
    }

    @ExceptionHandler({
            MerchantOrderWorkflowException.class,
            CourierAssignmentException.class
    })
    public String merchantOrderWorkflowFailed(RedirectAttributes redirectAttributes,
                                              OrderOperationException e,
                                              HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        return "redirect:" + redirectUrlResolver.resolveRefererOrDefault(request, "/dashboard/merchant/orders");
    }

    @ExceptionHandler({
            CourierOrderWorkflowException.class
    })
    public String courierOrderWorkflowFailed(RedirectAttributes redirectAttributes,
                                             CourierOrderWorkflowException e,
                                             HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        return "redirect:" + redirectUrlResolver.resolveRefererOrDefault(request, "/courier/orders");
    }

    @ExceptionHandler({
            OrderOperationException.class
    })
    public String orderOperationFailed(RedirectAttributes redirectAttributes,
                                       OrderOperationException e,
                                       HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        return "redirect:" + redirectUrlResolver.resolveRefererOrDefault(request, "/cart");
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            InactiveMerchantAccessException.class,
            MerchantNotFoundException.class,
            OrderNotFoundException.class
    })
    public String notFound() {
        return "exception/not-found";
    }
}
