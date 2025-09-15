package nihvostain;

import java.util.HashMap;

public class Checker {

    private HashMap<String, String> data;
    private int x;
    private float y;
    private int r;
    public Checker(HashMap<String,String> data) {
        this.data = data;
    }
    public boolean isValid() {
        try {
            x = Integer.parseInt(data.get("x"));
            y = Float.parseFloat(data.get("y"));
            r = Integer.parseInt(data.get("r"));
            return (-2 <= x && x <= 2) && (-5 <= y && y <= 5) && (1 <= r && r <= 5);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean inZone() {
        return (x >= -r/2f && y <= r && x <= 0 && y >= 0) ||
                (x <= r && y >= x/2f - r/2f && x >= 0 && y <= 0) ||
                (x * x + y * y <= r*r && x <= 0 && y <= 0);
    }
}
