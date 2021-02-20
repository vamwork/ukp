package ukp;

import java.util.Vector;

// Класс-контейнер для текущих значений считанных величин счётчика
public class Unit {
    // контроль "уровней"

    public static final int STATE_ZERRO = 0; // ноль

    public static final int STATE_NOREC = 1; // не опрашивалось

    public static final int STATE_NONE = 2; // состояние ещё не определялось

    public static final int STATE_NORMAL = 3; // нормальное

    public static final int STATE_MIN = 4; // выше максимального

    public static final int STATE_MAX = 5; // меньше минимального

    public float value;

    public int state;

    public byte parNumber;

    public Vector values = new Vector();

    /**
     * Creates a new instance of Unit
     */
    public Unit() {
        value = 0;
        state = STATE_NOREC;
    }

    public float GetAverageValue() {
        float result = 0;
        if (values.size() > 0) {
            for (int i = 0; i < values.size(); i++) {
                result = result + ((Float) values.elementAt(i)).floatValue();
            }
            result = result / values.size();
        }
        return result;
    }

    public void AddValue(float val) {
        values.addElement(new Float(val));
        value = val;
        if (values.size() > 20) {
            values.removeElementAt(0);
        }
    }

    public void ClearAverageArray() {
        values.removeAllElements();
    }

}
