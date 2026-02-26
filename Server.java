package bank;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Filter;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

/*
 * Server.java — HTTP server that exposes the Bank as a REST API.
 *
 * It also SERVES the frontend HTML file at http://localhost:8080
 * so there are NO CORS issues (frontend and API are on the same port).
 *
 * Routes:
 *   GET  /                             → serves frontend/index.html
 *   GET  /api/accounts                 → list all accounts
 *   POST /api/accounts                 → create account
 *   GET  /api/accounts/{id}            → get one account
 *   POST /api/accounts/{id}/deposit    → deposit
 *   POST /api/accounts/{id}/withdraw   → withdraw
 *   GET  /api/accounts/{id}/history    → transaction history
 *   POST /api/undo                     → undo last transaction
 *   GET  /api/search?name=...          → search by name
 *   GET  /api/stats                    → bank stats
 */
public class Server {

    private static final int PORT = 8081;
    private final Bank bank = new Bank();

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Attach routes
        server.createContext("/",            new FrontendHandler());
        server.createContext("/api/accounts",new AccountsHandler());
        server.createContext("/api/undo",    new UndoHandler());
        server.createContext("/api/search",  new SearchHandler());
        server.createContext("/api/stats",   new StatsHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("Server running at http://localhost:" + PORT);
        System.out.println("Open that URL in your browser.");
    }

    public static void main(String[] args) throws IOException {
        new Server().start();
    }

    // ── Serve the HTML frontend ──────────────────────────────────────────────
    class FrontendHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            // Only handle GET /
            if (!ex.getRequestURI().getPath().equals("/")) {
                respond(ex, 404, "text/plain", "Not found");
                return;
            }
            File file = new File("frontend/index.html");
            if (!file.exists()) {
                respond(ex, 404, "text/plain", "Run from project root. Missing: frontend/index.html");
                return;
            }
            byte[] html = java.nio.file.Files.readAllBytes(file.toPath());
            ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            ex.sendResponseHeaders(200, html.length);
            ex.getResponseBody().write(html);
            ex.getResponseBody().close();
        }
    }

    // ── /api/accounts and sub-routes ────────────────────────────────────────
    class AccountsHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            String[] parts  = ex.getRequestURI().getPath().split("/"); // ["", "api", "accounts", ...]
            String   method = ex.getRequestMethod();

            try {
                if (parts.length == 3) {
                    // /api/accounts
                    if ("GET".equals(method)) {
                        sendJson(ex, 200, accountListJson(bank.getAllAccounts()));

                    } else if ("POST".equals(method)) {
                        Map<String, String> body = readBody(ex);
                        Account acc = bank.createAccount(
                            body.get("ownerName"),
                            body.get("email"),
                            Double.parseDouble(body.getOrDefault("balance", "0")),
                            body.getOrDefault("type", "SAVINGS")
                        );
                        sendJson(ex, 201, accountJson(acc));
                    }

                } else if (parts.length == 4) {
                    // /api/accounts/{id}
                    String id  = parts[3];
                    Account acc = bank.getAccount(id);
                    if (acc == null) { sendJson(ex, 404, "{\"error\":\"Account not found\"}"); return; }
                    sendJson(ex, 200, accountJson(acc));

                } else if (parts.length == 5) {
                    // /api/accounts/{id}/deposit  or  /api/accounts/{id}/withdraw  or  /api/accounts/{id}/history
                    String id     = parts[3];
                    String action = parts[4];

                    if ("history".equals(action)) {
                        sendJson(ex, 200, txListJson(bank.getHistory(id)));

                    } else if ("deposit".equals(action) && "POST".equals(method)) {
                        Map<String, String> body = readBody(ex);
                        bank.deposit(id, Double.parseDouble(body.get("amount")), body.getOrDefault("note", "Deposit"));
                        sendJson(ex, 200, accountJson(bank.getAccount(id)));

                    } else if ("withdraw".equals(action) && "POST".equals(method)) {
                        Map<String, String> body = readBody(ex);
                        bank.withdraw(id, Double.parseDouble(body.get("amount")), body.getOrDefault("note", "Withdrawal"));
                        sendJson(ex, 200, accountJson(bank.getAccount(id)));
                    }
                }
            } catch (Exception e) {
                sendJson(ex, 400, "{\"error\":\"" + esc(e.getMessage()) + "\"}");
            }
        }
    }

    // ── /api/undo ────────────────────────────────────────────────────────────
    class UndoHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            String msg = bank.undo();
            sendJson(ex, 200, "{\"message\":\"" + esc(msg) + "\"}");
        }
    }

    // ── /api/search?name=... ─────────────────────────────────────────────────
    class SearchHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            String query = ex.getRequestURI().getQuery(); // "name=Alice"
            String name  = "";
            if (query != null && query.startsWith("name=")) {
                name = java.net.URLDecoder.decode(query.substring(5), "UTF-8");
            }
            sendJson(ex, 200, accountListJson(bank.searchByName(name)));
        }
    }

    // ── /api/stats ───────────────────────────────────────────────────────────
    class StatsHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            Map<String, Object> stats = bank.getStats();
            sendJson(ex, 200,
                "{\"totalAccounts\":" + stats.get("totalAccounts") +
                ",\"totalBalance\":"  + stats.get("totalBalance")  +
                ",\"undoStackSize\":" + stats.get("undoStackSize") + "}");
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void sendJson(HttpExchange ex, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    private void respond(HttpExchange ex, int code, String ct, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", ct);
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    // Read JSON body and parse it into a simple key->value map
    private Map<String, String> readBody(HttpExchange ex) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        int n;
        InputStream is = ex.getRequestBody();
        while ((n = is.read(chunk)) != -1) buf.write(chunk, 0, n);
        String raw = buf.toString("UTF-8").trim();
        Map<String, String> map = new LinkedHashMap<>();
        raw = raw.replaceAll("^\\{|\\}$", ""); // strip { and }
        // Split on commas that are not inside quotes
        for (String pair : raw.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                map.put(kv[0].trim().replaceAll("\"", ""),
                        kv[1].trim().replaceAll("\"", ""));
            }
        }
        return map;
    }

    private String accountJson(Account a) {
        return "{\"accountNumber\":\"" + a.accountNumber + "\"" +
               ",\"ownerName\":\""     + esc(a.ownerName) + "\"" +
               ",\"email\":\""         + esc(a.email)     + "\"" +
               ",\"balance\":"         + a.balance        +
               ",\"type\":\""          + a.type           + "\"}";
    }

    private String accountListJson(java.util.List<Account> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(accountJson(list.get(i)));
        }
        return sb.append("]").toString();
    }

    private String txListJson(java.util.List<Transaction> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            Transaction t = list.get(i);
            sb.append("{\"id\":\"")          .append(t.id)           .append("\"")
              .append(",\"type\":\"")         .append(t.type)         .append("\"")
              .append(",\"amount\":")         .append(t.amount)
              .append(",\"balanceAfter\":")   .append(t.balanceAfter)
              .append(",\"note\":\"")         .append(esc(t.note))    .append("\"}");
        }
        return sb.append("]").toString();
    }

    // Escape special characters for JSON strings
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
