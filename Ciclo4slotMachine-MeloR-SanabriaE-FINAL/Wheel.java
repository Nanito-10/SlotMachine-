import java.util.ArrayList;
import java.util.List;

/**
 * Representa una rueda de la máquina tragamonedas.
 * La secuencia de símbolos es fija y nb se reordena; lo único que cambia al girar es visibleIndex,
 * el puntero que indica cuál símbolo de esa secuencia queda alineado con
 * lo que se ve en ese momento.
 * Es la superclase de las ruedas especiales (LeftyWheel, RebelWheel,
 * RainbowWheel); ella misma representa el tipo "normal".
 * 
 * @author Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * @version 3.0
 */
public class Wheel {
    private List<Symbol> symbols;
    private int xPosition;
    private int yPosition;
    private boolean isVisible;
    private Rectangle backgroundWindow;
    private boolean isLocked;
    private int visibleIndex; // índice de "symbols" alineado con la ventana visible
    protected Wheel leftNeighbor; // la rueda inmediatamente a su izquierda, o null
    protected List<Wheel> machineWheels; // vista de TODAS las ruedas de la máquina (incluida esta)

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
        this.backgroundWindow = new Rectangle();
        this.backgroundWindow.changeSize(50, 50); // Alto, Ancho osea una ventana cuadrada
        this.backgroundWindow.changeColor("white");
        this.backgroundWindow.moveHorizontal(x - 70); // Ajuste desde la posición original de Rectangle
        this.backgroundWindow.moveVertical(y - 15);
        this.isLocked = false;
        this.visibleIndex = 0;
        this.leftNeighbor = null;
        this.machineWheels = new ArrayList<>();
    }

    /**
     * Permite que las subclases cambien el color de la ventana para
     * distinguirse visualmente del tipo normal (requisito de usabilidad).
     * 
     * @param color Nuevo color de la ventana.
     */
    protected void setWindowColor(String color) {
        this.backgroundWindow.changeColor(color);
    }

    /**
     * Actualiza quién es la rueda inmediatamente a la izquierda de esta.
     * SlotMachine lo llama cada vez que el orden de las ruedas cambia.
     * 
     * @param neighbor La rueda a la izquierda, o null si es la primera.
     */
    public void setLeftNeighbor(Wheel neighbor) {
        this.leftNeighbor = neighbor;
    }

    /**
     * Actualiza la lista de TODAS las ruedas de la máquina (incluida esta).
     * SlotMachine lo llama cada vez que el conjunto u orden de ruedas cambia.
     * Permite que ruedas como RainbowWheel observen a las demás sin que
     * SlotMachine tenga que conocer su tipo concreto.
     * 
     * @param all Ruedas de la máquina, de izquierda a derecha.
     */
    public void setMachineWheels(List<Wheel> all) {
        this.machineWheels = all;
    }

    /**
     * Indica si esta rueda es de tipo rebelde (no se deja fijar, intercambiar
     * ni eliminar). Por defecto false; RebelWheel lo sobrescribe a true.
     * 
     * @return true si es rebelde.
     */
    public boolean isRebel() {
        return false;
    }

    /**
     *  fijar una rueda en este caso cuando la fijemos pasara a un estado true para que las
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
            this.backgroundWindow.moveHorizontal(deltaX);
            this.backgroundWindow.moveVertical(deltaY);
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
     * Agrega un símbolo de tipo normal. Delega en la versión con tipo.
     * 
     * @param pos Posición donde se desea insertar (1 a N).
     * @param color Color del nuevo símbolo a agregar.
     * @return true si se agregó correctamente, false si el color ya existía.
     */
    public boolean addSymbol(int pos, String color) {
        return addSymbol("normal", pos, color);
    }

    /**
     * Agrega un símbolo del tipo indicado ("normal", "ephemeral", "shy"),
     * garantizando que su color no exista antes. Reinicia visibleIndex a 0,
     * porque insertar cambia los índices de la lista.
     * 
     * @param type Tipo de símbolo a crear.
     * @param pos Posición donde se desea insertar (1 a N).
     * @param color Color del nuevo símbolo a agregar.
     * @return true si se agregó correctamente, false si el color ya existía.
     */
    public boolean addSymbol(String type, int pos, String color) {
        for (Symbol s : symbols) {
            if (s.getColor().equalsIgnoreCase(color)) return false;
        }
        Symbol newSymbol = createSymbol(type, color);
        symbols.add(adjustPosition(pos), newSymbol);
        this.visibleIndex = 0;
        updateVisuals();
        return true;
    }

    /**
     * crea la subclase de Symbol correspondiente al tipo.
     * Un tipo no reconocido crea un símbolo normal.
     * 
     * @param type Tipo deseado.
     * @param color Color del símbolo.
     * @return El símbolo creado.
     */
    private Symbol createSymbol(String type, String color) {
        if (type.equalsIgnoreCase("ephemeral")) return new EphemeralSymbol(color);
        if (type.equalsIgnoreCase("shy")) return new ShySymbol(color);
        return new Symbol(color);
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
     * Gira la rueda. Es el punto de extensión del polimorfismo: las
     * subclases (LeftyWheel, RainbowWheel) lo sobrescriben para cambiar
     * QUÉ pasa al girar. Por defecto avanza un paso (advance()).
     */
    public void spin() {
        advance();
    }

    /**
     * Mecánica básica e invariable de girar UN paso: avanza el puntero
     * visibleIndex de forma circular. La lista de símbolos NUNCA se
     * reordena. Antes de avanzar avisa a TODOS los símbolos (visibles o
     * no) para que los ephemeral puedan encogerse.
     * Es protected y NO polimórfico a propósito: placeSymbol() y las
     * subclases lo usan para mover la rueda sin volver a disparar el
     * comportamiento especial de spin() (evita recursión infinita).
     */
    protected void advance() {
        if (symbols.isEmpty()) return;
        for (Symbol s : symbols) {
            s.onWheelSpin();
        }
        visibleIndex = (visibleIndex + 1) % symbols.size();
        symbols.get(visibleIndex).onSelected(); // el símbolo shy alterna aquí, UNA vez por selección
        updateVisuals();
    }

    /**
     * Gira la rueda internamente (con advance(), no con spin()) hasta que
     * el color deseado quede visible arriba.
     * 
     * @param color Color objetivo a poner.
     * @return true si el símbolo existe y se ubicó, false si no existe.
     */
    public boolean placeSymbol(String color) {
        boolean exists = symbols.stream().anyMatch(s -> s.getColor().equalsIgnoreCase(color));
        if (!exists) return false;

        while (!getVisibleColor().equalsIgnoreCase(color)) {
            advance(); // NO spin(): spin() es polimórfico y volvería a llamar a placeSymbol
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
        this.backgroundWindow.makeVisible(); // Mostramos la ventana blanca
        updateVisuals();
    }

    /**
     * Oculta la rueda de la pantalla.
     */
    public void makeInvisible() {
        this.isVisible = false;
        this.backgroundWindow.makeInvisible();
        for (Symbol s : symbols) {
            s.makeInvisible();
        }
    }
}