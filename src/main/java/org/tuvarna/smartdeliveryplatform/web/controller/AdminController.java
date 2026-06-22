package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tuvarna.smartdeliveryplatform.courier.service.CourierService;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserStatus;
import org.tuvarna.smartdeliveryplatform.user.service.AdminService;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.AdminEmailRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.AdminSearchRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.CourierResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.UserResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.UserStatusRequest;
import org.tuvarna.smartdeliveryplatform.web.util.FlashValidationAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final MerchantService merchantService;
    private final CourierService courierService;
    private final FlashValidationAttributes flashValidationAttributes;

    public AdminController(AdminService adminService,
                           MerchantService merchantService,
                           CourierService courierService,
                           FlashValidationAttributes flashValidationAttributes) {
        this.adminService = adminService;
        this.merchantService = merchantService;
        this.courierService = courierService;
        this.flashValidationAttributes = flashValidationAttributes;
    }

    @GetMapping("/users")
    public String getUsers(
            @Valid @ModelAttribute("searchRequest") AdminSearchRequest searchRequest,
            BindingResult bindingResult,
            Model model) {
        UserResponse userResponse = UserResponse.builder().build();
        UserStatusRequest userStatusRequest = UserStatusRequest.builder().build();

        if (!bindingResult.hasErrors()) {
            userResponse = adminService.findUserResponseByEmail(searchRequest.getSearchEmail());
            if (userResponse.getEmail() != null) {
                userStatusRequest = UserStatusRequest.builder()
                        .email(userResponse.getEmail())
                        .status(userResponse.getStatus())
                        .build();
            }
        }

        model.addAttribute("userResponse", userResponse);
        model.addAttribute("userStatusRequest", userStatusRequest);
        model.addAttribute("userStatuses", UserStatus.values());
        model.addAttribute("searchHasErrors", bindingResult.hasErrors());
        return "admin/users";
    }

    @PostMapping("/users/status")
    public String updateUserStatus(
            @Valid @ModelAttribute("userStatusRequest") UserStatusRequest userStatusRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid user status request.");
            return "redirect:/admin/users";
        }

        adminService.updateUserStatus(userStatusRequest.getEmail(), userStatusRequest.getStatus());
        redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully for: " + userStatusRequest.getEmail());

        return "redirect:/admin/users";
    }

    @GetMapping("/merchants")
    public String getMerchants(
            @Valid @ModelAttribute("searchRequest") AdminSearchRequest searchRequest,
            BindingResult bindingResult,
            Model model) {
        MerchantResponse merchantResponse = MerchantResponse.builder().build();
        AdminEmailRequest merchantEmailRequest = AdminEmailRequest.builder().build();

        if (!bindingResult.hasErrors()) {
            merchantResponse = merchantService.getMerchantResponse(searchRequest.getSearchEmail());
        }

        model.addAttribute("merchantResponse", merchantResponse);
        flashValidationAttributes.addModelAttributeIfMissing(model, "merchantRequest", MerchantRequest.builder().build());
        model.addAttribute("merchantEmailRequest", merchantEmailRequest);
        model.addAttribute("searchHasErrors", bindingResult.hasErrors());
        return "admin/merchants";
    }

    @PostMapping("/merchants/assign")
    public String makeUserMerchant(
            @Valid @ModelAttribute("merchantRequest") MerchantRequest merchantRequest,
            BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            flashValidationAttributes.addValidationFlashAttribute(redirectAttributes, "merchantRequest", merchantRequest, bindingResult);
            return "redirect:/admin/merchants";
        }

        adminService.makeUserMerchant(merchantRequest);
        redirectAttributes.addFlashAttribute("successMessage","Merchant created successfully for: " + merchantRequest.getEmail());

        return "redirect:/admin/merchants";
    }

    @GetMapping("/couriers")
    public String getCouriers(
            @Valid @ModelAttribute("searchRequest") AdminSearchRequest searchRequest,
            BindingResult bindingResult,
            Model model) {
        CourierResponse courierResponse = CourierResponse.builder().build();

        if (!bindingResult.hasErrors()) {
            courierResponse = courierService.getCourierResponse(searchRequest.getSearchEmail());
        }

        model.addAttribute("courierResponse", courierResponse);
        flashValidationAttributes.addModelAttributeIfMissing(model, "courierEmailRequest", AdminEmailRequest.builder().build());
        model.addAttribute("searchHasErrors", bindingResult.hasErrors());
        return "admin/couriers";
    }

    @PostMapping("/couriers/assign")
    public String makeUserCourier(
            @Valid @ModelAttribute("courierEmailRequest") AdminEmailRequest courierEmailRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            flashValidationAttributes.addValidationFlashAttribute(redirectAttributes, "courierEmailRequest", courierEmailRequest, bindingResult);
            return "redirect:/admin/couriers";
        }

        adminService.makeUserCourier(courierEmailRequest.getEmail());
        redirectAttributes.addFlashAttribute("successMessage", "User assigned as courier successfully: " + courierEmailRequest.getEmail());
        return "redirect:/admin/couriers";
    }

    @GetMapping("/admins")
    public String getAllAdmins(
            @Valid @ModelAttribute("searchRequest") AdminSearchRequest searchRequest,
            BindingResult bindingResult,
            Model model) {
        List<UserResponse> admins = adminService.getAdmins();
        UserResponse userResponse = UserResponse.builder().build();
        AdminEmailRequest demoteAdminRequest = AdminEmailRequest.builder().build();

        if (!bindingResult.hasErrors()) {
            userResponse = adminService.getAdminByEmailAndRole(searchRequest.getSearchEmail());
        }

        model.addAttribute("admins", admins);
        model.addAttribute("userResponse", userResponse);
        flashValidationAttributes.addModelAttributeIfMissing(model, "adminEmailRequest", AdminEmailRequest.builder().build());
        model.addAttribute("demoteAdminRequest", demoteAdminRequest);
        model.addAttribute("searchHasErrors", bindingResult.hasErrors());
        return "admin/admins";
    }

    @PostMapping("/admins/assign")
    public String makeUserAdmin(
            @Valid @ModelAttribute("adminEmailRequest") AdminEmailRequest adminEmailRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            flashValidationAttributes.addValidationFlashAttribute(redirectAttributes, "adminEmailRequest", adminEmailRequest, bindingResult);
            return "redirect:/admin/admins";
        }

        adminService.makeUserAdmin(adminEmailRequest.getEmail());
        redirectAttributes.addFlashAttribute("successMessage", "User assigned as admin successfully: " + adminEmailRequest.getEmail());
        return "redirect:/admin/admins";
    }

    @PostMapping("/admins/demote")
    public String demoteAdmin(
            @Valid @ModelAttribute("demoteAdminRequest") AdminEmailRequest demoteAdminRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid admin demotion request.");
            return "redirect:/admin/admins";
        }

        adminService.demoteAdmin(demoteAdminRequest.getEmail());
        redirectAttributes.addFlashAttribute("successMessage", "Admin demoted successfully: " + demoteAdminRequest.getEmail());
        return "redirect:/admin/admins";
    }

    @PostMapping("/merchants/toggle-status")
    public String toggleMerchantStatus(
            @Valid @ModelAttribute("merchantEmailRequest") AdminEmailRequest merchantEmailRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid merchant status request.");
            return "redirect:/admin/merchants";
        }

        merchantService.toggleMerchantActiveStatus(merchantEmailRequest.getEmail());
        redirectAttributes.addFlashAttribute("successMessage", "Merchant status toggled successfully: " + merchantEmailRequest.getEmail());
        return "redirect:/admin/merchants";
    }

    @PostMapping("/couriers/toggle-status")
    public String toggleCourierActiveStatus(
            @Valid @ModelAttribute("courierEmailRequest") AdminEmailRequest courierEmailRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid courier status request.");
            return "redirect:/admin/couriers";
        }

        courierService.toggleCourierActiveStatus(courierEmailRequest.getEmail());
        redirectAttributes.addFlashAttribute("successMessage", "Courier active status toggled successfully: " + courierEmailRequest.getEmail());
        return "redirect:/admin/couriers";
    }
}
