import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;

/**
 * Demonstrates parsing complex JSON structures using Jackson.
 * Covers: nested objects, arrays, dynamic node traversal, and type-safe access.
 *
 * Dependency (Maven):
 *   <dependency>
 *     <groupId>com.fasterxml.jackson.core</groupId>
 *     <artifactId>jackson-databind</artifactId>
 *     <version>2.17.0</version>
 *   </dependency>
 */
public class JsonParser {

    // ─── Sample complex JSON ────────────────────────────────────────────────────
    private static final String COMPLEX_JSON = """
        {
          "company": {
            "name": "TechCorp",
            "founded": 2010,
            "active": true,
            "revenue": 4500000.75,
            "headquarters": {
              "city": "San Francisco",
              "state": "CA",
              "country": "USA",
              "coordinates": {
                "lat": 37.7749,
                "lon": -122.4194
              }
            },
            "departments": [
              {
                "id": 1,
                "name": "Engineering",
                "headcount": 120,
                "teams": [
                  { "team": "Backend",  "size": 45, "lead": "Alice" },
                  { "team": "Frontend", "size": 30, "lead": "Bob"   },
                  { "team": "DevOps",   "size": 15, "lead": "Carol" }
                ]
              },
              {
                "id": 2,
                "name": "Marketing",
                "headcount": 40,
                "teams": [
                  { "team": "Digital", "size": 20, "lead": "Dave"  },
                  { "team": "Brand",   "size": 10, "lead": "Eve"   }
                ]
              }
            ],
            "tags": ["startup", "saas", "cloud"],
            "metadata": null
          }
        }
        """;

    // ─── Main ────────────────────────────────────────────────────────────────────
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(COMPLEX_JSON);

        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  JSON Node Parser Demo");
        System.out.println("═══════════════════════════════════════════════════\n");

        demo1_BasicNodeAccess(root);
        demo2_DeepNestedAccess(root);
        demo3_ArrayTraversal(root);
        demo4_NestedArrays(root);
        demo5_DynamicTraversal(root);
        demo6_TypeChecks(root);
        demo7_BuildAndModify(mapper);
    }

    // ─── Demo 1: Basic node access ───────────────────────────────────────────────
    static void demo1_BasicNodeAccess(JsonNode root) {
        System.out.println("── Demo 1: Basic Node Access ──────────────────────");

        JsonNode company = root.get("company");

        String  name    = company.get("name").asText();
        int     founded = company.get("founded").asInt();
        boolean active  = company.get("active").asBoolean();
        double  revenue = company.get("revenue").asDouble();

        System.out.printf("  Name    : %s%n",  name);
        System.out.printf("  Founded : %d%n",  founded);
        System.out.printf("  Active  : %b%n",  active);
        System.out.printf("  Revenue : $%.2f%n%n", revenue);
    }

    // ─── Demo 2: Deep nested access ──────────────────────────────────────────────
    static void demo2_DeepNestedAccess(JsonNode root) {
        System.out.println("── Demo 2: Deep Nested Access ─────────────────────");

        // Chained .get()
        JsonNode hq = root.get("company").get("headquarters");
        System.out.println("  City    : " + hq.get("city").asText());
        System.out.println("  State   : " + hq.get("state").asText());

        // Jackson path() — safe: returns MissingNode instead of null
        double lat = root.path("company").path("headquarters")
                         .path("coordinates").path("lat").asDouble();
        double lon = root.path("company").path("headquarters")
                         .path("coordinates").path("lon").asDouble();
        System.out.printf("  Coords  : %.4f, %.4f%n", lat, lon);

        // at() — JSON Pointer (RFC 6901)
        String country = root.at("/company/headquarters/country").asText();
        System.out.println("  Country : " + country);

        // Null / missing handling
        JsonNode metadata = root.path("company").path("metadata");
        System.out.println("  metadata isNull?    " + metadata.isNull());
        JsonNode missing  = root.path("company").path("nonexistent");
        System.out.println("  nonexistent isMissing? " + missing.isMissingNode());
        System.out.println();
    }

    // ─── Demo 3: Array traversal ─────────────────────────────────────────────────
    static void demo3_ArrayTraversal(JsonNode root) {
        System.out.println("── Demo 3: Array Traversal ────────────────────────");

        JsonNode tags = root.at("/company/tags");
        System.out.println("  Tags (" + tags.size() + "):");
        for (JsonNode tag : tags) {
            System.out.println("    • " + tag.asText());
        }

        // Index-based access
        System.out.println("  First tag : " + tags.get(0).asText());
        System.out.println("  Last  tag : " + tags.get(tags.size() - 1).asText());
        System.out.println();
    }

    // ─── Demo 4: Nested arrays (departments → teams) ─────────────────────────────
    static void demo4_NestedArrays(JsonNode root) {
        System.out.println("── Demo 4: Nested Arrays (departments → teams) ────");

        ArrayNode departments = (ArrayNode) root.at("/company/departments");

        for (JsonNode dept : departments) {
            System.out.printf("  Department [%d]: %s  (headcount: %d)%n",
                    dept.get("id").asInt(),
                    dept.get("name").asText(),
                    dept.get("headcount").asInt());

            for (JsonNode team : dept.get("teams")) {
                System.out.printf("      ↳ %-10s  size: %2d  lead: %s%n",
                        team.get("team").asText(),
                        team.get("size").asInt(),
                        team.get("lead").asText());
            }
        }
        System.out.println();
    }

    // ─── Demo 5: Dynamic / generic traversal (all fields recursively) ────────────
    static void demo5_DynamicTraversal(JsonNode root) {
        System.out.println("── Demo 5: Dynamic Recursive Traversal ────────────");
        traverseNode(root, "");
        System.out.println();
    }

    static void traverseNode(JsonNode node, String indent) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key   = entry.getKey();
                JsonNode val = entry.getValue();

                if (val.isValueNode()) {
                    System.out.printf("  %s%s : %s%n", indent, key, val.asText());
                } else if (val.isObject()) {
                    System.out.printf("  %s%s : {object}%n", indent, key);
                    traverseNode(val, indent + "  ");
                } else if (val.isArray()) {
                    System.out.printf("  %s%s : [array, size=%d]%n", indent, key, val.size());
                    // Only recurse one level for brevity in demo output
                } else if (val.isNull()) {
                    System.out.printf("  %s%s : null%n", indent, key);
                }
            }
        }
    }

    // ─── Demo 6: Node type checks ────────────────────────────────────────────────
    static void demo6_TypeChecks(JsonNode root) {
        System.out.println("── Demo 6: Node Type Introspection ────────────────");

        JsonNode company = root.get("company");
        checkType("company",      company);
        checkType("name",         company.get("name"));
        checkType("founded",      company.get("founded"));
        checkType("active",       company.get("active"));
        checkType("revenue",      company.get("revenue"));
        checkType("departments",  company.get("departments"));
        checkType("metadata",     company.get("metadata"));
        checkType("missing",      company.path("missing"));
        System.out.println();
    }

    static void checkType(String label, JsonNode node) {
        String type = switch (node.getNodeType()) {
            case OBJECT  -> "ObjectNode";
            case ARRAY   -> "ArrayNode";
            case STRING  -> "StringNode";
            case NUMBER  -> node.isInt() ? "IntNode" : "DoubleNode";
            case BOOLEAN -> "BooleanNode";
            case NULL    -> "NullNode";
            case MISSING -> "MissingNode";
            default      -> "Unknown";
        };
        System.out.printf("  %-14s → %s%n", label, type);
    }

    // ─── Demo 7: Build & modify JSON programmatically ───────────────────────────
    static void demo7_BuildAndModify(ObjectMapper mapper) throws Exception {
        System.out.println("── Demo 7: Build & Modify JSON Programmatically ───");

        ObjectNode person = mapper.createObjectNode();
        person.put("id",    42);
        person.put("name",  "Jane Doe");
        person.put("email", "jane@example.com");

        ObjectNode address = mapper.createObjectNode();
        address.put("street", "123 Main St");
        address.put("city",   "Austin");
        person.set("address", address);

        ArrayNode skills = mapper.createArrayNode();
        skills.add("Java").add("Kotlin").add("Spring Boot");
        person.set("skills", skills);

        // Modify existing node
        ((ObjectNode) person).put("name", "Jane Smith");   // update
        ((ObjectNode) person).remove("email");             // delete

        System.out.println("  Built JSON:");
        System.out.println(mapper.writerWithDefaultPrettyPrinter()
                                 .writeValueAsString(person)
                                 .replaceAll("(?m)^", "    "));
    }
}
