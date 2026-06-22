package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.ModelAndView;
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

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final MerchantService merchantService;
    private final CourierService courierService;

    public AdminController(AdminService adminService, MerchantService merchantService, CourierService courierService) {
        this.adminService = adminService;
        this.merchantService = merchantService;
        this.courierService = courierService;
    }

    @GetMapping("/users")
    public ModelAndView getUsers(
            @Valid @ModelAttribute("searchRequest") AdminSearchRequest searchRequest,
            BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView("admin/users");

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

        modelAndView.addObject("userResponse", userResponse);
        modelAndView.addObject("userStatusRequest", userStatusRequest);
        modelAndView.addObject("userStatuses", UserStatus.values());
        modelAndView.addObject("searchHasErrors", bindingResult.hasErrors());
        return modelAndView;
    }

    @PostMapping("/users/status")
    public ModelAndView updateUserStatus(
            @Valid @ModelAttribute("userStatusRequest") UserStatusRequest userStatusRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid user status request.");
            return new ModelAndView("redirect:/admin/users");
        }

        adminService.updateUserStatus(userStatusRequest.getEmail(), userStatusRequest.getStatus());
        redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully for: " + userStatusRequest.getEmail());

        return new ModelAndView("redirect:/admin/users");
    }

    @GetMapping("/merchants")
    public ModelAndView getMerchants(
            @Valid @ModelAttribute("searchRequest") AdminSearchRequest searchRequest,
            BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView("admin/merchants");
        MerchantResponse merchantResponse = MerchantResponse.builder().build();
        MerchantRequest merchantRequest = MerchantRequest.builder().build();
        AdminEmailRequest merchantEmailRequest = AdminEmailRequest.builder().build();

        if (!bindingResult.hasErrors()) {
            merchantResponse = merchantService.getMerchantResponse(searchRequest.getSearchEmail());
        }

        modelAndView.addObject("merchantResponse", merchantResponse);
        modelAndView.addObject("merchantRequest", merchantRequest);
        modelAndView.addObject("merchantEmailRequest", merchantEmailRequest);
        modelAndView.addObject("searchHasErrors", bindingResult.hasErrors());
        return modelAndView;
    }

    @PostMapping("/merchants/assign")
    public ModelAndView makeUserMerchant(
            @Valid @ModelAttribute("merchantRequest") MerchantRequest merchantRequest,
            BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("admin/merchants");
            MerchantResponse merchantResponse = MerchantResponse.builder().build();
            AdminEmailRequest merchantEmailRequest = AdminEmailRequest.builder().build();

            modelAndView.addObject("merchantResponse", merchantResponse);
            modelAndView.addObject("searchRequest", AdminSearchRequest.builder().build());
            modelAndView.addObject("merchantRequest", merchantRequest);
            modelAndView.addObject("merchantEmailRequest", merchantEmailRequest);
            modelAndView.addObject("searchHasErrors", false);
            return modelAndView;
        }

        adminService.makeUserMerchant(merchantRequest);
        redirectAttributes.addFlashAttribute("successMessage","Merchant created successfully for: " + merchantRequest.getEmail());

        return new ModelAndView("redirect:/admin/merchants");
    }

    @GetMapping("/couriers")
    public ModelAndView getCouriers(
            @Valid @ModelAttribute("searchRequest") AdminSearchRequest searchRequest,
            BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView("admin/couriers");
        CourierResponse courierResponse = CourierResponse.builder().build();
        AdminEmailRequest courierEmailRequest = AdminEmailRequest.builder().build();

        if (!bindingResult.hasErrors()) {
            courierResponse = courierService.getCourierResponse(searchRequest.getSearchEmail());
        }

        modelAndView.addObject("courierResponse", courierResponse);
        modelAndView.addObject("courierEmailRequest", courierEmailRequest);
        modelAndView.addObject("searchHasErrors", bindingResult.hasErrors());
        return modelAndView;
    }

    @PostMapping("/couriers/assign")
    public ModelAndView makeUserCourier(
            @Valid @ModelAttribute("courierEmailRequest") AdminEmailRequest courierEmailRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("admin/couriers");
            CourierResponse courierResponse = CourierResponse.builder().build();

            modelAndView.addObject("courierResponse", courierResponse);
            modelAndView.addObject("searchRequest", AdminSearchRequest.builder().build());
            modelAndView.addObject("courierEmailRequest", courierEmailRequest);
            modelAndView.addObject("searchHasErrors", false);
            return modelAndView;
        }

        adminService.makeUserCourier(courierEmailRequest.getEmail());
        redirectAttributes.addFlashAttribute("successMessage", "User assigned as courier successfully: " + courierEmailRequest.getEmail());
        return new ModelAndView("redirect:/admin/couriers");
    }

    @GetMapping("/admins")
    public ModelAndView getAllAdmins(
            @Valid @ModelAttribute("searchRequest") AdminSearchRequest searchRequest,
            BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView("admin/admins");
        List<UserResponse> admins = adminService.getAdmins();
        UserResponse userResponse = UserResponse.builder().build();
        AdminEmailRequest adminEmailRequest = AdminEmailRequest.builder().build();
        AdminEmailRequest demoteAdminRequest = AdminEmailRequest.builder().build();

        if (!bindingResult.hasErrors()) {
            userResponse = adminService.getAdminByEmailAndRole(searchRequest.getSearchEmail());
        }

        modelAndView.addObject("admins", admins);
        modelAndView.addObject("userResponse", userResponse);
        modelAndView.addObject("adminEmailRequest", adminEmailRequest);
        modelAndView.addObject("demoteAdminRequest", demoteAdminRequest);
        modelAndView.addObject("searchHasErrors", bindingResult.hasErrors());
        return modelAndView;
    }

    @PostMapping("/admins/assign")
    public ModelAndView makeUserAdmin(
            @Valid @ModelAttribute("adminEmailRequest") AdminEmailRequest adminEmailRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("admin/admins");
            List<UserResponse> admins = adminService.getAdmins();
            UserResponse userResponse = UserResponse.builder().build();
            AdminEmailRequest demoteAdminRequest = AdminEmailRequest.builder().build();

            modelAndView.addObject("admins", admins);
            modelAndView.addObject("userResponse", userResponse);
            modelAndView.addObject("searchRequest", AdminSearchRequest.builder().build());
            modelAndView.addObject("adminEmailRequest", adminEmailRequest);
            modelAndView.addObject("demoteAdminRequest", demoteAdminRequest);
            modelAndView.addObject("searchHasErrors", false);
            return modelAndView;
        }

        adminService.makeUserAdmin(adminEmailRequest.getEmail());
        redirectAttributes.addFlashAttribute("successMessage", "User assigned as admin successfully: " + adminEmailRequest.getEmail());
        return new ModelAndView("redirect:/admin/admins");
    }

    @PostMapping("/admins/demote")
    public ModelAndView demoteAdmin(
            @Valid @ModelAttribute("demoteAdminRequest") AdminEmailRequest demoteAdminRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid admin demotion request.");
            return new ModelAndView("redirect:/admin/admins");
        }

        adminService.demoteAdmin(demoteAdminRequest.getEmail());
        redirectAttributes.addFlashAttribute("successMessage", "Admin demoted successfully: " + demoteAdminRequest.getEmail());
        return new ModelAndView("redirect:/admin/admins");
    }

    @PostMapping("/merchants/toggle-status")
    public ModelAndView toggleMerchantStatus(
            @Valid @ModelAttribute("merchantEmailRequest") AdminEmailRequest merchantEmailRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid merchant status request.");
            return new ModelAndView("redirect:/admin/merchants");
        }

        merchantService.toggleMerchantActiveStatus(merchantEmailRequest.getEmail());
        redirectAttributes.addFlashAttribute("successMessage", "Merchant status toggled successfully: " + merchantEmailRequest.getEmail());
        return new ModelAndView("redirect:/admin/merchants");
    }

    @PostMapping("/couriers/toggle-status")
    public ModelAndView toggleCourierActiveStatus(
            @Valid @ModelAttribute("courierEmailRequest") AdminEmailRequest courierEmailRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid courier status request.");
            return new ModelAndView("redirect:/admin/couriers");
        }

        courierService.toggleCourierActiveStatus(courierEmailRequest.getEmail());
        redirectAttributes.addFlashAttribute("successMessage", "Courier active status toggled successfully: " + courierEmailRequest.getEmail());
        return new ModelAndView("redirect:/admin/couriers");
    }
}
