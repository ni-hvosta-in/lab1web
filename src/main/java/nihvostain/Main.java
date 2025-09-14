package nihvostain;

import com.fastcgi.FCGIInterface;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        String portStr = System.getProperty("FCGI_PORT");
        if (portStr == null) {
            System.err.println("Не указан порт: запускай с флагом -DFCGI_PORT=<порт>");
            return;
        }

        System.out.println("Запускаю FastCGI сервер на порту " + portStr);

        FCGIInterface fcgi = new FCGIInterface();

        while (fcgi.FCGIaccept() >= 0) {
            try {
                String method = System.getProperty("REQUEST_METHOD");

                if ("POST".equalsIgnoreCase(method)) {
                    int len = Integer.parseInt(System.getProperty("CONTENT_LENGTH"));
                    byte[] buffer = new byte[len];
                    int read = 0;
                    while (read < len) {
                        int r = System.in.read(buffer, read, len - read);
                        if (r == -1) break;
                        read += r;
                    }

                    String data = new String(buffer, StandardCharsets.UTF_8);

                    System.out.print("Status: 200 OK\r\n");
                    System.out.print("Content-Type: text/html; charset=UTF-8\r\n\r\n");
                    System.out.print("<html><body><h2>Полученные данные:</h2><pre>" + data + "</pre></body></html>");
                    System.out.flush();

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                System.err.println("CONTENT_LENGTH не задан или не число");
            }
        }
    }

    private HashMap<String, Float> parse(String data) {
        HashMap<String, Float> map = new HashMap<>();
        String [] params = data.split("&");
        for (String param : params) {
            map.put(param.split("=")[0], Float.parseFloat(param.split("=")[1]));
        }
        return map;
    }
}
