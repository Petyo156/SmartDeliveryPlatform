package org.tuvarna.smartdeliveryplatform.web.util;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Component
public class FlashValidationAttributes {
    private static final String BINDING_RESULT_PREFIX = "org.springframework.validation.BindingResult.";

    public void addModelAttributeIfMissing(Model model,
                                           String attributeName,
                                           Object defaultValue) {
        if (!model.containsAttribute(attributeName)) {
            model.addAttribute(attributeName, defaultValue);
        }
    }

    public void addValidationFlashAttribute(RedirectAttributes redirectAttributes,
                                            String attributeName,
                                            Object attributeValue,
                                            BindingResult bindingResult) {
        redirectAttributes.addFlashAttribute(attributeName, attributeValue);
        redirectAttributes.addFlashAttribute(BINDING_RESULT_PREFIX + attributeName, bindingResult);
    }
}
