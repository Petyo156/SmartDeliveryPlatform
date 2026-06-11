Project architecture requirements

Backend:

* Follow the existing application style and conventions.
* Keep controllers thin.
* Keep business logic inside services.
* Use Request and Response DTOs only.
* Do not pass entities directly to the view layer.
* Do not use try/catch blocks inside controllers.
* Keep the implementation scalable and reusable.

Controller responsibilities:

* Call the Service.
* Use Request DTOs to receive data from the frontend with jakarta.validation.constraints.
* Return a Response DTO.
* Do not include business logic.
* Do not include filtering logic.
* Follow the current project standard.
* Avoid using `@ResponseBody`, since this controller follows the MVC pattern rather than acting as a REST API.
* It is cleaner to handle errors through exceptions and return the appropriate response from a centralized exception handler.

Frontend:

* Do not include business logic in Thymeleaf.
* Thymeleaf should only display data provided by the controller.
* Use fragments where necessary.
* Keep HTML pages clean.
* Avoid unnecessary CSS.