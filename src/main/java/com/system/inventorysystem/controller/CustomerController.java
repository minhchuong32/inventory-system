package com.system.inventorysystem.controller;

import com.system.inventorysystem.entity.Customer;
import com.system.inventorysystem.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping
    public String listCustomer(Model model,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Customer> customers = customerService.findAll(search, page, size);
        model.addAttribute("customers", customers);
        model.addAttribute("stats", customerService.getCustomerStats());
        model.addAttribute("search", search);
        model.addAttribute("activePage", "customers");
        return "customer/list";
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("activePage", "customers");
        return "customer/form";
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return customerService.findById(id).map(c -> {
            model.addAttribute("customer", c);
            model.addAttribute("activePage", "customers");
            return "customer/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Không tìm thấy khách hàng");
            return "redirect:/customers";
        });
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Customer customer,
            BindingResult result,
            Model model,
            RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("activePage", "customers");
            return "customer/form";
        }
        try {
            if (customer.getId() != null) {
                customerService.update(customer.getId(), customer);
            } else {
                customerService.save(customer);
            }
            ra.addFlashAttribute("success", "Lưu khách hàng thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customers";
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            customerService.deleteById(id);
            ra.addFlashAttribute("success", "Xóa khách hàng thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customers";
    }

    // ============= REST API Endpoints (JSON) =============

    @GetMapping("/api/list")
    @ResponseBody
    public Page<Customer> apiListCustomers(@RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return customerService.findAll(search, page, size);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public Customer apiGetCustomer(@PathVariable Long id) {
        return customerService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + id));
    }

    @PostMapping("/api/create")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public Customer apiCreateCustomer(@Valid @RequestBody Customer customer) {
        return customerService.save(customer);
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public Customer apiUpdateCustomer(@PathVariable Long id, @Valid @RequestBody Customer customer) {
        return customerService.update(id, customer);
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void apiDeleteCustomer(@PathVariable Long id) {
        customerService.deleteById(id);
    }

    @GetMapping("/api/active")
    @ResponseBody
    public java.util.List<Customer> apiGetActiveCustomers() {
        return customerService.findAllActive();
    }

    @GetMapping("/api/count-active")
    @ResponseBody
    public long apiCountActiveCustomers() {
        return customerService.countActiveCustomers();
    }
}
