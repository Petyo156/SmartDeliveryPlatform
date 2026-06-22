package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tuvarna.smartdeliveryplatform.exception.AddressOperationException;
import org.tuvarna.smartdeliveryplatform.exception.AdminOperationException;
import org.tuvarna.smartdeliveryplatform.exception.CartMerchantConflictException;
import org.tuvarna.smartdeliveryplatform.exception.CartOperationException;
import org.tuvarna.smartdeliveryplatform.exception.CategoryOperationException;
import org.tuvarna.smartdeliveryplatform.exception.CourierAssignmentException;
import org.tuvarna.smartdeliveryplatform.exception.CourierOperationException;
import org.tuvarna.smartdeliveryplatform.exception.CourierOrderWorkflowException;
import org.tuvarna.smartdeliveryplatform.exception.InactiveMerchantAccessException;
import org.tuvarna.smartdeliveryplatform.exception.MerchantNotFoundException;
import org.tuvarna.smartdeliveryplatform.exception.MerchantOperationException;
import org.tuvarna.smartdeliveryplatform.exception.MerchantOrderWorkflowException;
import org.tuvarna.smartdeliveryplatform.exception.OrderOperationException;
import org.tuvarna.smartdeliveryplatform.exception.OrderNotFoundException;
import org.tuvarna.smartdeliveryplatform.exception.PasswordsDoNotMatchException;
import org.tuvarna.smartdeliveryplatform.exception.ProductOperationException;
import org.tuvarna.smartdeliveryplatform.exception.ExceptionMessages;
import org.tuvarna.smartdeliveryplatform.exception.SystemOperationException;
import org.tuvarna.smartdeliveryplatform.exception.UserOperationException;
import org.tuvarna.smartdeliveryplatform.exception.UserWithEmailAlreadyExistsException;
import org.tuvarna.smartdeliveryplatform.exception.UserWithPhoneNumberAlreadyExistsException;
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
            UserWithPhoneNumberAlreadyExistsException.class
    })
    public String userWithThisPhoneNumberAlreadyExists(RedirectAttributes redirectAttributes,
                                                       UserWithPhoneNumberAlreadyExistsException e,
                                                       HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        return "redirect:" + redirectUrlResolver.resolveRefererOrDefault(request, request.getRequestURI());
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

    @ExceptionHandler({
            ProductOperationException.class,
            CategoryOperationException.class
    })
    public String productCatalogOperationFailed(RedirectAttributes redirectAttributes,
                                                RuntimeException e,
                                                HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        return "redirect:" + redirectUrlResolver.resolveRefererOrDefault(request, "/products");
    }

    @ExceptionHandler({
            AddressOperationException.class
    })
    public String addressOperationFailed(RedirectAttributes redirectAttributes,
                                         AddressOperationException e,
                                         HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        return "redirect:" + redirectUrlResolver.resolveRefererOrDefault(request, "/addresses");
    }

    @ExceptionHandler({
            AdminOperationException.class
    })
    public String adminOperationFailed(RedirectAttributes redirectAttributes,
                                       AdminOperationException e,
                                       HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        return "redirect:" + redirectUrlResolver.resolveRefererOrDefault(request, "/admin/users");
    }

    @ExceptionHandler({
            CourierOperationException.class
    })
    public String courierOperationFailed(RedirectAttributes redirectAttributes,
                                         CourierOperationException e,
                                         HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        return "redirect:" + redirectUrlResolver.resolveRefererOrDefault(request, "/admin/couriers");
    }

    @ExceptionHandler({
            MerchantOperationException.class
    })
    public String merchantOperationFailed(RedirectAttributes redirectAttributes,
                                          MerchantOperationException e,
                                          HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        return "redirect:" + redirectUrlResolver.resolveRefererOrDefault(request, "/admin/merchants");
    }

    @ExceptionHandler({
            UserOperationException.class
    })
    public String userOperationFailed(RedirectAttributes redirectAttributes,
                                      UserOperationException e,
                                      HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        return "redirect:" + redirectUrlResolver.resolveRefererOrDefault(request, "/register");
    }

    @ExceptionHandler({
            SystemOperationException.class
    })
    public String systemOperationFailed(SystemOperationException e, Model model) {
        model.addAttribute("errorTitle", ExceptionMessages.SERVER_ERROR_TITLE);
        model.addAttribute("errorMessage", e.getMessage());
        return "exception/server-error";
    }

    @ExceptionHandler({
            InactiveMerchantAccessException.class,
            MerchantNotFoundException.class,
            OrderNotFoundException.class
    })
    public String notFound() {
        return "exception/not-found";
    }
}
