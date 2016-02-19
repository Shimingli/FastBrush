package co.adrianblan.calligraphy;

/**
 * Class which encapsulates touch data.
 */
public class TouchData {
    private Vector2 position;
    private float size;
    private float pressure;

    public TouchData(Vector2 position, float size, float pressure) {
        this.position = position;
        this.pressure = pressure;

        // The touch size is always 0.0 on an emulator
        if(!Utils.floatsAreEquivalent(size, 0f)) {
            this.size = size;
        } else {
            this.size = 1.0f;
        }
    }

    public TouchData(float x, float y, float size, float pressure) {
        this.position = new Vector2(x, y);
        this.pressure = pressure;

        // The touch size is always 0.0 on an emulator
        if(!Utils.floatsAreEquivalent(size, 0f)) {
            this.size = size;
        } else {
            this.size = 1.0f;
        }
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public float getX() {
        return position.getX();
    }

    public float getY() {
        return position.getY();
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    /** Gets the normalized pressure in proportion to screen size, 1f is roughly normal */
    public float getNormalizedPressure() {

        if(Utils.floatsAreEquivalent(pressure, 1.0f)) {
            return 1.0f;
        } else {
            return pressure / (pressure * 10f);
        }
    }

    /** Takes a touch size and returns the normalized size [0, 1] */
    public float getNormalizedTouchSize() {

        float TOUCH_SIZE_MIN = 0.2f; // The minimum touch size treshold
        float TOUCH_SIZE_MAX = 0.4f; // The maximum touch size treshold

        return Utils.normalize(size * getNormalizedPressure(), TOUCH_SIZE_MIN, TOUCH_SIZE_MAX);
    }
}