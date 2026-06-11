package A;

import java.net.http.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.*;

public class ChatIA {

    static Scanner scanner = new Scanner(System.in);

    // ===================== WRAP =====================

    static String wrapTexto(String texto, int ancho) {
        StringBuilder sb = new StringBuilder();
        String[] palabras = texto.split(" ");
        int lineaActual = 0;
        for (String palabra : palabras) {
            if (lineaActual + palabra.length() > ancho) {
                sb.append("\n      ");
                lineaActual = 6;
            }
            sb.append(palabra).append(" ");
            lineaActual += palabra.length() + 1;
        }
        return sb.toString().trim();
    }

    // ===================== API =====================

    static String consultarGroq(List<JSONObject> historial) throws Exception {

        JSONArray mensajes = new JSONArray();
        for (JSONObject msg : historial) {
            mensajes.put(msg);
        }

        String requestBody = new JSONObject()
            .put("model", "llama-3.3-70b-versatile")
            .put("max_tokens", 1000)
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

    // ===================== MAIN =====================

    public static void main(String[] args) throws Exception {

        List<JSONObject> historial = new ArrayList<>();

        historial.add(new JSONObject()
            .put("role", "system")
            .put("content", "Sos un asistente simpático que responde siempre en español. Sos conciso y directo."));

        System.out.println("=== CHAT CON GROQ ===");
        System.out.println("(escribí 'salir' para terminar)\n");

        while (true) {
            System.out.print("Vos: ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("salir")) {
                System.out.println("Groq: ¡Hasta luego!");
                break;
            }

            historial.add(new JSONObject()
                .put("role", "user")
                .put("content", input));

            String respuesta = consultarGroq(historial);
            System.out.println("Groq: " + wrapTexto(respuesta, 80) + "\n");

            historial.add(new JSONObject()
                .put("role", "assistant")
                .put("content", respuesta));
        }
    }
}