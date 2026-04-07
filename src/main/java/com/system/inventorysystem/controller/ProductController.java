package com.system.inventorysystem.controller;

import com.system.inventorysystem.entity.Category;
import com.system.inventorysystem.entity.Product;
import com.system.inventorysystem.entity.Supplier;
import com.system.inventorysystem.entity.Unit;
import com.system.inventorysystem.repository.CategoryRepository;
import com.system.inventorysystem.repository.ProductRepository;
import com.system.inventorysystem.repository.SupplierRepository;
import com.system.inventorysystem.repository.UnitRepository;
import com.system.inventorysystem.repository.WareHouseRepository;
import com.system.inventorysystem.service.FileUploadService;
import com.system.inventorysystem.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FileUploadService fileUploadService;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final WareHouseRepository warehouseRepository;
    private final UnitRepository unitRepository;
    private final ProductRepository productRepository;

    // ============= WEB ENDPOINTS (HTML Views) =============

    @GetMapping
    public String listProduct(Model model,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Product> products = productService.findAll(search, page, size);
        model.addAttribute("products", products);
        model.addAttribute("search", search);
        // Stats thực từ database
        model.addAttribute("countActive",   productService.countActiveProducts());
        model.addAttribute("countLowStock", productService.findLowStockProducts().size());
        model.addAttribute("totalStockValue", productRepository.getTotalStockValue());
        model.addAttribute("activePage", "products");
        return "product/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("suppliers", supplierRepository.findAll());
        model.addAttribute("warehouses", warehouseRepository.findAll());
        model.addAttribute("units", unitRepository.findAll());
        model.addAttribute("activePage", "products");
        return "product/form";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return productService.findById(id).map(p -> {
            model.addAttribute("product", p);
            model.addAttribute("activePage", "products");
            return "product/detail";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Không tìm thấy sản phẩm");
            return "redirect:/products";
        });
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return productService.findById(id).map(p -> {
            model.addAttribute("product", p);
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("suppliers", supplierRepository.findAll());
            model.addAttribute("warehouses", warehouseRepository.findAll());
            model.addAttribute("units", unitRepository.findAll());
            model.addAttribute("activePage", "products");
            return "product/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Không tìm thấy sản phẩm");
            return "redirect:/products";
        });
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Product product,
            BindingResult result,
            @RequestParam(required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("suppliers", supplierRepository.findAll());
            model.addAttribute("warehouses", warehouseRepository.findAll());
            model.addAttribute("units", unitRepository.findAll());
            model.addAttribute("activePage", "products");
            return "product/form";
        }

        try {
            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                if (fileUploadService.isValidImage(imageFile)) {
                    String imageUrl = fileUploadService.uploadProductImage(imageFile);
                    product.setImageUrl(imageUrl);
                } else {
                    ra.addFlashAttribute("error", "Định dạng hình ảnh không hỗ trợ");
                    return "redirect:/products/" + (product.getId() != null ? product.getId() + "/edit" : "new");
                }
            }

            // Save or update
            if (product.getId() != null) {
                productService.update(product.getId(), product);
            } else {
                productService.save(product);
            }
            ra.addFlashAttribute("success", "Lưu sản phẩm thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productService.deleteById(id);
            ra.addFlashAttribute("success", "Xóa sản phẩm thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products";
    }

    // ============= REST API Endpoints (JSON) =============

    @GetMapping("/api/list")
    @ResponseBody
    public Page<Product> apiListProducts(@RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return productService.findAll(search, page, size);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public Product apiGetProduct(@PathVariable Long id) {
        return productService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
    }

    @PostMapping("/api/create")
    @ResponseBody
    public Product apiCreateProduct(@Valid @RequestBody Product product) {
        return productService.save(product);
    }

    @PostMapping("/api/{id}/upload-image")
    @ResponseBody
    public java.util.Map<String, Object> apiUploadProductImage(@PathVariable Long id,
            @RequestParam("image") MultipartFile imageFile) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        if (!fileUploadService.isValidImage(imageFile)) {
            throw new RuntimeException("Định dạng hình ảnh không hỗ trợ");
        }

        String imageUrl = fileUploadService.uploadProductImage(imageFile);
        product.setImageUrl(imageUrl);
        productService.save(product);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("imageUrl", imageUrl);
        return response;
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public Product apiUpdateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        return productService.update(id, product);
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public void apiDeleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
    }

    @GetMapping("/api/active")
    @ResponseBody
    public List<Product> apiGetActiveProducts() {
        return productService.findAllActive();
    }

    @GetMapping("/api/low-stock")
    @ResponseBody
    public List<Product> apiGetLowStockProducts() {
        return productService.findLowStockProducts();
    }

    @GetMapping("/api/count-active")
    @ResponseBody
    public long apiCountActiveProducts() {
        return productService.countActiveProducts();
    }

    @PostMapping("/api/{id}/adjust-stock")
    @ResponseBody
    public void apiAdjustStock(@PathVariable Long id,
            @RequestParam int delta,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String refCode) {
        productService.adjustStock(id, delta, reason != null ? reason : "Manual adjustment", refCode);
    }
}
