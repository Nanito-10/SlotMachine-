import java.util.ArrayList;
import java.util.List;

/**
 * Representa una rueda de la máquina tragamonedas.
 * La secuencia de símbolos es fija y NUNCA se reordena; lo único que cambia al girar es visibleIndex,
 * el puntero que indica cuál símbolo de esa secuencia queda alineado con
 * lo que se ve en ese momento
 * 
 * @author Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * @version 2.1
 */
public class Wheel {
    private List<Symbol> symbols;
    private int xPosition;
    private int yPosition;
    private boolean isVisible;
    private Rectangle fondo;
    private boolean isLocked;
    private int visibleIndex; // índice de "symbols" 

    /**
     * Constructor para inicializar una rueda vacía en una posición específica.
     * 
     * @param x Coordenada X inicial de la rueda en el lienzo.
     * @param y Coordenada Y inicial de la rueda en el lienzo.
     */
    public Wheel(int x, int y) {
        this.symbols = new ArrayList<>();
        this.xPosition = x;
        this.yPosition = y;
        this.isVisible = false;

        // Creamos la ventana visual blanca: un cuadro (no una columna larga),
        // ya que en una tragamonedas real cada rueda muestra UN solo símbolo.
        this.fondo = new Rectangle();
        this.fondo.changeSize(50, 50); // Alto, Ancho osea una ventana cuadrada
        this.fondo.changeColor("white");
        this.fondo.moveHorizontal(x - 70); // Ajuste desde la posición original de Rectangle
        this.fondo.moveVertical(y - 15);
        this.isLocked = false;
        this.visibleIndex = 0;
    }

    /**
     * Cumplimos el requisito de fijar una rueda en este caso cuando la fijemos pasara a un estado true para que las
     * verificaciones sean mas faciles y solo sea chequear el estado de esa rueda (lock o no)
     */
    public void lock() {
        this.isLocked = true;
    }

    /**
     * Aqui simplemente pasamos a un estado false si esta fijada osea soltaremos esa rueda.
     */
    public void unlock() {
        this.isLocked = false;
    }

    public boolean isLocked() {
        return this.isLocked;
    }

    /**
     * Reubica la rueda completa (ventana + símbolo visible) 
     * porque al insertar/eliminar ruedas en medio de la máquina,
     * las ruedas restantes deben desplazarse horizontalmente.
     *
     * @param newX Nueva coordenada X.
     * @param newY Nueva coordenada Y.
     */
    public void reposition(int newX, int newY) {
        int deltaX = newX - this.xPosition;
        int deltaY = newY - this.yPosition;
        this.xPosition = newX;
        this.yPosition = newY;
        if (deltaX != 0 || deltaY != 0) {
            this.fondo.moveHorizontal(deltaX);
            this.fondo.moveVertical(deltaY);
        }
        updateVisuals();
    }

    /**
     * Ajusta la posición solicitada por el usuario a un índice válido.
     * 
     * @param pos Posición solicitada (empezando en 1).
     * @return Índice ajustado de 0 al tamaño de la lista.
     */
    private int adjustPosition(int pos) {
        if (pos < 1) return 0;
        if (pos > symbols.size()) return symbols.size();
        return pos - 1;
    }

    /**
     * Agrega un símbolo garantizando que su color no exista antes.
     * Reinicia visibleIndex a 0, porque insertar cambia los índices de la
     * lista y el puntero anterior podría quedar apuntando a otro símbolo
     * distinto al que se veía antes de la inserción.
     * 
     * @param pos Posición donde se desea insertar (1 a N).
     * @param color Color del nuevo símbolo a agregar.
     * @return true si se agregó correctamente, false si el color ya existía.
     */
    public boolean addSymbol(int pos, String color) {
        for (Symbol s : symbols) {
            if (s.getColor().equalsIgnoreCase(color)) return false;
        }
        Symbol newSymbol = new Symbol(color);
        symbols.add(adjustPosition(pos), newSymbol);
        this.visibleIndex = 0;
        updateVisuals();
        return true;
    }

    /**
     * Elimina el símbolo que coincida con el color especificado.
     * Si el puntero visibleIndex queda fuera de rango tras la eliminación,
     * se reinicia a 0 para evitar un índice inválido.
     * 
     * @param color Color del símbolo a quitar.
     * @return true si se encontró y eliminó, false en caso contrario.
     */
    public boolean delSymbol(String color) {
        for (int i = 0; i < symbols.size(); i++) {
            if (symbols.get(i).getColor().equalsIgnoreCase(color)) {
                symbols.remove(i).makeInvisible();
                if (this.visibleIndex >= symbols.size()) {
                    this.visibleIndex = 0;
                }
                updateVisuals();
                return true;
            }
        }
        return false;
    }

    /**
     * Gira la rueda avanzando el puntero visibleIndex una posición, de forma
     * circular. La lista de símbolos NUNCA se reordena — solo cambia cuál
     * símbolo queda alineado con la ventana visible.
     */
    public void spin() {
        if (symbols.isEmpty()) return;
        visibleIndex = (visibleIndex + 1) % symbols.size();
        updateVisuals();
    }

    /**
     * Gira la rueda internamente hasta que el color deseado quede visible arriba.
     * 
     * @param color Color objetivo a poner.
     * @return true si el símbolo existe y se ubicó, false si no existe.
     */
    public boolean placeSymbol(String color) {
        boolean exists = symbols.stream().anyMatch(s -> s.getColor().equalsIgnoreCase(color));
        if (!exists) return false;

        while (!getVisibleColor().equalsIgnoreCase(color)) {
            spin();
        }
        return true;
    }

    /**
     * Obtiene el color del símbolo que está alineado con la ventana visible
     * (el símbolo en la posición visibleIndex de la secuencia fija).
     * 
     * @return Color visible, o cadena vacía si no hay símbolos.
     */
    public String getVisibleColor() {
        if (symbols.isEmpty()) return "";
        return symbols.get(visibleIndex).getColor();
    }

    /**
     * Obtiene un arreglo con los colores de todos los símbolos en la rueda,
     * en su orden fijo original (nunca cambia, sin importar cuántos spin()
     * se hayan hecho).
     * 
     * @return Arreglo de cadenas ordenado según la secuencia fija.
     */
    public String[] getSymbols() {
        String[] colors = new String[symbols.size()];
        for (int i = 0; i < symbols.size(); i++) {
            colors[i] = symbols.get(i).getColor();
        }
        return colors;
    }

    /**
     * Actualiza cómo se ve la rueda en pantalla.
     * Solo el símbolo en la posición visibleIndex (el que está alineado con
     * la ventana en este momento) se muestra, centrado dentro de la ventana
     * cuadrada. El resto de símbolos de la rueda permanecen ocultos.
     */
    private void updateVisuals() {
        for (int i = 0; i < symbols.size(); i++) {
            Symbol s = symbols.get(i);
            if (i == visibleIndex) {
                s.setPosition(this.xPosition + 10, this.yPosition + 10); // Centrado en la ventana 50x50
                if (this.isVisible) s.makeVisible();
            } else {
                s.makeInvisible();
            }
        }
    }

    /**
     * Despliega la rueda en pantalla.
     */
    public void makeVisible() {
        this.isVisible = true;
        this.fondo.makeVisible(); // Mostramos la ventana blanca
        updateVisuals();
    }

    /**
     * Oculta la rueda de la pantalla.
     */
    public void makeInvisible() {
        this.isVisible = false;
        this.fondo.makeInvisible();
        for (Symbol s : symbols) {
            s.makeInvisible();
        }
    }
}