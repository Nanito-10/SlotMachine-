/**
 * Representa un símbolo dentro de una rueda de la máquina tragamonedas.
 * reutilizamos el paquete shapes.
 * Es la superclase de los símbolos especiales (EphemeralSymbol, ShySymbol);
 * ella misma representa el tipo "normal".
 * 
 * @author Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * @version 2.0
 */
public class Symbol {
    private String color;
    private Circle figure;
    private int xPosition;
    private int yPosition;
    private boolean isVisible;

    /**
     * Constructor para crear simbolos.
     * 
     * @param color El nombre del color del símbolo
     */
    public Symbol(String color) {
        this.color = color;
        this.figure = new Circle();
        this.figure.changeColor(color);
        this.figure.changeSize(30);
        this.xPosition = 20; // Posición inicial por defecto que aparecia en la clase ya creada en shapes de cirle
        this.yPosition = 15;
        this.isVisible = false;
    }

    /**
     * Obtiene el color representativo de este símbolo.
     * 
     * @return Cadena de texto con el color del símbolo.
     */
    public String getColor() {
        return this.color;
    }

    /**
     * Indica si el símbolo está actualmente mostrándose en pantalla.
     * Útil para probar subclases como ShySymbol, que alternan este estado.
     * 
     * @return true si está visible en este momento.
     */
    public boolean isShowing() {
        return this.isVisible;
    }

    /**
     * Gancho que Wheel.spin() llama sobre TODOS sus símbolos cada vez que
     * gira (estén visibles o no). El símbolo normal no hace nada especial;
     * las subclases como EphemeralSymbol lo sobrescriben para reaccionar.
     */
    protected void onWheelSpin() {
        // Comportamiento normal: no reacciona al giro.
    }

    /**
     * Gancho que Wheel.advance() llama sobre el símbolo que QUEDA seleccionado
     * (alineado con la ventana) tras cada paso de giro. El símbolo normal no
     * hace nada; ShySymbol lo sobrescribe para alternar su visibilidad.
     */
    protected void onSelected() {
        // Comportamiento normal: no reacciona a ser seleccionado.
    }

    /**
     * Permite que las subclases cambien el tamaño de la figura sin tener
     * acceso directo al campo privado figura.
     * 
     * @param diameter Nuevo diámetro en píxeles.
     */
    protected void resizeFigure(int diameter) {
        this.figure.changeSize(diameter);
    }

    /**
     * Actualiza la posición geométrica del símbolo en el Canvas.
     * Calcula la diferencia de movimiento respecto a su posición actual.
     * 
     * @param x Nueva coordenada X.
     * @param y Nueva coordenada Y.
     */
    public void setPosition(int x, int y) {
        int deltaX = x - this.xPosition;
        int deltaY = y - this.yPosition;
        this.xPosition = x;
        this.yPosition = y;
        this.figure.moveHorizontal(deltaX);
        this.figure.moveVertical(deltaY);
    }

    /**
     * Hace que la gráfica del símbolo sea visible en pantalla.
     */
    public void makeVisible() {
        this.isVisible = true;
        this.figure.makeVisible();
    }

    /**
     * Oculta la representación gráfica del símbolo de la pantalla.
     */
    public void makeInvisible() {
        this.isVisible = false;
        this.figure.makeInvisible();
    }

}