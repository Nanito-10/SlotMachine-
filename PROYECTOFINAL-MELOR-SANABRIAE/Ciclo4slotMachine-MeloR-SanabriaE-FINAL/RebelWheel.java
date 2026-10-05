/**
 * Rueda especial : no se deja fijar (lock), intercambiar
 * (swap) ni eliminar (delWheel). SlotMachine verifica isRebel() antes de
 * permitir cualquiera de esas tres operaciones y rechaza con reportError
 * si la rueda es rebelde.
 * 
 * @author Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * @version 1.0
 */
public class RebelWheel extends Wheel {

    /**
     * Crea una rueda rebelde en la posición indicada.
     * 
     * @param x Coordenada X inicial.
     * @param y Coordenada Y inicial.
     */
    public RebelWheel(int x, int y) {
        super(x, y);
        setWindowColor("red"); // distinción visual del tipo
    }

    @Override
    public boolean isRebel() {
        return true;
    }
}