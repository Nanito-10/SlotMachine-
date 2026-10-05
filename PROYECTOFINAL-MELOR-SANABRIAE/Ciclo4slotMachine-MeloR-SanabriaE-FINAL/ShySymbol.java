/**
 * Símbolo especial: alterna su estado de visible a invisible
 * cada vez que es seleccionado en la rueda, es decir, cada vez que un giro
 * lo deja alineado con la ventana (Wheel.advance() llama onSelected()).
 * Empieza visible: la primera vez que es seleccionado se oculta, la
 * segunda se muestra, y así sucesivamente. Mientras está "oculto" y es el
 * símbolo seleccionado, la ventana de la rueda se ve vacía.
 * 
 * @author Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * @version 2.0
 */
public class ShySymbol extends Symbol {
    private boolean shown = true;

    /**
     * Crea un símbolo shy del color indicado, en estado visible.
     * 
     * @param color Color del símbolo.
     */
    public ShySymbol(String color) {
        super(color);
    }

    /**
     * Cada vez que el símbolo es seleccionado en la rueda, alterna entre
     * visible e invisible.
     */
    @Override
    protected void onSelected() {
        shown = !shown;
    }

    /**
     * Indica si, cuando le toque mostrarse, este símbolo debe verse (true)
     * o permanecer oculto (false).
     * 
     * @return true si su estado actual es "visible".
     */
    public boolean isShown() {
        return shown;
    }

    /**
     * Solo se dibuja si su estado actual es visible; si está en estado
     * "oculto" permanece invisible aunque su rueda lo haya seleccionado.
     */
    @Override
    public void makeVisible() {
        if (shown) {
            super.makeVisible();
        } else {
            super.makeInvisible();
        }
    }
}
