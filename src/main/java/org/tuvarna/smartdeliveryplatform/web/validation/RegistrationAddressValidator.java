package org.tuvarna.smartdeliveryplatform.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.tuvarna.smartdeliveryplatform.shared.constants.ValidationMessages;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.AddressRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.RegisterRequest;

import static org.springframework.util.StringUtils.hasText;

public class RegistrationAddressValidator implements ConstraintValidator<ValidRegistrationAddress, RegisterRequest> {

    @Override
    public boolean isValid(RegisterRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getAddressRequest() == null) {
            return true;
        }

        AddressRequest addressRequest = request.getAddressRequest();
        boolean hasAnyAddressField = hasText(addressRequest.getCity())
                || hasText(addressRequest.getStreet())
                || hasText(addressRequest.getBuilding());

        if (!hasAnyAddressField) {
            return true;
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (!hasText(addressRequest.getCity())) {
            addViolation(context, ValidationMessages.CITY_REQUIRED_WHEN_ADDING_ADDRESS, "city");
            valid = false;
        }

        if (!hasText(addressRequest.getStreet())) {
            addViolation(context, ValidationMessages.STREET_REQUIRED_WHEN_ADDING_ADDRESS, "street");
            valid = false;
        }

        if (!hasText(addressRequest.getBuilding())) {
            addViolation(context, ValidationMessages.BUILDING_REQUIRED_WHEN_ADDING_ADDRESS, "building");
            valid = false;
        }

        return valid;
    }

    private void addViolation(ConstraintValidatorContext context, String message, String property) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("addressRequest")
                .addPropertyNode(property)
                .addConstraintViolation();
    }
}
