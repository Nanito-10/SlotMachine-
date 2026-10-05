import java.util.HashMap;
import java.util.Map;

/**
 * Rueda especial propuesta por nosotros . Al girar, en vez de
 * avanzar por su cuenta, adopta el símbolo MÁS FRECUENTE entre las demás
 * ruedas de la máquina ("se une a la mayoría"). En caso de empate gana el
 * símbolo de la rueda más a la izquierda. Si no hay otras ruedas se
 * comporta como una rueda normal.
 * 
 * Se diferencia de LeftyWheel en que observa a TODAS las ruedas (no solo a
 * su vecina izquierda) y de placeSymbol en que el usuario no elige el
 * color: lo decide la rueda al girar. Todo vive dentro de spin(), por lo
 * que SlotMachine no necesita conocer su tipo osea aplicamos(polimorfismo).
 * 
 * @author Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * @version 2.0
 */
public class RainbowWheel extends Wheel {

    /**
     * Crea una rueda rainbow en la posición indicada.
     * 
     * @param x Coordenada X inicial.
     * @param y Coordenada Y inicial.
     */
    public RainbowWheel(int x, int y) {
        super(x, y);
        setWindowColor("green"); // distinción visual del tipo
    }

    /**
     * Copia el símbolo visible más frecuente entre las demás ruedas. Sin
     * otras ruedas, se comporta como una rueda normal.
     */
    @Override
    public void spin() {
        String popular = mostFrequentColorOfOthers();
        if (popular == null) {
            advance();
            return;
        }
        this.placeSymbol(popular);
    }

    /**
     * Calcula el color visible más frecuente entre las otras ruedas.
     * 
     * @return El color, o null si no hay otras ruedas con símbolos.
     */
    private String mostFrequentColorOfOthers() {
        Map<String, Integer> counts = new HashMap<>();
        String best = null;
        int bestCount = 0;
        for (Wheel w : machineWheels) {
            if (w == this) continue;
            String c = w.getVisibleColor();
            if (c.isEmpty()) continue;
            int n = counts.merge(c, 1, Integer::sum);
            if (n > bestCount) {
                bestCount = n;
                best = c;
            }
        }
        return best;
    }
}
