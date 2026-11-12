package com.restman.web;

import com.restman.controller.ComboController3;
import com.restman.controller.SearchDishController3;
import com.restman.controller.UserController3;
import com.restman.dao.RestmanWebDAO3;
import com.restman.model.Combo3;
import com.restman.model.Dish3;
import com.restman.model.User3;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RestmanWebController {
    private final SearchDishController3 searchDishController = new SearchDishController3();
    private final ComboController3 comboController = new ComboController3();
    private final UserController3 userController = new UserController3();
    private final RestmanWebDAO3 webDAO = new RestmanWebDAO3();

    @GetMapping({"/", "/search"})
    public String index(HttpSession session,
                        @RequestParam(value = "keyword", required = false) String keyword,
                        @RequestParam(name = "category", defaultValue = "") String category,
                        @RequestParam(name = "error", required = false) String error,
                        Model model) {
        Map<String, Object> user = getLoggedUser(session);
        if (user == null) return "redirect:/login";
        keyword = keyword == null ? "" : keyword;
        category = category == null ? "" : category;
        model.addAttribute("loggedUser", user);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("activePage", "search");
        if (error != null) model.addAttribute("error", error);
        try {
            model.addAttribute("categories", webDAO.getDishCategories());
            List<Dish3> dishList = webDAO.searchDishes(keyword, category);
            model.addAttribute("dishList", dishList);
            model.addAttribute("dishes", dishList);
        } catch (SQLException exception) {
            model.addAttribute("errorMessage", databaseError(exception));
            model.addAttribute("dishList", List.of());
            model.addAttribute("dishes", List.of());
        }
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session,
                            @RequestParam(name = "registered", required = false) String registered,
                            Model model) {
        if (getLoggedUser(session) != null) return "redirect:/dashboard";
        model.addAttribute("activePage", "login");
        if ("true".equals(registered)) {
            model.addAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session, Model model) {
        if (getLoggedUser(session) != null) return "redirect:/dashboard";
        model.addAttribute("username", "");
        model.addAttribute("fullName", "");
        model.addAttribute("phone", "");
        model.addAttribute("email", "");
        model.addAttribute("address", "");
        return "register";
    }

    @PostMapping("/register")
    public String registerCustomer(@RequestParam(defaultValue = "") String username,
                                   @RequestParam(defaultValue = "") String password,
                                   @RequestParam(defaultValue = "") String confirmPassword,
                                   @RequestParam(defaultValue = "") String fullName,
                                   @RequestParam(defaultValue = "") String phone,
                                   @RequestParam(defaultValue = "") String email,
                                   @RequestParam(defaultValue = "") String address,
                                   Model model) {
        model.addAttribute("username", username);
        model.addAttribute("fullName", fullName);
        model.addAttribute("phone", phone);
        model.addAttribute("email", email);
        model.addAttribute("address", address);
        if (password.isBlank() || !password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu và xác nhận mật khẩu không khớp hoặc đang để trống.");
            return "register";
        }
        try {
            if (!userController.registerCustomer(username, password, fullName, phone, email, address)) {
                model.addAttribute("errorMessage", "Tên đăng nhập đã tồn tại.");
                return "register";
            }
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        } catch (SQLException exception) {
            String message = exception.getMessage();
            model.addAttribute("errorMessage", message != null && message.contains("đã được sử dụng")
                    ? message : databaseError(exception));
        }
        return "register";
    }

    @PostMapping("/login")
    public String login(@RequestParam(name = "username", defaultValue = "") String username,
                        @RequestParam(name = "password", defaultValue = "") String password,
                        HttpSession session, Model model) {
        return authenticate(username, password, session, model);
    }

    @PostMapping("/login/demo")
    public String demoLogin(@RequestParam("role") String role, HttpSession session, Model model) {
        Map<String, String[]> demoAccounts = Map.of(
                "Manager", new String[]{"manager", "manager123"},
                "Customer", new String[]{"customer", "customer123"},
                "SalesStaff", new String[]{"sales", "sales123"},
                "InventoryStaff", new String[]{"inventory", "inventory123"});
        String[] credentials = demoAccounts.get(role);
        if (credentials == null) {
            model.addAttribute("errorMessage", "Vai trò demo không hợp lệ.");
            return "login";
        }
        return authenticate(credentials[0], credentials[1], session, model);
    }

    private String authenticate(String username, String password, HttpSession session, Model model) {
        try {
            Optional<User3> userOptional = userController.login(username, password);
            if (userOptional.isEmpty()) {
                model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không đúng.");
                model.addAttribute("activePage", "login");
                return "login";
            }
            User3 user = userOptional.get();
            String role = userController.getRoleByUserId(user.getId());
            Map<String, Object> loggedInUser = new LinkedHashMap<>();
            loggedInUser.put("userId", user.getId());
            loggedInUser.put("username", user.getUsername());
            loggedInUser.put("role", role);
            loggedInUser.put("displayName", userController.getDisplayNameByUserId(user.getId(), role));
            session.setAttribute("loggedInUser", loggedInUser);
            return "redirect:/dashboard";
        } catch (SQLException exception) {
            model.addAttribute("errorMessage", databaseError(exception));
            model.addAttribute("activePage", "login");
            return "login";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model,
                            @RequestParam(name = "success", required = false) String success,
                            @RequestParam(name = "error", required = false) String error,
                            @RequestParam(name = "keyword", defaultValue = "") String keyword,
                            @RequestParam(name = "category", defaultValue = "") String category,
                            @RequestParam(name = "paymentOrderId", required = false) Integer paymentOrderId,
                            @RequestParam(name = "membershipLookup", required = false) String membershipLookup) {
        Map<String, Object> user = getLoggedUser(session);
        if (user == null) return "redirect:/login";
        String role = String.valueOf(user.get("role"));
        model.addAttribute("loggedUser", user);
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        if (success != null) model.addAttribute("successMessage", success);
        if (error != null) model.addAttribute("errorMessage", error);
        try {
            model.addAttribute("dishCount", webDAO.count("tblDish3"));
            if ("Manager".equals(role)) {
                model.addAttribute("comboCount", webDAO.count("tblCombo3"));
                model.addAttribute("customerCount", webDAO.count("tblCustomer3"));
                model.addAttribute("ingredientCount", webDAO.count("tblIngredient3"));
                model.addAttribute("supplierCount", webDAO.count("tblSupplier3"));
                model.addAttribute("statistics", webDAO.getStatistics());
            } else if ("Customer".equals(role)) {
                model.addAttribute("tables", webDAO.getAvailableTables(LocalDateTime.now().plusDays(1)));
                model.addAttribute("categories", webDAO.getDishCategories());
                model.addAttribute("dishes", webDAO.searchDishes(keyword, category));
                List<Map<String, Object>> cartItems = webDAO.getCartItems(cartFromSession(session, "onlineOrderCart"));
                model.addAttribute("cartItems", cartItems);
                model.addAttribute("totalAmount", cartTotal(cartItems));
            } else if ("SalesStaff".equals(role)) {
                model.addAttribute("pendingReservations", webDAO.getPendingReservations());
                model.addAttribute("pendingOrders", webDAO.getPendingOrders());
                model.addAttribute("paymentOrders", webDAO.getOrdersForPayment());
                model.addAttribute("billHistory", webDAO.getBillHistory());
                model.addAttribute("membershipCards", webDAO.getMembershipCards());
                model.addAttribute("cashierName", user.get("displayName"));
                model.addAttribute("tables", webDAO.getDiningTables());
                model.addAttribute("categories", webDAO.getDishCategories());
                model.addAttribute("dishes", webDAO.searchDishes(keyword, category));
                List<Map<String, Object>> cartItems = webDAO.getCartItems(cartFromSession(session, "tableOrderCart"));
                model.addAttribute("cartItems", cartItems);
                model.addAttribute("totalAmount", cartTotal(cartItems));
                if (paymentOrderId != null) {
                    Map<String, Object> selectedPaymentOrder = webDAO.getOrderPaymentDetails(paymentOrderId);
                    model.addAttribute("selectedPaymentOrder", selectedPaymentOrder);
                    if (selectedPaymentOrder == null) {
                        model.addAttribute("errorMessage", "Đơn hàng không còn chờ thanh toán.");
                    } else {
                        BigDecimal originalAmount = (BigDecimal) selectedPaymentOrder.get("totalAmount");
                        BigDecimal discountAmount = BigDecimal.ZERO.setScale(2);
                        String paymentCustomerName = (String) selectedPaymentOrder.get("customerName");
                        Map<String, Object> selectedMembershipCard = null;
                        if (membershipLookup != null && !membershipLookup.isBlank()) {
                            selectedMembershipCard = webDAO.findMembershipCard(membershipLookup.trim());
                            if (selectedMembershipCard == null) {
                                model.addAttribute("membershipError", "Không tìm thấy thẻ thành viên hợp lệ");
                            } else {
                                paymentCustomerName = (String) selectedMembershipCard.get("customerName");
                                discountAmount = originalAmount.multiply(new BigDecimal("0.10"))
                                        .setScale(2, RoundingMode.HALF_UP);
                            }
                        }
                        model.addAttribute("selectedMembershipCard", selectedMembershipCard);
                        model.addAttribute("paymentCustomerName", paymentCustomerName);
                        model.addAttribute("discountAmount", discountAmount);
                        model.addAttribute("paymentFinalAmount", originalAmount.subtract(discountAmount));
                    }
                }
                model.addAttribute("membershipLookup", membershipLookup == null ? "" : membershipLookup);
            } else if ("InventoryStaff".equals(role)) {
                model.addAttribute("suppliers", webDAO.getSuppliers());
                model.addAttribute("ingredients", webDAO.getIngredients());
            }
        } catch (SQLException exception) {
            model.addAttribute("errorMessage", databaseError(exception));
            model.addAttribute("dishes", List.of());
            model.addAttribute("cartItems", List.of());
            model.addAttribute("totalAmount", BigDecimal.ZERO);
            model.addAttribute("membershipCards", List.of());
        }
        if ("Manager".equals(role)) return "manager-dashboard";
        if ("SalesStaff".equals(role)) return "sales-dashboard";
        if ("InventoryStaff".equals(role)) return "inventory-dashboard";
        return "customer-dashboard";
    }

    @GetMapping({"/manager-dashboard", "/sales-dashboard", "/inventory-dashboard", "/customer-dashboard"})
    public String roleDashboard(HttpSession session, Model model) {
        return dashboard(session, model, null, null, "", "", null, null);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/dish/{id}")
    public String detail(HttpSession session, @PathVariable("id") int id, Model model) {
        Map<String, Object> user = getLoggedUser(session);
        if (user == null) return "redirect:/login";
        try {
            Optional<Dish3> dish = searchDishController.getDishDetail(id);
            if (dish.isEmpty()) return "redirect:/search?error=DishNotFound";
            model.addAttribute("dish", dish.get());
            model.addAttribute("loggedUser", user);
            model.addAttribute("activePage", "search");
            return "detail";
        } catch (SQLException exception) {
            model.addAttribute("errorMessage", databaseError(exception));
            model.addAttribute("loggedUser", user);
            return "detail";
        }
    }

    @GetMapping("/combo")
    public String combo(HttpSession session,
                        @RequestParam(name = "keyword", defaultValue = "") String keyword,
                        @RequestParam(name = "editId", required = false) Integer editId,
                        @RequestParam(name = "success", required = false) String success,
                        @RequestParam(name = "error", required = false) String error,
                        Model model) {
        Map<String, Object> user = requireRole(session, "Manager");
        if (user == null) return "redirect:/dashboard";
        try {
            List<Dish3> selectedDishes = selectedDishes(session);
            model.addAttribute("loggedUser", user);
            model.addAttribute("activePage", "combo");
            model.addAttribute("keyword", keyword);
            model.addAttribute("dishOptions", searchDishController.searchDish(editId == null ? keyword : ""));
            model.addAttribute("selectedDishes", selectedDishes);
            model.addAttribute("selectedDishIds", selectedDishes.stream().map(Dish3::getId).toList());
            model.addAttribute("combos", comboController.getAllCombos());
            if (success != null) model.addAttribute("successMessage", success);
            if (error != null) model.addAttribute("errorMessage", error);
            if (editId != null) {
                Optional<Combo3> combo = comboController.getComboById(editId);
                if (combo.isPresent()) {
                    model.addAttribute("comboId", combo.get().getId());
                    model.addAttribute("comboName", combo.get().getComboName());
                    model.addAttribute("comboPrice", combo.get().getPrice().toPlainString());
                    model.addAttribute("comboDescription", combo.get().getDescription());
                    model.addAttribute("selectedDishIds", combo.get().getDishes().stream().map(Dish3::getId).toList());
                }
            }
            if (editId == null) {
                model.addAttribute("comboId", "");
                model.addAttribute("comboName", "");
                model.addAttribute("comboPrice", "");
                model.addAttribute("comboDescription", "");
            }
        } catch (SQLException exception) {
            model.addAttribute("errorMessage", databaseError(exception));
            model.addAttribute("dishOptions", List.of());
            model.addAttribute("combos", List.of());
            model.addAttribute("selectedDishes", List.of());
            model.addAttribute("selectedDishIds", List.of());
        }
        return "manage-combo";
    }

    @PostMapping("/combo/add-dish")
    public String addComboDish(HttpSession session, @RequestParam int dishId,
                               @RequestParam(defaultValue = "") String keyword) throws SQLException {
        if (requireRole(session, "Manager") == null) return "redirect:/dashboard";
        List<Dish3> selected = selectedDishes(session);
        if (selected.stream().noneMatch(dish -> dish.getId() == dishId)) {
            searchDishController.getDishDetail(dishId).ifPresent(selected::add);
        }
        session.setAttribute("selectedDishes", selected);
        return "redirect:/combo?keyword=" + java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8);
    }

    @PostMapping("/combo/remove-dish/{id}")
    public String removeComboDish(HttpSession session, @PathVariable int id,
                                  @RequestParam(defaultValue = "") String keyword) {
        if (requireRole(session, "Manager") == null) return "redirect:/dashboard";
        List<Dish3> selected = selectedDishes(session);
        selected.removeIf(dish -> dish.getId() == id);
        session.setAttribute("selectedDishes", selected);
        return "redirect:/combo?keyword=" + java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8);
    }

    @PostMapping("/combo/save")
    public String saveCombo(HttpSession session,
                            @RequestParam(name = "comboId", required = false) Integer comboId,
                            @RequestParam String comboName,
                            @RequestParam String comboPrice,
                            @RequestParam(defaultValue = "") String comboDescription,
                            @RequestParam(name = "dishIds", required = false) List<Integer> dishIds,
                            RedirectAttributes redirect) {
        if (requireRole(session, "Manager") == null) return "redirect:/dashboard";
        try {
            List<Integer> selectedIds = comboId == null
                    ? selectedDishes(session).stream().map(Dish3::getId).toList()
                    : (dishIds == null ? List.of() : dishIds);
            if (selectedIds.size() < 2) {
                redirect.addAttribute("error", "Vui lòng chọn ít nhất 2 món để tạo combo");
                return "redirect:/combo";
            }
            List<Dish3> availableDishes = searchDishController.searchDish("");
            List<Dish3> dishes = availableDishes.stream().filter(dish -> selectedIds.contains(dish.getId())).toList();
            if (dishes.size() != selectedIds.stream().distinct().count()) {
                redirect.addAttribute("error", "Một hoặc nhiều món đã chọn không còn tồn tại.");
                return "redirect:/combo";
            }
            Combo3 combo = new Combo3();
            if (comboId != null) combo.setId(comboId);
            combo.setComboName(comboName.trim());
            combo.setPrice(new BigDecimal(comboPrice));
            combo.setDescription(comboDescription);
            combo.setDishes(dishes);
            if (comboId == null) {
                if (comboController.createCombo(combo) <= 0) throw new SQLException("Không thể lưu combo.");
                session.removeAttribute("selectedDishes");
                redirect.addAttribute("success", "Tạo combo thành công!");
            } else {
                if (!comboController.updateCombo(combo)) throw new SQLException("Không thể cập nhật combo.");
                redirect.addAttribute("success", "Cập nhật combo thành công!");
            }
            return "redirect:/combo";
        } catch (NumberFormatException exception) {
            redirect.addAttribute("error", "Giá combo không hợp lệ.");
            return "redirect:/combo";
        } catch (SQLException exception) {
            redirect.addAttribute("error", databaseError(exception));
            return "redirect:/combo";
        }
    }

    @PostMapping("/combo/delete/{id}")
    public String deleteCombo(HttpSession session, @PathVariable int id, RedirectAttributes redirect) {
        if (requireRole(session, "Manager") == null) return "redirect:/dashboard";
        try {
            if (comboController.deleteCombo(id)) redirect.addAttribute("success", "Xóa combo thành công!");
            else redirect.addAttribute("error", "Không tìm thấy combo cần xóa.");
        } catch (SQLException exception) {
            redirect.addAttribute("error", databaseError(exception));
        }
        return "redirect:/combo";
    }

    @GetMapping("/dishes")
    public String manageDishes(HttpSession session, @RequestParam(required = false) Integer editId,
                               @RequestParam(required = false) String success,
                               @RequestParam(required = false) String error, Model model) {
        if (requireRole(session, "Manager") == null) return "redirect:/dashboard";
        try {
            model.addAttribute("loggedUser", getLoggedUser(session));
            model.addAttribute("dishes", webDAO.searchDishes("", ""));
            model.addAttribute("categories", webDAO.getDishCategories());
            if (success != null) model.addAttribute("successMessage", success);
            if (error != null) model.addAttribute("errorMessage", error);
            if (editId != null) {
                searchDishController.getDishDetail(editId).ifPresent(dish -> model.addAttribute("editDish", dish));
            }
        } catch (SQLException exception) {
            model.addAttribute("errorMessage", databaseError(exception));
            model.addAttribute("dishes", List.of());
            model.addAttribute("categories", List.of());
        }
        return "manage-dish";
    }

    @PostMapping("/dishes/save")
    public String saveDish(HttpSession session, @RequestParam(defaultValue = "0") int id,
                           @RequestParam String dishName, @RequestParam BigDecimal price,
                           @RequestParam(defaultValue = "") String description,
                           @RequestParam String category, @RequestParam(defaultValue = "") String imageUrl,
                           @RequestParam(defaultValue = "false") boolean available,
                           RedirectAttributes redirect) {
        if (requireRole(session, "Manager") == null) return "redirect:/dashboard";
        try {
            if (dishName.isBlank() || category.isBlank() || price.signum() < 0) {
                throw new IllegalArgumentException("Vui lòng nhập tên, thể loại hợp lệ và giá không âm.");
            }
            webDAO.saveDish(new Dish3(id, dishName, price, description, category, imageUrl, available));
            redirect.addAttribute("success", id > 0 ? "Cập nhật món ăn thành công!" : "Thêm món ăn thành công!");
        } catch (SQLException | IllegalArgumentException exception) {
            redirect.addAttribute("error", exception instanceof SQLException ? databaseError((SQLException) exception) : exception.getMessage());
        }
        return "redirect:/dishes";
    }

    @PostMapping("/dishes/delete/{id}")
    public String deleteDish(HttpSession session, @PathVariable int id, RedirectAttributes redirect) {
        if (requireRole(session, "Manager") == null) return "redirect:/dashboard";
        try {
            if (webDAO.deleteDish(id)) redirect.addAttribute("success", "Xóa món ăn thành công!");
            else redirect.addAttribute("error", "Không tìm thấy món ăn cần xóa.");
        } catch (SQLException exception) {
            redirect.addAttribute("error", databaseError(exception));
        }
        return "redirect:/dishes";
    }

    @GetMapping("/statistics")
    public String statistics(HttpSession session,
                             @RequestParam(value = "status", required = false) String status,
                             Model model) {
        if (requireRole(session, "Manager") == null) return "redirect:/dashboard";
        String orderStatus = status != null && List.of("ALL", "PAID", "PENDING").contains(status)
                ? status : "ALL";
        model.addAttribute("loggedUser", getLoggedUser(session));
        model.addAttribute("orderStatus", orderStatus);
        model.addAttribute("statistics", List.of());
        model.addAttribute("orderSummary", Map.of(
                "totalOrders", 0, "totalRevenue", BigDecimal.ZERO, "paidOrders", 0, "pendingOrders", 0));
        model.addAttribute("orderStatistics", List.of());
        model.addAttribute("customers", List.of());
        model.addAttribute("suppliers", List.of());
        model.addAttribute("ingredients", List.of());
        try {
            model.addAttribute("statistics", webDAO.getStatistics());
            model.addAttribute("orderSummary", webDAO.getOrderStatisticsSummary());
            model.addAttribute("orderStatistics", webDAO.getOrderStatistics(orderStatus));
            model.addAttribute("customers", webDAO.getCustomers());
            model.addAttribute("suppliers", webDAO.getSuppliers());
            model.addAttribute("ingredients", webDAO.getIngredients());
        } catch (SQLException exception) {
            model.addAttribute("errorMessage", databaseError(exception));
        }
        return "statistics";
    }

    @PostMapping("/reservation")
    public String reserve(HttpSession session, @RequestParam int tableId,
                          @RequestParam String reservationTime, @RequestParam int guestCount,
                          RedirectAttributes redirect) {
        Map<String, Object> user = requireRole(session, "Customer");
        if (user == null) return "redirect:/dashboard";
        try {
            LocalDateTime time = LocalDateTime.parse(reservationTime);
            if (time.isBefore(LocalDateTime.now()) || guestCount < 1) throw new IllegalArgumentException("Thời gian hoặc số khách không hợp lệ.");
            webDAO.createReservation(webDAO.getCustomerId((Integer) user.get("userId")), tableId, time, guestCount);
            redirect.addAttribute("success", "Đặt bàn thành công! Đơn đặt bàn đang chờ xác nhận.");
        } catch (SQLException | IllegalArgumentException exception) {
            redirect.addAttribute("error", exception instanceof SQLException ? databaseError((SQLException) exception) : exception.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/order/online/cart/add")
    public String addOnlineOrderItem(HttpSession session, @RequestParam int dishId, @RequestParam int quantity,
                                     @RequestParam(defaultValue = "") String keyword,
                                     @RequestParam(defaultValue = "") String category,
                                     RedirectAttributes redirect) {
        if (requireRole(session, "Customer") == null) return "redirect:/dashboard";
        try {
            addCartItem(session, "onlineOrderCart", dishId, quantity);
            redirect.addFlashAttribute("successMessage", "Đã thêm món vào giỏ hàng.");
        } catch (SQLException | IllegalArgumentException exception) {
            redirect.addFlashAttribute("errorMessage", orderError(exception));
        }
        return cartRedirect(redirect, keyword, category);
    }

    @PostMapping("/order/online/cart/update")
    public String updateOnlineOrderItem(HttpSession session, @RequestParam int dishId, @RequestParam int quantity,
                                        @RequestParam(defaultValue = "") String keyword,
                                        @RequestParam(defaultValue = "") String category,
                                        RedirectAttributes redirect) {
        if (requireRole(session, "Customer") == null) return "redirect:/dashboard";
        try {
            updateCartItem(session, "onlineOrderCart", dishId, quantity);
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("errorMessage", orderError(exception));
        }
        return cartRedirect(redirect, keyword, category);
    }

    @PostMapping("/order/online/cart/remove")
    public String removeOnlineOrderItem(HttpSession session, @RequestParam int dishId,
                                        @RequestParam(defaultValue = "") String keyword,
                                        @RequestParam(defaultValue = "") String category,
                                        RedirectAttributes redirect) {
        if (requireRole(session, "Customer") == null) return "redirect:/dashboard";
        removeCartItem(session, "onlineOrderCart", dishId);
        return cartRedirect(redirect, keyword, category);
    }

    @PostMapping("/order/online/submit")
    public String submitOnlineOrder(HttpSession session, @RequestParam(defaultValue = "") String keyword,
                                    @RequestParam(defaultValue = "") String category,
                                    @RequestParam(name = "cartDishIds", required = false) List<Integer> cartDishIds,
                                    @RequestParam(name = "cartQuantities", required = false) List<Integer> cartQuantities,
                                    RedirectAttributes redirect) {
        Map<String, Object> user = requireRole(session, "Customer");
        if (user == null) return "redirect:/dashboard";
        try {
            syncCartFromRequest(session, "onlineOrderCart", cartDishIds, cartQuantities);
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("errorMessage", exception.getMessage());
            return cartRedirect(redirect, keyword, category);
        }
        Map<Integer, Integer> cart = cartFromSession(session, "onlineOrderCart");
        if (cart.isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất 1 món ăn");
            return cartRedirect(redirect, keyword, category);
        }
        try {
            int customerId = webDAO.getCustomerId((Integer) user.get("userId"));
            webDAO.createFoodOrder(customerId, null, null, cart, null, "PENDING");
            session.removeAttribute("onlineOrderCart");
            redirect.addFlashAttribute("successMessage",
                    "Đặt món trực tuyến thành công! Đang chờ nhân viên xác nhận");
        } catch (SQLException | IllegalArgumentException exception) {
            redirect.addFlashAttribute("errorMessage", orderError(exception));
        }
        return cartRedirect(redirect, keyword, category);
    }

    @PostMapping("/sales/confirm")
    public String confirmOrder(HttpSession session, @RequestParam String type, @RequestParam int id,
                               RedirectAttributes redirect) {
        if (requireRole(session, "SalesStaff") == null) return "redirect:/dashboard";
        String table = "reservation".equals(type) ? "tblReservation3" : "order".equals(type) ? "tblFoodOrder3" : null;
        if (table == null) {
            redirect.addAttribute("error", "Loại đơn cần xác nhận không hợp lệ.");
        } else {
            try {
                if (webDAO.updateStatus(table, id, "PENDING", "CONFIRMED")) {
                    redirect.addAttribute("success", "Đã xác nhận đơn thành công.");
                } else {
                    redirect.addAttribute("error", "Đơn không còn ở trạng thái chờ xác nhận.");
                }
            } catch (SQLException exception) {
                redirect.addAttribute("error", databaseError(exception));
            }
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/sales/table-order/cart/add")
    public String addTableOrderItem(HttpSession session, @RequestParam int dishId, @RequestParam int quantity,
                                    @RequestParam(defaultValue = "") String keyword,
                                    @RequestParam(defaultValue = "") String category,
                                    RedirectAttributes redirect) {
        if (requireRole(session, "SalesStaff") == null) return "redirect:/dashboard";
        try {
            addCartItem(session, "tableOrderCart", dishId, quantity);
            redirect.addFlashAttribute("successMessage", "Đã thêm món vào phiếu gọi món.");
        } catch (SQLException | IllegalArgumentException exception) {
            redirect.addFlashAttribute("errorMessage", orderError(exception));
        }
        return cartRedirect(redirect, keyword, category);
    }

    @PostMapping("/sales/table-order/cart/update")
    public String updateTableOrderItem(HttpSession session, @RequestParam int dishId, @RequestParam int quantity,
                                       @RequestParam(defaultValue = "") String keyword,
                                       @RequestParam(defaultValue = "") String category,
                                       RedirectAttributes redirect) {
        if (requireRole(session, "SalesStaff") == null) return "redirect:/dashboard";
        try {
            updateCartItem(session, "tableOrderCart", dishId, quantity);
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("errorMessage", orderError(exception));
        }
        return cartRedirect(redirect, keyword, category);
    }

    @PostMapping("/sales/table-order/cart/remove")
    public String removeTableOrderItem(HttpSession session, @RequestParam int dishId,
                                       @RequestParam(defaultValue = "") String keyword,
                                       @RequestParam(defaultValue = "") String category,
                                       RedirectAttributes redirect) {
        if (requireRole(session, "SalesStaff") == null) return "redirect:/dashboard";
        removeCartItem(session, "tableOrderCart", dishId);
        return cartRedirect(redirect, keyword, category);
    }

    @PostMapping("/sales/table-order/submit")
    public String submitTableOrder(HttpSession session,
                                   @RequestParam(name = "tableId", required = false) Integer tableId,
                                   @RequestParam(defaultValue = "") String notes,
                                   @RequestParam(defaultValue = "") String keyword,
                                   @RequestParam(defaultValue = "") String category,
                                   @RequestParam(name = "cartDishIds", required = false) List<Integer> cartDishIds,
                                   @RequestParam(name = "cartQuantities", required = false) List<Integer> cartQuantities,
                                   RedirectAttributes redirect) {
        Map<String, Object> user = requireRole(session, "SalesStaff");
        if (user == null) return "redirect:/dashboard";
        try {
            syncCartFromRequest(session, "tableOrderCart", cartDishIds, cartQuantities);
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("errorMessage", exception.getMessage());
            return cartRedirect(redirect, keyword, category);
        }
        Map<Integer, Integer> cart = cartFromSession(session, "tableOrderCart");
        if (cart.isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất 1 món ăn");
            return cartRedirect(redirect, keyword, category);
        }
        try {
            int salesStaffId = webDAO.getSalesStaffId((Integer) user.get("userId"));
            webDAO.createFoodOrder(null, salesStaffId, tableId, cart, notes, "CONFIRMED");
            session.removeAttribute("tableOrderCart");
            redirect.addFlashAttribute("successMessage", "Tạo đơn gọi món tại bàn thành công!");
        } catch (SQLException | IllegalArgumentException exception) {
            redirect.addFlashAttribute("errorMessage", orderError(exception));
        }
        return cartRedirect(redirect, keyword, category);
    }

    @PostMapping("/sales/payment")
    public String payment(HttpSession session, @RequestParam int orderId,
                          @RequestParam String paymentMethod,
                          @RequestParam(defaultValue = "") String customerName,
                          @RequestParam(defaultValue = "") String membershipLookup,
                          RedirectAttributes redirect) {
        Map<String, Object> user = requireRole(session, "SalesStaff");
        if (user == null) return "redirect:/dashboard";
        try {
            int salesStaffId = webDAO.getSalesStaffId((Integer) user.get("userId"));
            Map<String, Object> paymentResult = webDAO.createBill(
                    orderId, salesStaffId, paymentMethod, customerName, membershipLookup);
            BigDecimal discountAmount = (BigDecimal) paymentResult.get("discountAmount");
            BigDecimal finalAmount = (BigDecimal) paymentResult.get("totalAmount");
            int pointsEarned = (Integer) paymentResult.get("pointsEarned");
            redirect.addAttribute("success", "Thanh toán và lưu hóa đơn thành công! Giảm "
                    + discountAmount.stripTrailingZeros().toPlainString() + " VND, thực thu "
                    + finalAmount.stripTrailingZeros().toPlainString() + " VND, cộng "
                    + pointsEarned + " điểm.");
        } catch (IllegalArgumentException exception) {
            redirect.addAttribute("error", exception.getMessage());
        } catch (SQLException exception) {
            redirect.addAttribute("error", databaseError(exception));
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/sales/membership")
    public String membership(HttpSession session, @RequestParam String fullName, @RequestParam String phone,
                            @RequestParam(defaultValue = "") String email, @RequestParam String cardTier,
                            RedirectAttributes redirect) {
        if (requireRole(session, "SalesStaff") == null) return "redirect:/dashboard";
        try {
            Map<String, Object> card = webDAO.createMembershipCard(fullName, phone, email, cardTier);
            redirect.addAttribute("success", "Tạo thẻ thành viên thành công cho khách hàng "
                    + card.get("customerName") + "!");
        } catch (SQLException | IllegalArgumentException exception) {
            redirect.addAttribute("error", exception instanceof SQLException ? databaseError((SQLException) exception) : exception.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/inventory/supplier")
    public String saveSupplier(HttpSession session, @RequestParam(required = false) Integer id,
                              @RequestParam String supplierName, @RequestParam(defaultValue = "") String phone,
                              @RequestParam(defaultValue = "") String email, @RequestParam(defaultValue = "") String address,
                              RedirectAttributes redirect) {
        if (requireRole(session, "InventoryStaff") == null) return "redirect:/dashboard";
        try {
            if (supplierName.isBlank()) throw new IllegalArgumentException("Tên nhà cung cấp không được để trống.");
            webDAO.saveSupplier(id, supplierName, phone, email, address);
            redirect.addAttribute("success", id == null ? "Thêm nhà cung cấp thành công!" : "Cập nhật nhà cung cấp thành công!");
        } catch (SQLException | IllegalArgumentException exception) {
            redirect.addAttribute("error", exception instanceof SQLException ? databaseError((SQLException) exception) : exception.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/inventory/restock")
    public String restock(HttpSession session, @RequestParam int ingredientId, @RequestParam BigDecimal quantity,
                          RedirectAttributes redirect) {
        if (requireRole(session, "InventoryStaff") == null) return "redirect:/dashboard";
        try {
            if (quantity.signum() <= 0) throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0.");
            webDAO.restockIngredient(ingredientId, quantity);
            redirect.addAttribute("success", "Nhập kho thành công, tồn kho đã được cộng dồn.");
        } catch (SQLException | IllegalArgumentException exception) {
            redirect.addAttribute("error", exception instanceof SQLException ? databaseError((SQLException) exception) : exception.getMessage());
        }
        return "redirect:/dashboard";
    }

    private List<Dish3> selectedDishes(HttpSession session) {
        Object value = session.getAttribute("selectedDishes");
        if (value instanceof List<?> list) {
            List<Dish3> dishes = new ArrayList<>();
            for (Object item : list) if (item instanceof Dish3 dish) dishes.add(dish);
            return dishes;
        }
        return new ArrayList<>();
    }

    private Map<Integer, Integer> cartFromSession(HttpSession session, String attribute) {
        Object value = session.getAttribute(attribute);
        Map<Integer, Integer> cart = new LinkedHashMap<>();
        if (value instanceof Map<?, ?> storedCart) {
            for (Map.Entry<?, ?> entry : storedCart.entrySet()) {
                if (entry.getKey() instanceof Integer dishId && entry.getValue() instanceof Integer quantity
                        && quantity > 0) {
                    cart.put(dishId, quantity);
                }
            }
        }
        return cart;
    }

    private void addCartItem(HttpSession session, String attribute, int dishId, int quantity) throws SQLException {
        if (quantity < 1) throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        List<Map<String, Object>> dish = webDAO.getCartItems(Map.of(dishId, 1));
        if (!Boolean.TRUE.equals(dish.get(0).get("available"))) {
            throw new IllegalArgumentException("Món ăn hiện không còn phục vụ.");
        }
        Map<Integer, Integer> cart = cartFromSession(session, attribute);
        int existingQuantity = cart.getOrDefault(dishId, 0);
        if (existingQuantity > Integer.MAX_VALUE - quantity) {
            throw new IllegalArgumentException("Số lượng vượt quá giới hạn.");
        }
        cart.put(dishId, existingQuantity + quantity);
        session.setAttribute(attribute, cart);
    }

    private void updateCartItem(HttpSession session, String attribute, int dishId, int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        Map<Integer, Integer> cart = cartFromSession(session, attribute);
        if (!cart.containsKey(dishId)) throw new IllegalArgumentException("Món ăn không còn trong giỏ hàng.");
        cart.put(dishId, quantity);
        session.setAttribute(attribute, cart);
    }

    private void syncCartFromRequest(HttpSession session, String attribute, List<Integer> dishIds,
                                     List<Integer> quantities) {
        if (dishIds == null && quantities == null) return;
        if (dishIds == null || quantities == null || dishIds.size() != quantities.size()) {
            throw new IllegalArgumentException("Không thể cập nhật số lượng giỏ hàng.");
        }
        Map<Integer, Integer> currentCart = cartFromSession(session, attribute);
        Map<Integer, Integer> updatedCart = new LinkedHashMap<>();
        for (int i = 0; i < dishIds.size(); i++) {
            Integer dishId = dishIds.get(i);
            Integer quantity = quantities.get(i);
            if (dishId == null || quantity == null || quantity < 1 || !currentCart.containsKey(dishId)) {
                throw new IllegalArgumentException("Số lượng giỏ hàng không hợp lệ.");
            }
            updatedCart.put(dishId, quantity);
        }
        if (updatedCart.size() != currentCart.size()) {
            throw new IllegalArgumentException("Không thể cập nhật số lượng giỏ hàng.");
        }
        session.setAttribute(attribute, updatedCart);
    }

    private void removeCartItem(HttpSession session, String attribute, int dishId) {
        Map<Integer, Integer> cart = cartFromSession(session, attribute);
        cart.remove(dishId);
        if (cart.isEmpty()) session.removeAttribute(attribute);
        else session.setAttribute(attribute, cart);
    }

    private BigDecimal cartTotal(List<Map<String, Object>> cartItems) {
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> item : cartItems) {
            total = total.add((BigDecimal) item.get("lineTotal"));
        }
        return total;
    }

    private String cartRedirect(RedirectAttributes redirect, String keyword, String category) {
        redirect.addAttribute("keyword", keyword);
        redirect.addAttribute("category", category);
        return "redirect:/dashboard";
    }

    private String orderError(Exception exception) {
        return exception instanceof SQLException sqlException
                ? databaseError(sqlException)
                : exception.getMessage();
    }

    private Map<String, Object> requireRole(HttpSession session, String role) {
        Map<String, Object> user = getLoggedUser(session);
        return user != null && role.equals(user.get("role")) ? user : null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getLoggedUser(HttpSession session) {
        Object value = session.getAttribute("loggedInUser");
        if (value instanceof Map<?, ?> map) return (Map<String, Object>) map;
        return null;
    }

    private String databaseError(SQLException exception) {
        return "Không thể truy cập cơ sở dữ liệu: " + exception.getMessage();
    }
}
