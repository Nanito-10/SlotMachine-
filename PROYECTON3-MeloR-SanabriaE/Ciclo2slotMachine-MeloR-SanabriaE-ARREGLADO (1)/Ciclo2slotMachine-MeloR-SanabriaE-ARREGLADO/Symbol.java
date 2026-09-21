/**
 * Representa un símbolo dentro de una rueda de la máquina tragamonedas.
 * reutilizamos el paquete shapes.
 * 
 * @author Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * @version 1.0
 */
public class Symbol {
    private String color;
    private Circle figura;
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
        this.figura = new Circle();
        this.figura.changeColor(color);
        this.figura.changeSize(30);
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
        this.figura.moveHorizontal(deltaX);
        this.figura.moveVertical(deltaY);
    }

    /**
     * Hace que la gráfica del símbolo sea visible en pantalla.
     */
    public void makeVisible() {
        this.isVisible = true;
        this.figura.makeVisible();
    }

    /**
     * Oculta la representación gráfica del símbolo de la pantalla.
     */
    public void makeInvisible() {
        this.isVisible = false;
        this.figura.makeInvisible();
    }

}