/**
 * Rueda especial : si tiene una rueda a su izquierda, al girar
 * copia exactamente el símbolo que esa vecina muestra en ese momento, en vez
 * de avanzar por su propia cuenta. Si no tiene vecina a la izquierda (es la
 * primera rueda de la máquina), se comporta como una rueda normal.
 * 
 * @author Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * @version 1.0
 */
public class LeftyWheel extends Wheel {

    /**
     * Crea una rueda lefty en la posición indicada.
     * 
     * @param x Coordenada X inicial.
     * @param y Coordenada Y inicial.
     */
    public LeftyWheel(int x, int y) {
        super(x, y);
        setWindowColor("blue"); // distinción de como se ve el tipo
    }

    /**
     * Si hay vecina a la izquierda, copia su símbolo visible en vez de
     * avanzar normalmente. Sin vecina, se comporta como una rueda normal.
     */
    @Override
    public void spin() {
        if (leftNeighbor == null) {
            advance();
            return;
        }
        this.placeSymbol(leftNeighbor.getVisibleColor());
    }
}