package org.tuvarna.smartdeliveryplatform.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.tuvarna.smartdeliveryplatform.shared.enums.CheckoutAddressMode;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderPlacementRequest;

import static org.springframework.util.StringUtils.hasText;

public class OrderPlacementValidator implements ConstraintValidator<ValidOrderPlacement, OrderPlacementRequest> {

    @Override
    public boolean isValid(OrderPlacementRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getAddressMode() == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        if (request.getAddressMode() == CheckoutAddressMode.EXISTING) {
            return validateExistingAddress(request, context);
        }

        if (request.getAddressMode() == CheckoutAddressMode.NEW) {
            return validateNewAddress(request, context);
        }

        return true;
    }

    private boolean validateExistingAddress(OrderPlacementRequest request, ConstraintValidatorContext context) {
        if (request.getAddressId() != null) {
            return true;
        }

        addViolation(context, "Choose one of your saved delivery addresses.", "addressId");
        return false;
    }

    private boolean validateNewAddress(OrderPlacementRequest request, ConstraintValidatorContext context) {
        boolean valid = true;

        if (!hasText(request.getCity())) {
            addViolation(context, "City is required.", "city");
            valid = false;
        }

        if (!hasText(request.getStreet())) {
            addViolation(context, "Street is required.", "street");
            valid = false;
        }

        if (!hasText(request.getBuilding())) {
            addViolation(context, "Building is required.", "building");
            valid = false;
        }

        return valid;
    }

    private void addViolation(ConstraintValidatorContext context, String message, String property) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(property)
                .addConstraintViolation();
    }
}
