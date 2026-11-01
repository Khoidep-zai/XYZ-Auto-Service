package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WorkshopFlowTests {
    @LocalServerPort int port;

    class Session {
        final HttpClient client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        String csrf;
        HttpResponse<String> send(String method, String path, String body, String contentType, boolean withCsrf) throws Exception {
            var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
            if (withCsrf) request.header("X-CSRF-TOKEN", csrf);
            if (contentType != null) request.header("Content-Type", contentType);
            return client.send(request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
        }
        void token() throws Exception {
            var response = send("GET", "/api/csrf", null, null, false);
            assertEquals(200, response.statusCode());
            var match = Pattern.compile("\\\"token\\\":\\\"([^\\\"]+)\\\"").matcher(response.body());
            assertTrue(match.find(), response.body()); csrf = match.group(1);
        }
        HttpResponse<String> json(String method, String path, String body) throws Exception {
            return send(method, path, body, "application/json", true);
        }
        void login(String phone, String password) throws Exception {
            token();
            assertEquals(204, send("POST", "/api/login", "phone=" + phone + "&password=" + password,
                    "application/x-www-form-urlencoded", true).statusCode());
            token();
        }
        void register(String phone, String name) throws Exception {
            token();
            assertEquals(201, json("POST", "/api/register", "{\"name\":\"" + name + "\",\"phone\":\"" + phone + "\",\"password\":\"password123\"}").statusCode());
            login(phone, "password123");
        }
    }

    LocalDate bookingDate() {
        LocalDate date = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(1);
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) date = date.plusDays(1);
        return date;
    }
    String booking(String phone, String plate, String slot) {
        return "{\"customerName\":\"Khach Hang\",\"phone\":\"" + phone + "\",\"licensePlate\":\"" + plate +
                "\",\"carModel\":\"Toyota\",\"date\":\"" + bookingDate() + "\",\"slot\":\"" + slot +
                "\",\"serviceIds\":[\"SRV-01\"],\"note\":\"\",\"walkIn\":false}";
    }

    @Test
    void accessCapacityAndAppointmentLifecycle() throws Exception {
        var guest = new Session(); guest.token();
        assertEquals(403, guest.send("POST", "/api/register", "{}", "application/json", false).statusCode());
        assertEquals(401, guest.json("POST", "/api/appointments", booking("0901000001", "51A-11111", "08:00")).statusCode());

        var admin = new Session(); admin.login("0988000000", "admin-test-password");
        assertEquals(200, admin.json("PUT", "/api/capacity", "{\"capacity\":1}").statusCode());
        assertEquals(201, admin.json("POST", "/api/accounts", "{\"name\":\"Staff\",\"phone\":\"0902000000\",\"password\":\"password123\",\"role\":\"STAFF\"}").statusCode());

        var first = new Session(); first.register("0901000001", "First Customer");
        var second = new Session(); second.register("0901000002", "Second Customer");
        assertEquals(403, first.json("PUT", "/api/capacity", "{\"capacity\":5}").statusCode());

        var start = new java.util.concurrent.CountDownLatch(1);
        var a = CompletableFuture.supplyAsync(() -> attemptBooking(first, booking("0901000001", "51A-11111", "08:00"), start));
        var b = CompletableFuture.supplyAsync(() -> attemptBooking(second, booking("0901000002", "51A-22222", "08:00"), start));
        start.countDown();
        var firstResponse = a.get(); var secondResponse = b.get();
        assertTrue((firstResponse.statusCode() == 201 && secondResponse.statusCode() == 409)
                || (firstResponse.statusCode() == 409 && secondResponse.statusCode() == 201));
        var owner = firstResponse.statusCode() == 201 ? first : second;
        var stranger = owner == first ? second : first;
        String body = (owner == first ? firstResponse : secondResponse).body();
        var idMatch = Pattern.compile("\\\"id\\\":(\\d+)").matcher(body);
        assertTrue(idMatch.find(), body); String id = idMatch.group(1);
        String ownerPlate = owner == first ? "51A-11111" : "51A-22222";
        assertFalse(stranger.send("GET", "/api/state", null, null, false).body().contains(ownerPlate));
        var managerState = admin.send("GET", "/api/state", null, null, false);
        assertEquals(200, managerState.statusCode(), managerState.body());
        assertTrue(managerState.body().contains(ownerPlate));
        assertEquals(403, stranger.json("PUT", "/api/appointments/" + id + "/status", "{\"status\":\"CANCELLED\"}").statusCode());
        assertEquals(403, owner.json("PUT", "/api/appointments/" + id + "/status", "{\"status\":\"CONFIRMED\"}").statusCode());

        String reschedule = "{\"date\":\"" + bookingDate() + "\",\"slot\":\"09:00\",\"serviceIds\":[\"SRV-02\"]}";
        assertEquals(200, owner.json("PUT", "/api/appointments/" + id + "/schedule", reschedule).statusCode());
        String strangerPhone = stranger == first ? "0901000001" : "0901000002";
        String strangerPlate = stranger == first ? "51A-11111" : "51A-22222";
        var replacement = stranger.json("POST", "/api/appointments", booking(strangerPhone, strangerPlate, "08:00"));
        assertEquals(201, replacement.statusCode());
        var replacementId = Pattern.compile("\\\"id\\\":(\\d+)").matcher(replacement.body());
        assertTrue(replacementId.find());
        assertEquals(200, stranger.json("PUT", "/api/appointments/" + replacementId.group(1) + "/status", "{\"status\":\"CANCELLED\"}").statusCode());
        String sunday = bookingDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toString();
        assertEquals(400, stranger.json("POST", "/api/appointments", booking(strangerPhone, strangerPlate, "08:00").replace(bookingDate().toString(), sunday)).statusCode());

        String updatedService = "{\"code\":\"SRV-02\",\"name\":\"Oil service\",\"category\":\"Oil\",\"duration\":45,\"price\":2500000,\"warranty\":\"\",\"description\":\"\",\"checklist\":[\"Check\"],\"active\":true}";
        assertEquals(200, admin.json("PUT", "/api/services/SRV-02", updatedService).statusCode());
        assertTrue(owner.send("GET", "/api/state", null, null, false).body().contains("\"totalPrice\":1150000"));

        var staff = new Session(); staff.login("0902000000", "password123");
        assertEquals(200, staff.json("PUT", "/api/appointments/" + id + "/status", "{\"status\":\"CONFIRMED\",\"bay\":\"Khoang 01\",\"technician\":\"KTV A\"}").statusCode());
        assertEquals(409, staff.json("PUT", "/api/appointments/" + id + "/status", "{\"status\":\"COMPLETED\"}").statusCode());
        assertEquals(200, staff.json("PUT", "/api/appointments/" + id + "/status", "{\"status\":\"IN_PROGRESS\"}").statusCode());
        assertEquals(200, staff.json("PUT", "/api/appointments/" + id + "/status", "{\"status\":\"COMPLETED\"}").statusCode());
        assertEquals(409, owner.json("PUT", "/api/appointments/" + id + "/status", "{\"status\":\"CANCELLED\"}").statusCode());
    }

    private HttpResponse<String> attemptBooking(Session session, String body, java.util.concurrent.CountDownLatch start) {
        try { start.await(); return session.json("POST", "/api/appointments", body); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
