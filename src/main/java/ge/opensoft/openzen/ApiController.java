package ge.opensoft.openzen;

import static ge.opensoft.openzen.Core.*;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.*;

@RestController
public class ApiController {
    private final Core core;

    public ApiController(Core core) {
        this.core = core;
    }

    @RequestMapping("/**")
    public ResponseEntity<?> dispatch(
            HttpServletRequest request, @RequestBody(required = false) byte[] bytes)
            throws Exception {
        return core.execute(
                request,
                bytes,
                this::route,
                e ->
                        map(
                                "error",
                                switch (e.status) {
                                    case 409 -> "UpdateConflict";
                                    case 404 -> "RecordNotFound";
                                    default -> e.title().replace(" ", "");
                                },
                                "description",
                                e.getMessage()));
    }

    private Result route() throws Exception {
        String path = core.request().getRequestURI(), method = core.request().getMethod();
        if (path.equals("/api/v2/tickets.json")) {
            requireMethod(method, "GET", "POST");
            agent();
            if (method.equals("GET"))
                return ok(
                        paged(
                                core.rows("ticket").stream().map(this::ticketView).toList(),
                                "tickets"));
            core.parameters();
            core.writable();
            var value = validate(object(core.body(), "ticket").get("ticket"), true);
            if ("closed".equals(value.get("status")))
                throw new Failure(400, "Create an active ticket first");
            return created(map("ticket", ticketView(newTicket(value))));
        }
        if (path.equals("/api/v2/requests.json")) {
            requireMethod(method, "GET", "POST");
            if (method.equals("GET"))
                return ok(
                        paged(
                                core.rows("ticket").stream()
                                        .filter(this::mine)
                                        .map(this::requestView)
                                        .toList(),
                                "requests"));
            core.parameters();
            var value = object(object(core.body(), "request").get("request"), "subject", "comment");
            validate(value, true);
            object(value.get("comment"), "body");
            return created(map("request", requestView(newTicket(value))));
        }
        Matcher route =
                Pattern.compile(
                                "/api/v2/(tickets|requests)/([0-9]+)(?:\\.json|/(comments\\.json|tags(?:\\.json)?))")
                        .matcher(path);
        if (!route.matches()) throw new Failure(404, "Resource not found");
        boolean ticket = route.group(1).equals("tickets");
        String id = new BigInteger(route.group(2)).toString(), resource = route.group(3);
        if (ticket) agent();
        var item = core.get("ticket", id);
        if (!ticket && !mine(item)) throw new Failure(404, "Request not found");
        if ("comments.json".equals(resource)) {
            requireMethod(method, "GET");
            return ok(
                    paged(
                            comments(item).stream()
                                    .filter(c -> ticket || Boolean.TRUE.equals(c.get("public")))
                                    .toList(),
                            "comments"));
        }
        core.parameters();
        if (resource != null) {
            if (!ticket) throw new Failure(404, "Resource not found");
            requireMethod(method, "GET", "PUT", "POST", "DELETE");
            if (method.equals("GET")) return ok(map("tags", item.get("tags")));
            core.writable();
            var value =
                    validate(
                            object(
                                    core.body(),
                                    Set.of("tags", "safe_update", "updated_stamp"),
                                    Set.of("tags")),
                            false);
            mutable(item, value);
            var tags = new ArrayList<>(new LinkedHashSet<>(list(value.get("tags"))));
            if (method.equals("PUT")) {
                var combined = new LinkedHashSet<>(list(item.get("tags")));
                combined.addAll(tags);
                tags = new ArrayList<>(combined);
            } else if (method.equals("DELETE")) {
                var removed = tags;
                tags =
                        new ArrayList<>(
                                list(item.get("tags")).stream()
                                        .filter(tag -> !removed.contains(tag))
                                        .toList());
            }
            validate(map("tags", tags), false);
            item.put("tags", tags);
            item.put("updated_at", timestamp());
            core.save("ticket", id, item);
            return ok(map("tags", tags));
        }
        requireMethod(
                method,
                ticket ? new String[] {"GET", "PUT", "DELETE"} : new String[] {"GET", "PUT"});
        if (method.equals("GET"))
            return ok(ticket ? map("ticket", ticketView(item)) : map("request", requestView(item)));
        if (!ticket) {
            if (item.get("status").equals("closed"))
                throw new Failure(409, "Closed requests cannot accept replies");
            var value = object(object(core.body(), "request").get("request"), "comment");
            object(value.get("comment"), "body");
            comments(item).add(comment(value.get("comment"), true));
        } else {
            core.writable();
            if (method.equals("DELETE")) {
                if (!core.actor().get("role").equals("admin"))
                    throw new Failure(403, "Administrator required to delete");
                core.delete("ticket", id);
                return empty();
            }
            var value = validate(object(core.body(), "ticket").get("ticket"), false);
            mutable(item, value);
            if (value.containsKey("comment"))
                comments(item)
                        .add(
                                comment(
                                        value.get("comment"),
                                        (Boolean) comments(item).getLast().get("public")));
            value.forEach((key, field) -> {
                if (!Set.of("comment", "safe_update", "updated_stamp").contains(key))
                    item.put(key, field);
            });
        }
        item.put("updated_at", timestamp());
        core.save("ticket", id, item);
        return ok(ticket ? map("ticket", ticketView(item)) : map("request", requestView(item)));
    }

    private void agent() {
        if (core.actor().get("role").equals("end_user"))
            throw new Failure(403, "Use the Requests API");
    }

    private BigInteger user() {
        return new BigInteger((String) core.actor().get("user"));
    }

    private boolean mine(Map<String, Object> item) {
        return new BigInteger(item.get("requester_id").toString()).equals(user());
    }

    private Map<String, Object> comment(Object value, boolean defaultPublic) throws Exception {
        var body = object(value, Set.of("body", "public"), Set.of("body"));
        text(body.get("body"));
        if (!(body.getOrDefault("public", defaultPublic) instanceof Boolean))
            throw new Failure(400, "Comment public must be boolean");
        return map(
                "id",
                core.sequence("comment"),
                "body",
                body.get("body"),
                "public",
                body.getOrDefault("public", defaultPublic),
                "author_id",
                user(),
                "created_at",
                timestamp());
    }

    private Map<String, Object> validate(Object body, boolean creating) {
        var value =
                object(
                        body,
                        Set.of(
                                "subject",
                                "comment",
                                "status",
                                "priority",
                                "tags",
                                "requester_id",
                                "safe_update",
                                "updated_stamp"),
                        creating ? Set.of("subject", "comment") : Set.of());
        if (value.containsKey("subject")) text(value.get("subject"), 255, false);
        if (value.containsKey("status")
                && !Arrays.asList("new", "open", "pending", "hold", "solved", "closed")
                        .contains(value.get("status"))) throw new Failure(400, "Invalid status");
        if (value.containsKey("priority")
                && !Arrays.asList("low", "normal", "high", "urgent")
                        .contains(value.get("priority")))
            throw new Failure(400, "Invalid priority");
        if (value.containsKey("requester_id")) {
            Object requester = value.get("requester_id");
            if (!(requester instanceof Integer
                            || requester instanceof Long
                            || requester instanceof BigInteger)
                    || new BigInteger(requester.toString()).signum() < 1)
                throw new Failure(400, "Invalid requester ID");
        }
        if (value.containsKey("tags")) {
            if (!(value.get("tags") instanceof List<?> tags) || tags.size() > 100)
                throw new Failure(400, "Provide at most 100 tags");
            for (Object tag : tags) text(tag, 100, false);
        }
        if (value.containsKey("safe_update") && !(value.get("safe_update") instanceof Boolean))
            throw new Failure(400, "safe_update must be boolean");
        if (Boolean.TRUE.equals(value.get("safe_update"))
                && !(value.get("updated_stamp") instanceof String))
            throw new Failure(400, "safe_update requires updated_stamp");
        return value;
    }

    private void mutable(Map<String, Object> item, Map<String, Object> value) {
        if (item.get("status").equals("closed"))
            throw new Failure(409, "Closed tickets are immutable in this subset");
        if (Boolean.TRUE.equals(value.get("safe_update"))
                && !value.get("updated_stamp").equals(item.get("updated_at")))
            throw new Failure(409, "Ticket changed; reload before updating");
    }

    private Map<String, Object> newTicket(Map<String, Object> value) throws Exception {
        var first = comment(value.get("comment"), true);
        long id = core.sequence("ticket");
        String now = timestamp();
        var item =
                map(
                        "id",
                        id,
                        "subject",
                        value.get("subject"),
                        "description",
                        first.get("body"),
                        "status",
                        value.getOrDefault("status", "new"),
                        "priority",
                        value.getOrDefault("priority", "normal"),
                        "tags",
                        new ArrayList<>(
                                new LinkedHashSet<>(list(value.getOrDefault("tags", List.of())))),
                        "requester_id",
                        value.getOrDefault("requester_id", user()),
                        "submitter_id",
                        user(),
                        "created_at",
                        now,
                        "updated_at",
                        now,
                        "comments",
                        new ArrayList<>(List.of(first)));
        return core.insert("ticket", id, item);
    }

    private Map<String, Object> ticketView(Map<String, Object> item) {
        var view = new LinkedHashMap<>(item);
        view.remove("comments");
        return view;
    }

    private Map<String, Object> requestView(Map<String, Object> item) {
        return map(
                "id",
                item.get("id"),
                "subject",
                item.get("subject"),
                "status",
                item.get("status"),
                "description",
                comments(item).stream()
                        .filter(c -> Boolean.TRUE.equals(c.get("public")))
                        .map(c -> c.get("body"))
                        .findFirst()
                        .orElse(""),
                "requester_id",
                item.get("requester_id"));
    }

    private Map<String, Object> paged(List<?> data, String key) {
        core.parameters("page", "per_page");
        int page = integer(core.argument("page", "1"), 1, 100),
                size = integer(core.argument("per_page", "100"), 1, 100),
                offset = (page - 1) * size;
        String base = core.request().getRequestURL().toString();
        return map(
                key,
                data.subList(Math.min(offset, data.size()), Math.min(offset + size, data.size())),
                "count",
                data.size(),
                "next_page",
                offset + size < data.size()
                        ? base + "?page=" + (page + 1) + "&per_page=" + size
                        : null,
                "previous_page",
                page > 1 ? base + "?page=" + (page - 1) + "&per_page=" + size : null);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> comments(Map<String, Object> item) {
        return (List<Map<String, Object>>) item.get("comments");
    }

    @SuppressWarnings("unchecked")
    private static List<Object> list(Object value) {
        return (List<Object>) value;
    }

    private static void requireMethod(String actual, String... methods) {
        if (!List.of(methods).contains(actual)) throw new Failure(405, "Method not allowed");
    }
}
