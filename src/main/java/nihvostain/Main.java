package nihvostain;

import com.fastcgi.FCGIInterface;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
            long startTime = System.nanoTime();
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

                    String rowData = new String(buffer, StandardCharsets.UTF_8);
                    Checker checker = new Checker(parse(rowData));
                    if (checker.isValid()) {
                        long elapsedNanos = System. nanoTime() - startTime;
                        System.out.print(createResponse("200 OK", "application/json",
                                """
                                {
                                "answer": %b,
                                "currentTime": "%s",
                                "workTimeMicros": %d
                                }
                                """.formatted(checker.inZone(),ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), elapsedNanos/1000)));
                        System.out.flush();
                    } else {
                        System.out.print(createResponse("400 Bad Request", "application/json",
                                """
                                {
                                "rawData": "%s"
                                }
                                """.formatted(rowData)));
                        System.out.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                System.err.println("CONTENT_LENGTH не задан или не число");
            }
        }
    }

    public static HashMap<String, String> parse(String rowData) throws ArrayIndexOutOfBoundsException {
        HashMap<String, String> data = new HashMap<>();
        String [] params = rowData.split("&");
        for (String param : params) {
            String[] pair = param.split("=");
            if (pair.length == 2) {
                data.put(pair[0], pair[1]);
            } else {
                data.put(pair[0], "");
            }
        }
        return data;
    }

    public static String createResponse(String status, String contentType,String content) {
        return """
           Status: %s
           Content-Type: %s
           
           %s""".formatted(status, contentType, content);
    }

}
