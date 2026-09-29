package com.codewithpaolo.frischcheck.controller.web;

import com.codewithpaolo.frischcheck.dto.ProductForm;
import com.codewithpaolo.frischcheck.exception.BusinessRuleException;
import com.codewithpaolo.frischcheck.exception.ResourceNotFoundException;
import com.codewithpaolo.frischcheck.model.ProductStatus;
import com.codewithpaolo.frischcheck.model.StorageLocation;
import com.codewithpaolo.frischcheck.model.Unit;
import com.codewithpaolo.frischcheck.service.CategoryService;
import com.codewithpaolo.frischcheck.service.ExpiryService;
import com.codewithpaolo.frischcheck.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Mobile Weboberfläche (Thymeleaf). Nutzt dieselben Services wie die REST-API.
 */
@Controller
public class ProductWebController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ExpiryService expiryService;

    public ProductWebController(ProductService productService, CategoryService categoryService,
                                ExpiryService expiryService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.expiryService = expiryService;
    }

    @GetMapping("/")
    public String overview(Model model) {
        model.addAttribute("today", expiryService.today());
        model.addAttribute("expiring", productService.findExpiring());
        model.addAttribute("inStock", productService.findAll(ProductStatus.AUF_LAGER));
        return "index";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        ProductForm form = new ProductForm();
        form.setReceivedDate(expiryService.today());
        model.addAttribute("form", form);
        addFormOptions(model);
        return "product-form";
    }

    @PostMapping("/products")
    public String createProduct(@Valid @ModelAttribute("form") ProductForm form, BindingResult bindingResult,
                                Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormOptions(model);
            return "product-form";
        }
        productService.create(form.toRequest());
        redirectAttributes.addFlashAttribute("success", "„" + form.getName() + "“ wurde erfasst.");
        return "redirect:/";
    }

    @PostMapping("/products/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam ProductStatus status,
                               RedirectAttributes redirectAttributes) {
        try {
            var product = productService.changeStatus(id, status);
            String action = status == ProductStatus.VERBRAUCHT ? "verbraucht" : "weggeworfen";
            redirectAttributes.addFlashAttribute("success", "„" + product.name() + "“ als " + action + " markiert.");
        } catch (BusinessRuleException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/";
    }

    private void addFormOptions(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("locations", StorageLocation.values());
        model.addAttribute("units", Unit.values());
    }
}
