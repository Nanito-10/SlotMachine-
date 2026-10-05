/**
 * Símbolo especial : en cada giro de su rueda (sin importar
 * si está visible o no en ese momento) va reduciendo su tamaño, hasta
 * quedar como un punto (tamaño mínimo).
 * 
 * @author Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * @version 1.0
 */
public class EphemeralSymbol extends Symbol {
    private static final int MIN_SIZE = 2;
    private static final int DECREMENT = 4;
    private int currentSize = 30;

    /**
     * Crea un símbolo ephemeral del color indicado, con el tamaño normal
     * inicial (30), que irá disminuyendo con cada giro de su rueda.
     * 
     * @param color Color del símbolo.
     */
    public EphemeralSymbol(String color) {
        super(color);
    }

    /**
     * Reduce el tamaño del símbolo cada vez que su rueda gira, hasta llegar
     * al mínimo (queda como un punto) y no seguir reduciendo después.
     */
    @Override
    protected void onWheelSpin() {
        if (currentSize > MIN_SIZE) {
            currentSize = Math.max(MIN_SIZE, currentSize - DECREMENT);
            resizeFigure(currentSize);
        }
    }

    /**
     * Consulta el tamaño actual del símbolo. principalmente para
     * poder verificarlo en las pruebas de unidad.
     * 
     * @return El diámetro actual en píxeles.
     */
    public int getCurrentSize() {
        return currentSize;
    }
}