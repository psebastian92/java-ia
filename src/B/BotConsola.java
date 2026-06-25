package B;

import java.net.http.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import org.json.*;

public class BotConsola {


	    static Scanner scanner = new Scanner(System.in);

	    // ===================== CRUD =====================

	    static void agregarProducto(List<HashMap<String, String>> productos) {
	        System.out.print("Nombre: ");
	        String nombre = scanner.nextLine();
	        System.out.print("Precio: ");
	        String precio = scanner.nextLine();
	        System.out.print("Stock: ");
	        String stock = scanner.nextLine();

	        HashMap<String, String> p = new HashMap<>();
	        p.put("nombre", nombre);
	        p.put("precio", precio);
	        p.put("stock", stock);
	        productos.add(p);
	        System.out.println("✓ Producto agregado.");
	    }

	    static void visualizarProductos(List<HashMap<String, String>> productos) {
	        if (productos.isEmpty()) {
	            System.out.println("No hay productos cargados.");
	            return;
	        }
	        System.out.println("\n--- PRODUCTOS ---");
	        for (int i = 0; i < productos.size(); i++) {
	            HashMap<String, String> p = productos.get(i);
	            System.out.println((i + 1) + ". " + p.get("nombre")
	                + " | $" + p.get("precio")
	                + " | Stock: " + p.get("stock"));
	        }
	        System.out.println("-----------------");
	    }

	    static void modificarProducto(List<HashMap<String, String>> productos) {
	        visualizarProductos(productos);
	        System.out.print("Número a modificar: ");
	        int idx = Integer.parseInt(scanner.nextLine()) - 1;
	        if (idx < 0 || idx >= productos.size()) {
	            System.out.println("Número inválido.");
	            return;
	        }
	        HashMap<String, String> p = productos.get(idx);
	        System.out.print("Nuevo nombre (" + p.get("nombre") + "): ");
	        p.put("nombre", scanner.nextLine());
	        System.out.print("Nuevo precio (" + p.get("precio") + "): ");
	        p.put("precio", scanner.nextLine());
	        System.out.print("Nuevo stock (" + p.get("stock") + "): ");
	        p.put("stock", scanner.nextLine());
	        System.out.println("✓ Producto modificado.");
	    }

	    static void eliminarProducto(List<HashMap<String, String>> productos) {
	        visualizarProductos(productos);
	        System.out.print("Número a eliminar: ");
	        int idx = Integer.parseInt(scanner.nextLine()) - 1;
	        if (idx < 0 || idx >= productos.size()) {
	            System.out.println("Número inválido.");
	            return;
	        }
	        System.out.println("✓ Eliminado: " + productos.get(idx).get("nombre"));
	        productos.remove(idx);
	    }

	    static void ordenarPorPrecio(List<HashMap<String, String>> productos) {
	        for (int i = 0; i < productos.size() - 1; i++)
	            for (int j = 0; j < productos.size() - i - 1; j++)
	                if (Double.parseDouble(productos.get(j).get("precio")) >
	                    Double.parseDouble(productos.get(j + 1).get("precio"))) {
	                    HashMap<String, String> temp = productos.get(j);
	                    productos.set(j, productos.get(j + 1));
	                    productos.set(j + 1, temp);
	                }
	        System.out.println("✓ Ordenado por precio.");
	        visualizarProductos(productos);
	    }

	    static void buscarProducto(List<HashMap<String, String>> productos) {
	        System.out.print("Nombre a buscar: ");
	        String busqueda = scanner.nextLine().toLowerCase();
	        boolean encontrado = false;
	        for (HashMap<String, String> p : productos) {
	            if (p.get("nombre").toLowerCase().contains(busqueda)) {
	                System.out.println("Encontrado: " + p.get("nombre")
	                    + " | $" + p.get("precio")
	                    + " | Stock: " + p.get("stock"));
	                encontrado = true;
	            }
	        }
	        if (!encontrado) System.out.println("No se encontró ningún producto.");
	    }

	    // ===================== IA =====================

	    static String wrapTexto(String texto, int ancho) {
	        StringBuilder sb = new StringBuilder();
	        String[] palabras = texto.split(" ");
	        int lineaActual = 0;
	        for (String palabra : palabras) {
	            if (lineaActual + palabra.length() > ancho) {
	                sb.append("\n           ");
	                lineaActual = 11;
	            }
	            sb.append(palabra).append(" ");
	            lineaActual += palabra.length() + 1;
	        }
	        return sb.toString().trim();
	    }

	    static String consultarGroq(List<JSONObject> historial) throws Exception {
	        JSONArray mensajes = new JSONArray();
	        for (JSONObject msg : historial) mensajes.put(msg);

	        String requestBody = new JSONObject()
	            .put("model", "llama-3.3-70b-versatile")
	            .put("max_tokens", 512)
	            .put("messages", mensajes)
	            .toString();

	        HttpClient client = HttpClient.newHttpClient();
	        HttpRequest request = HttpRequest.newBuilder()
	            .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
	            .header("Authorization", "Bearer " + System.getenv("GROQ_API_KEY"))
	            .header("Content-Type", "application/json")
	            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
	            .build();

	        HttpResponse<String> response = client.send(request,
	            HttpResponse.BodyHandlers.ofString());
	        JSONObject json = new JSONObject(response.body());
	        return json.getJSONArray("choices")
	                   .getJSONObject(0)
	                   .getJSONObject("message")
	                   .getString("content");
	    }

	    static void chatIA(List<HashMap<String, String>> productos) throws Exception {
	        List<JSONObject> historial = new ArrayList<>();

	        StringBuilder lista = new StringBuilder();
	        if (productos.isEmpty()) {
	            lista.append("No hay productos cargados.");
	        } else {
	            for (HashMap<String, String> p : productos) {
	                lista.append(p.get("nombre"))
	                     .append(" | $").append(p.get("precio"))
	                     .append(" | Stock: ").append(p.get("stock"))
	                     .append("\n");
	            }
	        }

	        historial.add(new JSONObject()
	            .put("role", "system")
	            .put("content", "Sos un asistente simpático de gestión de productos. "
	                + "Tenés acceso a esta lista de productos:\n" + lista
	                + "Respondé preguntas sobre esos productos de forma breve y en español. "
	                + "Si te piden modificar o eliminar, deciles que lo hagan desde el menú principal."));

	        System.out.println("\nAsistente: ¡Hola! Tengo acceso a tu lista de productos.");
	        System.out.println("           ¿Qué querés saber? (escribí 'salir' para volver)\n");

	        while (true) {
	            System.out.print("Vos: ");
	            String input = scanner.nextLine();

	            if (input.equalsIgnoreCase("salir")) {
	                System.out.println("Asistente: ¡Hasta luego! Volvé cuando necesites.\n");
	                break;
	            }

	            historial.add(new JSONObject().put("role", "user").put("content", input));
	            String respuesta = consultarGroq(historial);
	            System.out.println("Asistente: " + wrapTexto(respuesta, 70) + "\n");
	            historial.add(new JSONObject().put("role", "assistant").put("content", respuesta));
	        }
	    }

	    // ===================== MAIN =====================

	    public static void main(String[] args) throws Exception {
	        List<HashMap<String, String>> productos = new ArrayList<>();

	        // Datos de prueba
	        HashMap<String, String> p1 = new HashMap<>();
	        p1.put("nombre", "Calculadora Científica"); p1.put("precio", "8500"); p1.put("stock", "10");
	        productos.add(p1);

	        HashMap<String, String> p2 = new HashMap<>();
	        p2.put("nombre", "Lápiz HB"); p2.put("precio", "150"); p2.put("stock", "50");
	        productos.add(p2);

	        HashMap<String, String> p3 = new HashMap<>();
	        p3.put("nombre", "Goma de borrar"); p3.put("precio", "200"); p3.put("stock", "2");
	        productos.add(p3);

	        HashMap<String, String> p4 = new HashMap<>();
	        p4.put("nombre", "Regla 30cm"); p4.put("precio", "350"); p4.put("stock", "15");
	        productos.add(p4);

	        HashMap<String, String> p5 = new HashMap<>();
	        p5.put("nombre", "Corrector líquido"); p5.put("precio", "750"); p5.put("stock", "2");
	        productos.add(p5);

	        while (true) {
	            System.out.println("\n=== GESTIÓN DE PRODUCTOS ===");
	            System.out.println("Productos cargados: " + productos.size());
	            System.out.println("1. Agregar producto");
	            System.out.println("2. Modificar producto");
	            System.out.println("3. Eliminar producto");
	            System.out.println("4. Visualizar productos");
	            System.out.println("5. Ordenar por precio");
	            System.out.println("6. Buscar producto");
	            System.out.println("7. Consultar al asistente IA 🤖");
	            System.out.println("0. Salir");
	            System.out.print("\nOpción: ");

	            String opcion = scanner.nextLine();

	            switch (opcion) {
	                case "1" -> agregarProducto(productos);
	                case "2" -> modificarProducto(productos);
	                case "3" -> eliminarProducto(productos);
	                case "4" -> visualizarProductos(productos);
	                case "5" -> ordenarPorPrecio(productos);
	                case "6" -> buscarProducto(productos);
	                case "7" -> chatIA(productos);
	                case "0" -> { System.out.println("¡Hasta luego!"); return; }
	                default  -> System.out.println("Opción inválida.");
	            }
	        }
	    }
}