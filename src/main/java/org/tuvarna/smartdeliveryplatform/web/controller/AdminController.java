package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.tuvarna.smartdeliveryplatform.courier.service.CourierService;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserStatus;
import org.tuvarna.smartdeliveryplatform.user.service.AdminService;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.CourierResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.UserResponse;

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
    public ModelAndView getUsers(@RequestParam(required = false) String searchEmail) {
        ModelAndView modelAndView = new ModelAndView("admin/users");

        UserResponse userResponse = UserResponse.builder().build();
        if (searchEmail != null && !searchEmail.isBlank()) {
            userResponse = adminService.getUserByEmail(searchEmail);
        }

        modelAndView.addObject("userResponse", userResponse);
        modelAndView.addObject("searchEmail", searchEmail);
        modelAndView.addObject("userStatuses", UserStatus.values());
        return modelAndView;
    }

    @PostMapping("/users/status")
    public ModelAndView updateUserStatus(@RequestParam String email, @RequestParam UserStatus status, RedirectAttributes redirectAttributes) {
        adminService.updateUserStatus(email, status);
        redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully for: " + email);

        return new ModelAndView("redirect:/admin/users");
    }

    @GetMapping("/merchants")
    public ModelAndView getMerchants(@RequestParam(required = false) String searchEmail) {
        ModelAndView modelAndView = new ModelAndView("admin/merchants");
        MerchantResponse merchantResponse = merchantService.getMerchantResponse(searchEmail);
        MerchantRequest merchantRequest = MerchantRequest.builder().build();

        modelAndView.addObject("merchantResponse", merchantResponse);
        modelAndView.addObject("searchEmail", searchEmail);
        modelAndView.addObject("merchantRequest", merchantRequest);
        return modelAndView;
    }

    @PostMapping("/merchants/assign")
    public ModelAndView makeUserMerchant(
            @Valid @ModelAttribute("merchantRequest") MerchantRequest merchantRequest,
            BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("admin/merchants");
            modelAndView.addObject("merchantResponse", MerchantResponse.builder().build());
            return modelAndView;
        }

        adminService.makeUserMerchant(merchantRequest);
        redirectAttributes.addFlashAttribute("successMessage","Merchant created successfully for: " + merchantRequest.getEmail());

        return new ModelAndView("redirect:/admin/merchants");
    }

    @GetMapping("/couriers")
    public ModelAndView getCouriers(@RequestParam(required = false) String searchEmail) {
        ModelAndView modelAndView = new ModelAndView("admin/couriers");
        CourierResponse courierResponse = courierService.getCourierResponse(searchEmail);

        modelAndView.addObject("courierResponse", courierResponse);
        modelAndView.addObject("searchEmail", searchEmail);
        return modelAndView;
    }

    @PostMapping("/couriers/assign")
    public ModelAndView makeUserCourier(@RequestParam String email, RedirectAttributes redirectAttributes) {
        if (null == email || email.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email is required.");
            return new ModelAndView("redirect:/admin/couriers");
        }

        adminService.makeUserCourier(email);
        redirectAttributes.addFlashAttribute("successMessage", "User assigned as courier successfully: " + email);
        return new ModelAndView("redirect:/admin/couriers");
    }

    @GetMapping("/admins")
    public ModelAndView getAllAdmins(@RequestParam(required = false) String searchEmail) {
        ModelAndView modelAndView = new ModelAndView("admin/admins");
        List<UserResponse> admins = adminService.getAdmins();
        UserResponse userResponse = adminService.getAdminByEmailAndRole(searchEmail);

        modelAndView.addObject("admins", admins);
        modelAndView.addObject("userResponse", userResponse);
        modelAndView.addObject("searchEmail", searchEmail);
        return modelAndView;
    }

    @PostMapping("/admins/assign")
    public ModelAndView makeUserAdmin(@RequestParam String email, RedirectAttributes redirectAttributes) {
        if (null == email || email.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email is required.");
            return new ModelAndView("redirect:/admin/admins");
        }

        adminService.makeUserAdmin(email);
        redirectAttributes.addFlashAttribute("successMessage", "User assigned as admin successfully: " + email);
        return new ModelAndView("redirect:/admin/admins");
    }

    @PostMapping("/admins/demote")
    public ModelAndView demoteAdmin(@RequestParam String email, RedirectAttributes redirectAttributes) {
        if (null == email || email.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email is required.");
            return new ModelAndView("redirect:/admin/admins");
        }

        adminService.demoteAdmin(email);
        redirectAttributes.addFlashAttribute("successMessage", "Admin demoted successfully: " + email);
        return new ModelAndView("redirect:/admin/admins");
    }

    @PostMapping("/merchants/toggle-status")
    public ModelAndView toggleMerchantStatus(@RequestParam String email, RedirectAttributes redirectAttributes) {
        if (null == email || email.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email is required.");
            return new ModelAndView("redirect:/admin/merchants");
        }

        merchantService.toggleMerchantActiveStatus(email);
        redirectAttributes.addFlashAttribute("successMessage", "Merchant status toggled successfully: " + email);
        return new ModelAndView("redirect:/admin/merchants");
    }

    @PostMapping("/couriers/toggle-status")
    public ModelAndView toggleCourierStatus(@RequestParam String email, RedirectAttributes redirectAttributes) {
        if (null == email || email.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email is required.");
            return new ModelAndView("redirect:/admin/couriers");
        }

        courierService.toggleCourierStatus(email);
        redirectAttributes.addFlashAttribute("successMessage", "Courier status toggled successfully: " + email);
        return new ModelAndView("redirect:/admin/couriers");
    }
}
