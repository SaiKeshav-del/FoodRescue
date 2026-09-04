import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class FoodRescueServer {

    private static final int PORT = 8080;

    // In-memory Thread-safe Data Store
    public static class FoodItem {
        public String id;
        public String title;
        public String restaurant;
        public String category; // Bakery, Meals, Groceries, Desserts, Dairy, NGO Bulk
        public String dietary;  // Veg, Non-Veg, Vegan, Jain
        public String city;
        public String location;
        public String distance;
        public double rating;
        public int origPrice;
        public int rescuePrice;
        public int qty;
        public String pickupStart;
        public String pickupEnd;
        public String image;
        public String description;
        public int mapX;
        public int mapY;

        public FoodItem(String id, String title, String restaurant, String category, String dietary,
                        String city, String location, String distance, double rating,
                        int origPrice, int rescuePrice, int qty, String pickupStart, String pickupEnd,
                        String image, String description, int mapX, int mapY) {
            this.id = id;
            this.title = title;
            this.restaurant = restaurant;
            this.category = category;
            this.dietary = dietary;
            this.city = city;
            this.location = location;
            this.distance = distance;
            this.rating = rating;
            this.origPrice = origPrice;
            this.rescuePrice = rescuePrice;
            this.qty = qty;
            this.pickupStart = pickupStart;
            this.pickupEnd = pickupEnd;
            this.image = image;
            this.description = description;
            this.mapX = mapX;
            this.mapY = mapY;
        }

        public String toJson() {
            return String.format(Locale.US,
                "{\"id\":\"%s\",\"title\":\"%s\",\"restaurant\":\"%s\",\"category\":\"%s\",\"dietary\":\"%s\",\"city\":\"%s\",\"location\":\"%s\",\"distance\":\"%s\",\"rating\":%.1f,\"origPrice\":%d,\"rescuePrice\":%d,\"qty\":%d,\"pickupStart\":\"%s\",\"pickupEnd\":\"%s\",\"image\":\"%s\",\"description\":\"%s\",\"mapCoords\":{\"x\":%d,\"y\":%d}}",
                escape(id), escape(title), escape(restaurant), escape(category), escape(dietary),
                escape(city), escape(location), escape(distance), rating, origPrice, rescuePrice,
                qty, escape(pickupStart), escape(pickupEnd), escape(image), escape(description), mapX, mapY
            );
        }
    }

    public static class Reservation {
        public String id;
        public String itemId;
        public String itemTitle;
        public String restaurant;
        public int qty;
        public int totalPrice;
        public String pickupWindow;
        public String status; // Active, Completed

        public Reservation(String id, String itemId, String itemTitle, String restaurant, int qty, int totalPrice, String pickupWindow, String status) {
            this.id = id;
            this.itemId = itemId;
            this.itemTitle = itemTitle;
            this.restaurant = restaurant;
            this.qty = qty;
            this.totalPrice = totalPrice;
            this.pickupWindow = pickupWindow;
            this.status = status;
        }

        public String toJson() {
            return String.format(Locale.US,
                "{\"id\":\"%s\",\"itemId\":\"%s\",\"itemTitle\":\"%s\",\"restaurant\":\"%s\",\"qty\":%d,\"totalPrice\":%d,\"pickupWindow\":\"%s\",\"status\":\"%s\"}",
                escape(id), escape(itemId), escape(itemTitle), escape(restaurant), qty, totalPrice, escape(pickupWindow), escape(status)
            );
        }
    }

    private static final List<FoodItem> foodItems = new CopyOnWriteArrayList<>();
    private static final Map<String, Reservation> reservations = new ConcurrentHashMap<>();
    private static final AtomicLong mealsCounter = new AtomicLong(14850);
    private static final AtomicLong moneySavedCounter = new AtomicLong(3142000);

    public static void main(String[] args) throws IOException {
        initSampleData();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new FrontendHandler());
        server.createContext("/api/food", new FoodApiHandler());
        server.createContext("/api/reservations", new ReservationApiHandler());
        server.createContext("/api/verify", new VerifyApiHandler());
        server.createContext("/api/stats", new StatsApiHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("================================================================");
        System.out.println("🌱 FoodRescue Single-Page Java Server running!");
        System.out.println("👉 Access the Web App at: http://localhost:" + PORT);
        System.out.println("👉 REST API endpoints:    http://localhost:" + PORT + "/api/food");
        System.out.println("================================================================");
    }

    private static void initSampleData() {
        foodItems.add(new FoodItem(
            "item-1", "Handi Dum Paneer Biryani Surplus Bag", "Royal Biryani House", "Meals", "Veg",
            "Bengaluru", "12th Main, Indiranagar, Bengaluru", "1.8 km", 4.9, 320, 99, 4,
            "8:30 PM", "10:00 PM",
            "https://images.unsplash.com/photo-1589302168068-964664d93dc0?auto=format&fit=crop&w=800&q=80",
            "Slow-cooked handi biryani packed with fresh cottage cheese cubes, saffron basmati rice, burani raita and gulab jamun.",
            32, 45
        ));

        foodItems.add(new FoodItem(
            "item-2", "Artisanal Sourdough & Croissant Box", "The French Crust Bakery", "Bakery", "Veg",
            "Bengaluru", "100ft Road, Indiranagar, Bengaluru", "2.4 km", 4.95, 450, 120, 5,
            "8:00 PM", "9:30 PM",
            "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=800&q=80",
            "1 full organic country sourdough loaf + 2 butter croissants baked this morning. 100% sealed & fresh.",
            48, 38
        ));

        foodItems.add(new FoodItem(
            "item-3", "Deluxe North Indian Thali Set", "Punjab Grill Express", "Meals", "Non-Veg",
            "Bengaluru", "Koramangala 5th Block, Bengaluru", "3.6 km", 4.8, 450, 140, 3,
            "9:00 PM", "10:30 PM",
            "https://images.unsplash.com/photo-1546833999-b9f581a1996d?auto=format&fit=crop&w=800&q=80",
            "Butter chicken or paneer makhani, dal makhani, 3 tandoori rotis, jeera rice, salad and gulab jamun.",
            62, 55
        ));

        foodItems.add(new FoodItem(
            "item-4", "Gourmet Pastry & Cupcake Assortment", "Sweet Treats Bakehouse", "Desserts", "Veg",
            "Bengaluru", "Lavelle Road, Bengaluru", "4.2 km", 4.9, 390, 110, 2,
            "7:30 PM", "9:00 PM",
            "https://images.unsplash.com/photo-1578985545062-69928b1d9587?auto=format&fit=crop&w=800&q=80",
            "Box of 4 pastries: Belgian chocolate ganache, red velvet, lemon tart, and blueberry mousse.",
            40, 70
        ));

        foodItems.add(new FoodItem(
            "item-5", "Organic Farm Fresh Veggie Crate (5kg)", "GreenRoots Supermarket", "Groceries", "Vegan",
            "Bengaluru", "HSR Layout Sector 3, Bengaluru", "5.1 km", 4.7, 280, 80, 6,
            "7:00 PM", "9:30 PM",
            "https://images.unsplash.com/photo-1610832958506-aa56368176cf?auto=format&fit=crop&w=800&q=80",
            "Hydroponic spinach, bell peppers, tomatoes, cucumbers, and carrots harvested today.",
            75, 65
        ));

        foodItems.add(new FoodItem(
            "item-6", "100-Portion Banquet Rice & Curry (Free for NGO)", "Grand Palace Banquets", "NGO Bulk", "Veg",
            "Bengaluru", "Whitefield Main Road, Bengaluru", "8.5 km", 5.0, 6500, 0, 1,
            "9:30 PM", "11:00 PM",
            "https://images.unsplash.com/photo-1541832676-9b763b0239ab?auto=format&fit=crop&w=800&q=80",
            "Hot banquet surplus in large stainless steel catering containers. Free for verified NGOs.",
            85, 30
        ));

        reservations.put("FR-BLR-89241", new Reservation(
            "FR-BLR-89241", "item-2", "Artisanal Sourdough & Croissant Box",
            "The French Crust Bakery", 1, 120, "8:00 PM - 9:30 PM", "Active"
        ));
    }

    // --- HTTP HANDLERS ---

    static class FrontendHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] response = getEmbeddedHtml().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }

    static class FoodApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            String method = exchange.getRequestMethod();

            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < foodItems.size(); i++) {
                    json.append(foodItems.get(i).toJson());
                    if (i < foodItems.size() - 1) json.append(",");
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readBody(exchange);
                String id = "item-" + System.currentTimeMillis();
                String title = extractJsonField(body, "title", "Surplus Surprise Bag");
                String restaurant = extractJsonField(body, "restaurant", "Partner Restaurant");
                String category = extractJsonField(body, "category", "Meals");
                String dietary = extractJsonField(body, "dietary", "Veg");
                String city = extractJsonField(body, "city", "Bengaluru");
                String location = extractJsonField(body, "location", "Indiranagar, Bengaluru");
                int origPrice = extractJsonInt(body, "origPrice", 300);
                int rescuePrice = extractJsonInt(body, "rescuePrice", 99);
                int qty = extractJsonInt(body, "qty", 3);
                String pickupStart = extractJsonField(body, "pickupStart", "8:00 PM");
                String pickupEnd = extractJsonField(body, "pickupEnd", "10:00 PM");
                String image = extractJsonField(body, "image", "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=800&q=80");
                String description = extractJsonField(body, "description", "Fresh surplus listed by restaurant.");

                FoodItem newItem = new FoodItem(id, title, restaurant, category, dietary, city, location, "1.2 km", 5.0,
                    origPrice, rescuePrice, qty, pickupStart, pickupEnd, image, description,
                    20 + new Random().nextInt(60), 20 + new Random().nextInt(60)
                );
                foodItems.add(0, newItem);
                sendJsonResponse(exchange, 201, newItem.toJson());
            }
        }
    }

    static class ReservationApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            String method = exchange.getRequestMethod();

            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                StringBuilder json = new StringBuilder("[");
                int count = 0;
                for (Reservation r : reservations.values()) {
                    if (count > 0) json.append(",");
                    json.append(r.toJson());
                    count++;
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readBody(exchange);
                String itemId = extractJsonField(body, "itemId", "item-1");
                int qty = extractJsonInt(body, "qty", 1);

                FoodItem matched = null;
                for (FoodItem fi : foodItems) {
                    if (fi.id.equals(itemId)) {
                        matched = fi;
                        break;
                    }
                }

                if (matched == null || matched.qty < qty) {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Item sold out or insufficient stock\"}");
                    return;
                }

                matched.qty -= qty;
                mealsCounter.addAndGet(qty);
                moneySavedCounter.addAndGet((long) (matched.origPrice - matched.rescuePrice) * qty);

                String resId = "FR-BLR-" + (10000 + new Random().nextInt(90000));
                Reservation res = new Reservation(resId, matched.id, matched.title, matched.restaurant,
                    qty, matched.rescuePrice * qty, matched.pickupStart + " - " + matched.pickupEnd, "Active"
                );
                reservations.put(resId, res);

                sendJsonResponse(exchange, 201, res.toJson());
            }
        }
    }

    static class VerifyApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readBody(exchange);
                String code = extractJsonField(body, "code", "").toUpperCase();
                Reservation res = reservations.get(code);

                if (res != null) {
                    res.status = "Completed";
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Pickup verified successfully!\",\"order\":" + res.toJson() + "}");
                } else {
                    sendJsonResponse(exchange, 404, "{\"success\":false,\"message\":\"Reservation ID not found\"}");
                }
            }
        }
    }

    static class StatsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            String json = String.format(Locale.US,
                "{\"mealsRescued\":%d,\"moneySavedINR\":%d,\"co2PreventedKg\":%.1f}",
                mealsCounter.get(), moneySavedCounter.get(), (mealsCounter.get() * 1.8)
            );
            sendJsonResponse(exchange, 200, json);
        }
    }

    // --- UTILITIES ---

    private static void setCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJsonResponse(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private static String extractJsonField(String json, String field, String defaultVal) {
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return defaultVal;
        int colon = json.indexOf(":", idx + key.length());
        if (colon == -1) return defaultVal;
        int startQuote = json.indexOf("\"", colon + 1);
        if (startQuote == -1) return defaultVal;
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote == -1) return defaultVal;
        return json.substring(startQuote + 1, endQuote);
    }

    private static int extractJsonInt(String json, String field, int defaultVal) {
        try {
            String key = "\"" + field + "\"";
            int idx = json.indexOf(key);
            if (idx == -1) return defaultVal;
            int colon = json.indexOf(":", idx + key.length());
            if (colon == -1) return defaultVal;
            int end = json.indexOf(",", colon + 1);
            if (end == -1) end = json.indexOf("}", colon + 1);
            if (end == -1) return defaultVal;
            String valStr = json.substring(colon + 1, end).replaceAll("[^0-9]", "").trim();
            return Integer.parseInt(valStr);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    // --- EMBEDDED SINGLE PAGE UI ---
    private static String getEmbeddedHtml() {
        return "<!DOCTYPE html>\n" +
"<html lang=\"en\">\n" +
"<head>\n" +
"  <meta charset=\"UTF-8\" />\n" +
"  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
"  <title>FoodRescue | Rescue Food. Reduce Waste. Feed Communities.</title>\n" +
"  <script src=\"https://cdn.tailwindcss.com\"></script>\n" +
"  <link href=\"https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap\" rel=\"stylesheet\">\n" +
"  <script src=\"https://unpkg.com/lucide@latest\"></script>\n" +
"  <script src=\"https://cdn.jsdelivr.net/npm/canvas-confetti@1.9.3/dist/confetti.browser.min.js\"></script>\n" +
"  <script>\n" +
"    tailwind.config = {\n" +
"      theme: {\n" +
"        extend: {\n" +
"          fontFamily: { sans: ['\"Plus Jakarta Sans\"', 'sans-serif'] },\n" +
"          colors: {\n" +
"            brand: { 50: '#ecfdf5', 100: '#d1fae5', 500: '#10b981', 600: '#059669', 700: '#047857', 800: '#065f46', 900: '#064e3b' },\n" +
"            cream: { 50: '#fdfbf7', 100: '#f8f5ee', 200: '#f0ebd8' },\n" +
"            accent: { orange: '#f97316', amber: '#f59e0b', red: '#ef4444' }\n" +
"          }\n" +
"        }\n" +
"      }\n" +
"    }\n" +
"  </script>\n" +
"  <style>\n" +
"    body { background-color: #FDFBF7; color: #1c1917; scroll-behavior: smooth; }\n" +
"    .glass-nav { background: rgba(253, 251, 247, 0.88); backdrop-filter: blur(12px); }\n" +
"    .map-pin { transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); }\n" +
"    .map-pin:hover { transform: translateY(-6px) scale(1.15); z-index: 30; }\n" +
"  </style>\n" +
"</head>\n" +
"<body class=\"min-h-screen flex flex-col antialiased selection:bg-brand-500 selection:text-white\">\n" +
"\n" +
"  <!-- TOP TICKER -->\n" +
"  <div class=\"bg-brand-900 text-white text-xs py-2 px-4 text-center font-medium flex items-center justify-center space-x-2\">\n" +
"    <span class=\"px-2 py-0.5 rounded-full text-[10px] font-bold bg-accent-orange text-white\">LIVE</span>\n" +
"    <span>⚡ Over <strong id=\"ticker-meals\">14,850+ meals</strong> rescued across Bengaluru, Mumbai & Delhi!</span>\n" +
"    <span class=\"hidden md:inline text-brand-200\">| Save up to 75% on fresh surplus food.</span>\n" +
"  </div>\n" +
"\n" +
"  <!-- HEADER -->\n" +
"  <header class=\"sticky top-0 z-40 glass-nav border-b border-stone-200\">\n" +
"    <div class=\"max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between\">\n" +
"      <a href=\"#\" onclick=\"navigateTo('landing')\" class=\"flex items-center gap-3\">\n" +
"        <div class=\"w-11 h-11 rounded-2xl bg-brand-600 flex items-center justify-center text-white shadow-md shadow-brand-600/30\">\n" +
"          <i data-lucide=\"leaf\" class=\"w-6 h-6\"></i>\n" +
"        </div>\n" +
"        <div>\n" +
"          <div class=\"flex items-center gap-1\">\n" +
"            <span class=\"text-2xl font-extrabold text-stone-900\">Food<span class=\"text-brand-600\">Rescue</span></span>\n" +
"            <span class=\"text-[10px] uppercase font-bold bg-brand-100 text-brand-800 px-1.5 py-0.5 rounded\">JAVA</span>\n" +
"          </div>\n" +
"          <p class=\"text-[11px] text-stone-500 font-medium\">Rescue Food • Reduce Waste</p>\n" +
"        </div>\n" +
"      </a>\n" +
"\n" +
"      <nav class=\"hidden md:flex items-center space-x-2\">\n" +
"        <button onclick=\"navigateTo('landing')\" id=\"nav-landing\" class=\"nav-item px-3.5 py-2 rounded-xl text-sm font-bold text-brand-700 bg-brand-50\">Home</button>\n" +
"        <button onclick=\"navigateTo('browse')\" id=\"nav-browse\" class=\"nav-item px-3.5 py-2 rounded-xl text-sm font-medium text-stone-600 hover:bg-stone-100\">Browse Food</button>\n" +
"      </nav>\n" +
"\n" +
"      <div class=\"flex items-center gap-3\">\n" +
"        <div class=\"hidden sm:flex bg-stone-100 p-1 rounded-xl border border-stone-200 text-xs font-semibold\">\n" +
"          <span class=\"text-stone-400 px-2 flex items-center gap-1\">Role:</span>\n" +
"          <button onclick=\"switchRole('customer')\" id=\"role-btn-customer\" class=\"px-2.5 py-1 rounded-lg bg-white text-brand-700 font-bold shadow-sm\">Citizen</button>\n" +
"          <button onclick=\"switchRole('business')\" id=\"role-btn-business\" class=\"px-2.5 py-1 rounded-lg text-stone-600\">Partner</button>\n" +
"          <button onclick=\"switchRole('ngo')\" id=\"role-btn-ngo\" class=\"px-2.5 py-1 rounded-lg text-stone-600\">NGO</button>\n" +
"          <button onclick=\"switchRole('admin')\" id=\"role-btn-admin\" class=\"px-2.5 py-1 rounded-lg text-stone-600\">Admin</button>\n" +
"        </div>\n" +
"\n" +
"        <button onclick=\"openCurrentDashboard()\" class=\"bg-brand-600 hover:bg-brand-700 text-white px-4 py-2.5 rounded-xl text-sm font-bold shadow-md flex items-center gap-2\">\n" +
"          <i data-lucide=\"layout-dashboard\" class=\"w-4 h-4\"></i>\n" +
"          <span id=\"dashboard-header-label\">My Rescues</span>\n" +
"        </button>\n" +
"      </div>\n" +
"    </div>\n" +
"  </header>\n" +
"\n" +
"  <!-- MAIN APP CONTAINER -->\n" +
"  <main class=\"flex-grow\">\n" +
"\n" +
"    <!-- PAGE: LANDING -->\n" +
"    <section id=\"page-landing\" class=\"page-view max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 space-y-16\">\n" +
"      <div class=\"grid grid-cols-1 lg:grid-cols-12 gap-12 items-center\">\n" +
"        <div class=\"lg:col-span-7 space-y-6\">\n" +
"          <span class=\"bg-brand-100 border border-brand-200 text-brand-800 text-xs font-bold px-3 py-1.5 rounded-full inline-block\">🌱 India's #1 Hyperlocal Food Rescue Platform</span>\n" +
"          <h1 class=\"text-4xl sm:text-6xl font-extrabold text-stone-900 leading-tight\">\n" +
"            Rescue Food.<br>\n" +
"            <span class=\"text-transparent bg-clip-text bg-gradient-to-r from-brand-600 to-emerald-700\">Save 70%+ Daily.</span><br>\n" +
"            Feed Communities.\n" +
"          </h1>\n" +
"          <p class=\"text-base text-stone-600 max-w-xl\">Buy fresh surprise meals from top restaurants, bakeries, and supermarkets in your neighborhood before closing time.</p>\n" +
"          <div class=\"flex gap-3\">\n" +
"            <button onclick=\"navigateTo('browse')\" class=\"bg-brand-600 hover:bg-brand-700 text-white font-extrabold text-sm px-6 py-3.5 rounded-xl shadow-lg flex items-center gap-2\">\n" +
"              <span>Explore Surplus Bags</span>\n" +
"              <i data-lucide=\"arrow-right\" class=\"w-4 h-4\"></i>\n" +
"            </button>\n" +
"            <button onclick=\"switchRole('business'); navigateTo('restaurant-dashboard')\" class=\"bg-white hover:bg-stone-100 text-stone-800 font-bold text-sm px-5 py-3.5 rounded-xl border border-stone-200\">\n" +
"              For Businesses\n" +
"            </button>\n" +
"          </div>\n" +
"        </div>\n" +
"        <div class=\"lg:col-span-5\">\n" +
"          <div class=\"bg-white rounded-3xl p-5 shadow-2xl border border-stone-200 space-y-3\">\n" +
"            <div class=\"relative h-60 rounded-2xl overflow-hidden\">\n" +
"              <img src=\"https://images.unsplash.com/photo-1589302168068-964664d93dc0?auto=format&fit=crop&w=800&q=80\" class=\"w-full h-full object-cover\" />\n" +
"              <span class=\"absolute top-3 right-3 bg-accent-orange text-white text-xs font-extrabold px-3 py-1 rounded-full\">Save 69% OFF</span>\n" +
"            </div>\n" +
"            <div class=\"flex justify-between items-center\">\n" +
"              <span class=\"text-xs font-bold text-brand-700 bg-brand-50 px-2 py-0.5 rounded\">Royal Biryani House • Indiranagar</span>\n" +
"              <span class=\"text-xs font-bold text-amber-500\">⭐ 4.9</span>\n" +
"            </div>\n" +
"            <h3 class=\"font-extrabold text-stone-900 text-base\">Handi Dum Paneer Biryani Surplus Bag</h3>\n" +
"            <div class=\"flex justify-between items-center pt-2 border-t border-stone-100\">\n" +
"              <div>\n" +
"                <span class=\"text-2xl font-black text-brand-700\">₹99</span>\n" +
"                <span class=\"text-xs text-stone-400 line-through ml-1.5\">₹320</span>\n" +
"              </div>\n" +
"              <button onclick=\"openFoodDetails('item-1')\" class=\"bg-brand-600 hover:bg-brand-700 text-white font-bold text-xs px-4 py-2.5 rounded-xl\">Reserve Now</button>\n" +
"            </div>\n" +
"          </div>\n" +
"        </div>\n" +
"      </div>\n" +
"    </section>\n" +
"\n" +
"    <!-- PAGE: BROWSE -->\n" +
"    <section id=\"page-browse\" class=\"page-view hidden max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6\">\n" +
"      <div class=\"flex flex-col md:flex-row items-center justify-between gap-4 bg-white p-4 rounded-2xl border border-stone-200\">\n" +
"        <div class=\"relative w-full md:w-80\">\n" +
"          <i data-lucide=\"search\" class=\"w-4 h-4 text-stone-400 absolute left-3 top-3\"></i>\n" +
"          <input type=\"text\" id=\"search-input\" oninput=\"renderBrowseGrid()\" placeholder=\"Search biryani, bakery, thali...\" class=\"w-full bg-stone-50 border border-stone-200 rounded-xl pl-9 pr-3 py-2 text-xs focus:outline-none\" />\n" +
"        </div>\n" +
"        <div class=\"flex items-center gap-2 overflow-x-auto w-full pb-1 text-xs font-semibold\">\n" +
"          <button onclick=\"setFilterCategory('All')\" class=\"cat-btn px-3 py-1.5 rounded-xl bg-brand-600 text-white\">All Items</button>\n" +
"          <button onclick=\"setFilterCategory('Bakery')\" class=\"cat-btn px-3 py-1.5 rounded-xl bg-stone-100 text-stone-700\">🥐 Bakery</button>\n" +
"          <button onclick=\"setFilterCategory('Meals')\" class=\"cat-btn px-3 py-1.5 rounded-xl bg-stone-100 text-stone-700\">🍛 Meals</button>\n" +
"          <button onclick=\"setFilterCategory('Desserts')\" class=\"cat-btn px-3 py-1.5 rounded-xl bg-stone-100 text-stone-700\">🍰 Desserts</button>\n" +
"          <button onclick=\"setFilterCategory('NGO Bulk')\" class=\"cat-btn px-3 py-1.5 rounded-xl bg-amber-100 text-amber-900\">🤝 ₹0 NGO Bulk</button>\n" +
"        </div>\n" +
"      </div>\n" +
"\n" +
"      <div id=\"browse-grid\" class=\"grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6\">\n" +
"        <!-- Dynamic Cards -->\n" +
"      </div>\n" +
"    </section>\n" +
"\n" +
"    <!-- PAGE: CUSTOMER DASHBOARD -->\n" +
"    <section id=\"page-customer-dashboard\" class=\"page-view hidden max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6\">\n" +
"      <div class=\"bg-brand-900 rounded-3xl p-6 text-white flex justify-between items-center\">\n" +
"        <div>\n" +
"          <h2 class=\"text-2xl font-extrabold\">Ananya's Rescue Hub 🌱</h2>\n" +
"          <p class=\"text-xs text-brand-200 mt-1\">Level 3 • Green Warrior (850 Eco Points)</p>\n" +
"        </div>\n" +
"        <button onclick=\"navigateTo('browse')\" class=\"bg-white text-brand-900 font-extrabold text-xs px-4 py-2.5 rounded-xl\">+ Rescue Food</button>\n" +
"      </div>\n" +
"      <h3 class=\"text-lg font-bold text-stone-900\">Active Pickup Passes</h3>\n" +
"      <div id=\"cust-passes\" class=\"grid grid-cols-1 md:grid-cols-2 gap-6\"></div>\n" +
"    </section>\n" +
"\n" +
"    <!-- PAGE: RESTAURANT DASHBOARD -->\n" +
"    <section id=\"page-restaurant-dashboard\" class=\"page-view hidden max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6\">\n" +
"      <div class=\"bg-stone-900 rounded-3xl p-6 text-white flex justify-between items-center\">\n" +
"        <div>\n" +
"          <h2 class=\"text-2xl font-extrabold\">The French Crust Bakery & Cafe 👨‍🍳</h2>\n" +
"          <p class=\"text-xs text-stone-400 mt-1\">Partner ID: #FR-BIZ-104 • Indiranagar</p>\n" +
"        </div>\n" +
"        <button onclick=\"openPostModal()\" class=\"bg-brand-600 hover:bg-brand-700 text-white font-bold text-xs px-4 py-2.5 rounded-xl\">+ Post Surplus Food</button>\n" +
"      </div>\n" +
"      <div class=\"bg-white p-5 rounded-3xl border border-stone-200 space-y-3\">\n" +
"        <h4 class=\"font-bold text-sm text-stone-900\">Scan Customer QR / Enter Reservation ID</h4>\n" +
"        <div class=\"flex gap-2\">\n" +
"          <input type=\"text\" id=\"verify-code-input\" placeholder=\"e.g. FR-BLR-89241\" class=\"w-full bg-stone-50 border border-stone-200 rounded-xl p-2.5 text-xs font-mono font-bold uppercase\" />\n" +
"          <button onclick=\"verifyPartnerPickup()\" class=\"bg-stone-900 hover:bg-black text-white font-bold text-xs px-5 rounded-xl\">Verify</button>\n" +
"        </div>\n" +
"        <div id=\"verify-msg\" class=\"hidden text-xs font-bold p-2.5 rounded-xl\"></div>\n" +
"      </div>\n" +
"    </section>\n" +
"\n" +
"    <!-- PAGE: NGO DASHBOARD -->\n" +
"    <section id=\"page-ngo-dashboard\" class=\"page-view hidden max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6\">\n" +
"      <div class=\"bg-blue-900 rounded-3xl p-6 text-white\">\n" +
"        <h2 class=\"text-2xl font-extrabold\">Hope & Feeding India Volunteer Network 🤝</h2>\n" +
"        <p class=\"text-xs text-blue-200 mt-1\">Claim ₹0 Free Bulk Surplus from Banquets & Hotels</p>\n" +
"      </div>\n" +
"      <div id=\"ngo-cards\" class=\"grid grid-cols-1 md:grid-cols-2 gap-6\"></div>\n" +
"    </section>\n" +
"\n" +
"    <!-- PAGE: ADMIN DASHBOARD -->\n" +
"    <section id=\"page-admin-dashboard\" class=\"page-view hidden max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6\">\n" +
"      <div class=\"bg-stone-900 rounded-3xl p-6 text-white\">\n" +
"        <h2 class=\"text-2xl font-extrabold\">Platform Command Center 🛡️</h2>\n" +
"        <p class=\"text-xs text-stone-400 mt-1\">Moderation & Platform Analytics</p>\n" +
"      </div>\n" +
"      <div class=\"grid grid-cols-3 gap-4 text-center\">\n" +
"        <div class=\"bg-white p-4 rounded-2xl border border-stone-200 font-bold\"><div class=\"text-2xl text-brand-700\" id=\"admin-meals\">14,850</div><div class=\"text-xs text-stone-400\">Meals Saved</div></div>\n" +
"        <div class=\"bg-white p-4 rounded-2xl border border-stone-200 font-bold\"><div class=\"text-2xl text-brand-700\" id=\"admin-savings\">₹31.4L</div><div class=\"text-xs text-stone-400\">Citizen Savings</div></div>\n" +
"        <div class=\"bg-white p-4 rounded-2xl border border-stone-200 font-bold\"><div class=\"text-2xl text-emerald-600\">37.2 Tons</div><div class=\"text-xs text-stone-400\">CO₂ Diverted</div></div>\n" +
"      </div>\n" +
"    </section>\n" +
"\n" +
"  </main>\n" +
"\n" +
"  <!-- RESERVATION MODAL -->\n" +
"  <div id=\"reserve-modal\" class=\"hidden fixed inset-0 z-50 bg-stone-900/60 flex items-center justify-center p-4\">\n" +
"    <div class=\"bg-white rounded-3xl max-w-md w-full p-6 space-y-4 shadow-2xl relative\">\n" +
"      <button onclick=\"closeModal('reserve-modal')\" class=\"absolute top-4 right-4 text-stone-400 hover:text-stone-600\">✕</button>\n" +
"      <div id=\"reserve-modal-body\"></div>\n" +
"    </div>\n" +
"  </div>\n" +
"\n" +
"  <!-- QR PASS CONFIRMATION MODAL -->\n" +
"  <div id=\"conf-modal\" class=\"hidden fixed inset-0 z-50 bg-stone-900/70 flex items-center justify-center p-4\">\n" +
"    <div class=\"bg-white rounded-3xl max-w-md w-full p-6 text-center space-y-4 shadow-2xl\">\n" +
"      <div class=\"text-3xl\">🎉</div>\n" +
"      <h3 class=\"text-xl font-extrabold text-stone-900\">Booking Confirmed!</h3>\n" +
"      <canvas id=\"qr-canvas\" width=\"180\" height=\"180\" class=\"mx-auto rounded-xl border border-stone-200\"></canvas>\n" +
"      <div class=\"text-xs font-mono font-bold text-stone-800\" id=\"conf-id\"></div>\n" +
"      <p class=\"text-xs text-stone-500\">Show this pass at the counter during pickup.</p>\n" +
"      <button onclick=\"closeModal('conf-modal'); navigateTo('customer-dashboard');\" class=\"w-full bg-brand-600 text-white font-bold text-xs py-3 rounded-xl\">View in Dashboard</button>\n" +
"    </div>\n" +
"  </div>\n" +
"\n" +
"  <!-- JAVASCRIPT STATE LOGIC -->\n" +
"  <script>\n" +
"    let currentRole = 'customer';\n" +
"    let activeCategory = 'All';\n" +
"    let foodData = [];\n" +
"    let reservationsData = [];\n" +
"\n" +
"    async function loadData() {\n" +
"      try {\n" +
"        const res = await fetch('/api/food');\n" +
"        foodData = await res.json();\n" +
"        const res2 = await fetch('/api/reservations');\n" +
"        reservationsData = await res2.json();\n" +
"        renderBrowseGrid();\n" +
"        renderDashboards();\n" +
"      } catch (e) { console.error(e); }\n" +
"      lucide.createIcons();\n" +
"    }\n" +
"\n" +
"    function navigateTo(page) {\n" +
"      document.querySelectorAll('.page-view').forEach(el => el.classList.add('hidden'));\n" +
"      const p = document.getElementById('page-' + page);\n" +
"      if (p) p.classList.remove('hidden');\n" +
"      window.scrollTo({ top: 0, behavior: 'smooth' });\n" +
"      setTimeout(() => lucide.createIcons(), 50);\n" +
"    }\n" +
"\n" +
"    function switchRole(role) {\n" +
"      currentRole = role;\n" +
"      document.getElementById('dashboard-header-label').innerText = role === 'customer' ? 'My Rescues' : role === 'business' ? 'Partner Portal' : role === 'ngo' ? 'NGO Hub' : 'Admin Desk';\n" +
"    }\n" +
"\n" +
"    function openCurrentDashboard() {\n" +
"      navigateTo(currentRole + '-dashboard');\n" +
"    }\n" +
"\n" +
"    function setFilterCategory(cat) {\n" +
"      activeCategory = cat;\n" +
"      renderBrowseGrid();\n" +
"    }\n" +
"\n" +
"    function renderBrowseGrid() {\n" +
"      const q = (document.getElementById('search-input')?.value || '').toLowerCase();\n" +
"      const filtered = foodData.filter(i => (activeCategory === 'All' || i.category === activeCategory) && (i.title.toLowerCase().includes(q) || i.restaurant.toLowerCase().includes(q)));\n" +
"      const grid = document.getElementById('browse-grid');\n" +
"      if (!grid) return;\n" +
"      grid.innerHTML = filtered.map(item => `\n" +
"        <div class=\"bg-white rounded-3xl p-4 border border-stone-200 shadow-sm flex flex-col justify-between\">\n" +
"          <div>\n" +
"            <div class=\"relative h-44 rounded-2xl overflow-hidden mb-3\">\n" +
"              <img src=\"${item.image}\" class=\"w-full h-full object-cover\" />\n" +
"              <span class=\"absolute top-2 left-2 bg-accent-orange text-white text-[10px] font-extrabold px-2 py-0.5 rounded-full\">${item.rescuePrice === 0 ? 'FREE NGO' : Math.round((item.origPrice-item.rescuePrice)*100/item.origPrice) + '% OFF'}</span>\n" +
"            </div>\n" +
"            <div class=\"text-xs text-stone-500 font-bold mb-1\">${item.restaurant}</div>\n" +
"            <h4 class=\"font-extrabold text-stone-900 text-sm mb-1\">${item.title}</h4>\n" +
"            <p class=\"text-xs text-stone-500 mb-2\">${item.description}</p>\n" +
"          </div>\n" +
"          <div class=\"flex justify-between items-center pt-3 border-t border-stone-100\">\n" +
"            <div><span class=\"text-xl font-black text-brand-700\">₹${item.rescuePrice}</span> <span class=\"text-xs line-through text-stone-400\">₹${item.origPrice}</span></div>\n" +
"            <button onclick=\"openFoodDetails('${item.id}')\" class=\"bg-brand-600 text-white font-bold text-xs px-4 py-2 rounded-xl\">Reserve</button>\n" +
"          </div>\n" +
"        </div>\n" +
"      `).join('');\n" +
"      lucide.createIcons();\n" +
"    }\n" +
"\n" +
"    function openFoodDetails(id) {\n" +
"      const item = foodData.find(i => i.id === id);\n" +
"      if (!item) return;\n" +
"      document.getElementById('reserve-modal-body').innerHTML = `\n" +
"        <h3 class=\"text-lg font-bold text-stone-900\">${item.title}</h3>\n" +
"        <p class=\"text-xs text-stone-500\">${item.restaurant} • ${item.location}</p>\n" +
"        <div class=\"bg-stone-50 p-3 rounded-xl text-xs space-y-1\">\n" +
"          <div>Pickup: <strong>${item.pickupStart} - ${item.pickupEnd}</strong></div>\n" +
"          <div>Price: <strong class=\"text-brand-700 text-base\">₹${item.rescuePrice}</strong></div>\n" +
"        </div>\n" +
"        <button onclick=\"confirmBooking('${item.id}')\" class=\"w-full bg-brand-600 text-white font-bold text-xs py-3 rounded-xl\">Confirm Reservation</button>\n" +
"      `;\n" +
"      openModal('reserve-modal');\n" +
"    }\n" +
"\n" +
"    async function confirmBooking(itemId) {\n" +
"      try {\n" +
"        const res = await fetch('/api/reservations', { method: 'POST', body: JSON.stringify({ itemId: itemId, qty: 1 }) });\n" +
"        const data = await res.json();\n" +
"        closeModal('reserve-modal');\n" +
"        document.getElementById('conf-id').innerText = data.id;\n" +
"        drawQR('qr-canvas', data.id);\n" +
"        openModal('conf-modal');\n" +
"        confetti({ particleCount: 70, spread: 60 });\n" +
"        loadData();\n" +
"      } catch (e) { alert('Reservation error'); }\n" +
"    }\n" +
"\n" +
"    async function verifyPartnerPickup() {\n" +
"      const code = document.getElementById('verify-code-input').value.trim();\n" +
"      const msg = document.getElementById('verify-msg');\n" +
"      const res = await fetch('/api/verify', { method: 'POST', body: JSON.stringify({ code }) });\n" +
"      const data = await res.json();\n" +
"      msg.classList.remove('hidden');\n" +
"      if (data.success) {\n" +
"        msg.className = 'text-xs font-bold p-2.5 rounded-xl bg-emerald-100 text-emerald-800';\n" +
"        msg.innerText = '✓ Verified! Order marked as completed.';\n" +
"        loadData();\n" +
"      } else {\n" +
"        msg.className = 'text-xs font-bold p-2.5 rounded-xl bg-red-100 text-red-800';\n" +
"        msg.innerText = '⚠️ Invalid or expired Reservation ID.';\n" +
"      }\n" +
"    }\n" +
"\n" +
"    function drawQR(canvasId, text) {\n" +
"      const canvas = document.getElementById(canvasId);\n" +
"      const ctx = canvas.getContext('2d');\n" +
"      ctx.fillStyle = '#fff'; ctx.fillRect(0,0,180,180);\n" +
"      ctx.fillStyle = '#064e3b';\n" +
"      for(let r=0; r<18; r++) {\n" +
"        for(let c=0; c<18; c++) {\n" +
"          if((r<5 && c<5) || (r<5 && c>12) || (r>12 && c<5)) ctx.fillRect(c*10, r*10, 9, 9);\n" +
"          else if ((r*c + text.length) % 3 === 0) ctx.fillRect(c*10, r*10, 8, 8);\n" +
"        }\n" +
"      }\n" +
"    }\n" +
"\n" +
"    function renderDashboards() {\n" +
"      const passes = document.getElementById('cust-passes');\n" +
"      if (passes) {\n" +
"        passes.innerHTML = reservationsData.map(r => `\n" +
"          <div class=\"bg-white p-5 rounded-3xl border border-stone-200 shadow-sm space-y-2\">\n" +
"            <div class=\"flex justify-between font-mono font-bold text-xs text-brand-700\"><span>${r.id}</span><span>${r.status}</span></div>\n" +
"            <h4 class=\"font-extrabold text-sm text-stone-900\">${r.itemTitle}</h4>\n" +
"            <div class=\"text-xs text-stone-500\">${r.restaurant} • ₹${r.totalPrice}</div>\n" +
"          </div>\n" +
"        `).join('');\n" +
"      }\n" +
"    }\n" +
"\n" +
"    function openModal(id) { document.getElementById(id).classList.remove('hidden'); }\n" +
"    function closeModal(id) { document.getElementById(id).classList.add('hidden'); }\n" +
"\n" +
"    window.onload = loadData;\n" +
"  </script>\n" +
"</body>\n" +
"</html>";
    }
}
