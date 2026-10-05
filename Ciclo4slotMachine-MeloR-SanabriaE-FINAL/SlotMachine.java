import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.Arrays;
import java.util.Random;

/**
 * Clase principal que simula una máquina tragamonedas (Slot Machine).
 *TODAS las ruedas comparten siempre la misma
 *secuencia de símbolos y en el mismo orden (como en una tragamonedas real,
 * donde todos los rodillos tienen impresa la misma tira de figuras).
 * Ahora pusimos a, Wheel y Symbol como superclases con herencia
 * especiales (LeftyWheel, RebelWheel, RainbowWheel, EphemeralSymbol,
 * ShySymbol)
 * 
 * @author Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * @version 3.0
 */
public class SlotMachine {
    private List<Wheel> wheels;
    private boolean isVisible;
    private boolean okState;
    private Rectangle background;
    private static final Random contestRng = new Random();

    /**
     * Inicializa una máquina tragamonedas vacía y oculta por defecto.
     */
    public SlotMachine() {
        this.wheels = new ArrayList<>();
        this.isVisible = false;
        this.okState = true;
        this.background = new Rectangle();
        this.background.changeSize(300, 480);
        this.background.changeColor("magenta");
        this.background.moveHorizontal(10 - 70);
        this.background.moveVertical(10 - 15);
    }

    /**
     * Crea una máquina de n ruedas y n símbolos, inicializada aleatoriamente:
     * la secuencia compartida de símbolos queda en un orden aleatorio, y cada
     * rueda arranca apuntando a una posición inicial también aleatoria e
     * independiente de las demás. Requisito 13: igual número de ruedas y símbolos.
     * Usada como "testing tool" de la maratón: nunca debe hacerse visible
     * mientras resuelve, solo al simular la solución ya encontrada.
     * 
     * @param n Número de ruedas y de símbolos (deben ser iguales).
     */
    public SlotMachine(int n) {
        this();

        String[] realColors = {"red", "blue", "green", "yellow", "magenta", "black"};
        List<String> sequence = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            sequence.add(i < realColors.length ? realColors[i] : "s" + i);
        }
        Collections.shuffle(sequence, contestRng);

        for (int i = 0; i < n; i++) addWheel(i + 1);
        for (String symbolColor : sequence) addSymbol(1, symbolColor);

        do {
            for (Wheel w : wheels) {
                int steps = contestRng.nextInt(n);
                for (int p = 0; p < steps; p++) w.spin();
            }
        } while (isJackpot());
    }

    /**
     * Revisamos si hay ruedas en nuestra maquina, sino mandamos error.
     * Las ruedas rebeldes no se dejan fijar.
     * 
     * @param wheel Número de la rueda a fijar (iniciando en 1).
     */
    public void lock(int wheel) {
        if (wheels.isEmpty()) {
            reportError("No hay ruedas disponibles.");
            return;
        }
        Wheel w = wheels.get(adjustWheelPosition(wheel));
        if (w.isRebel()) {
            reportError("La rueda es rebelde y no se deja fijar.");
            return;
        }
        w.lock();
        this.okState = true;
    }

    /**
     * Revisamos si hay ruedas en nuestra maquina, sino mandamos error.
     * En otro caso desbloqueamos dicha rueda y pasamos a verificar el estado unlock.
     * 
     * @param wheel Número de la rueda a soltar (iniciando en 1).
     */
    public void unlock(int wheel) {
        if (wheels.isEmpty()) {
            reportError("No hay ruedas disponibles.");
            return;
        }
        wheels.get(adjustWheelPosition(wheel)).unlock();
        this.okState = true;
    }

    /**
     * Intercambia la posición de dos ruedas de la máquina.
     * Ninguna de las dos puede ser rebelde.
     * 
     * @param wheel1 Número de la primera rueda recordemos que(iniciando en 1).
     * @param wheel2 Número de la segunda rueda recordemos que(iniciando en 1).
     */
    public void swap(int wheel1, int wheel2) {
        if (wheels.size() < 2) {
            reportError("Se requieren al menos dos ruedas para intercambiar.");
            return;
        }
        int i1 = adjustWheelPosition(wheel1);
        int i2 = adjustWheelPosition(wheel2);
        if (wheels.get(i1).isRebel() || wheels.get(i2).isRebel()) {
            reportError("Una rueda rebelde no se deja intercambiar.");
            return;
        }
        Collections.swap(wheels, i1, i2);
        recalcWheelPositions();
        this.okState = true;
        checkJackpotVisuals();
    }

    /**
     * Gira una rueda específica un número determinado de pasos.
     * Si la rueda está visible, cada paso se muestra individualmente.
     * 
     * @param wheel Número de la rueda a girar (iniciando en 1).
     * @param steps Cantidad de pasos a girar.
     */
    public void spin(int wheel, int steps) {
        if (wheels.isEmpty()) {
            reportError("No hay ruedas para girar.");
            return;
        }
        Wheel w = wheels.get(adjustWheelPosition(wheel));
        if (w.isLocked()) {
            reportError("La rueda está fija y no puede girar.");
            return;
        }
        for (int i = 0; i < steps; i++) {
            w.spin();
        }
        this.okState = true;
        checkJackpotVisuals();
    }

    /**
     * Deja la máquina mostrando exactamente la configuración de colores dada.
     * La operación es atómica: si algún símbolo no existe en su rueda, o si una
     * rueda bloqueada no puede alcanzar el color pedido, no se mueve ninguna rueda.
     * 
     * @param setSymbols Arreglo con el color deseado para cada rueda, de izquierda a derecha.
     */
    public void spin(String[] setSymbols) {
        if (setSymbols.length != wheels.size()) {
            reportError("El número de símbolos no coincide con el número de ruedas.");
            return;
        }
        for (int i = 0; i < wheels.size(); i++) {
            Wheel w = wheels.get(i);
            String target = setSymbols[i];
            boolean existsInWheel = Arrays.asList(w.getSymbols())
                                           .stream()
                                           .anyMatch(c -> c.equalsIgnoreCase(target));
            if (!existsInWheel) {
                reportError("El símbolo '" + target + "' no existe en la rueda " + (i + 1) + ".");
                return;
            }
            if (w.isLocked() && !w.getVisibleColor().equalsIgnoreCase(target)) {
                reportError("La rueda " + (i + 1) + " está fija y no puede cambiar a '" + target + "'.");
                return;
            }
        }
        for (int i = 0; i < wheels.size(); i++) {
            wheels.get(i).placeSymbol(setSymbols[i]);
        }
        this.okState = true;
        checkJackpotVisuals();
    }

    /**
     * Gira una sola rueda un espacio hacia abajo.
     * 
     * @param wheel Número de la rueda a girar.
     */
    public void spin(int wheel) {
        if (wheels.isEmpty()) {
            reportError("No hay ruedas para girar.");
            return;
        }
        Wheel w = wheels.get(adjustWheelPosition(wheel));
        if (w.isLocked()) {
            reportError("La rueda está fija y no puede girar.");
            return;
        }
        w.spin();
        this.okState = true;
        checkJackpotVisuals();
    }

    /**
     * Gira todas las ruedas no bloqueadas un espacio hacia abajo.
     * Las ruedas fijas (locked) se omiten; las demás giran normalmente.
     */
    public void spin() {
        if (wheels.isEmpty()) {
            reportError("La máquina está vacía.");
            return;
        }
        boolean anyMoved = false;
        for (Wheel w : wheels) {
            if (!w.isLocked()) {
                w.spin();
                anyMoved = true;
            }
        }
        if (!anyMoved) {
            reportError("Todas las ruedas están fijas.");
            return;
        }
        this.okState = true;
        checkJackpotVisuals();
    }

    /**
     * Copia el símbolo visible de otra rueda hacia una rueda de tipo
     * rainbow. Requisito 19.
     * 
     * @param wheel Número de la rueda rainbow destino (iniciando en 1).
     * @param sourceWheel Número de la rueda fuente a copiar (iniciando en 1).
     */
    public void rainbowCopy(int wheel, int sourceWheel) {
        if (wheels.isEmpty()) {
            reportError("No hay ruedas disponibles.");
            return;
        }
        Wheel target = wheels.get(adjustWheelPosition(wheel));
        if (!(target instanceof RainbowWheel)) {
            reportError("Esa rueda no es de tipo rainbow.");
            return;
        }
        Wheel source = wheels.get(adjustWheelPosition(sourceWheel));
        target.placeSymbol(source.getVisibleColor());
        this.okState = true;
        checkJackpotVisuals();
    }

    /**
     * Ajusta la posición de entrada a un índice de lista válido.
     * 
     * @param pos Posición de entrada.
     * @return Índice ajustado.
     */
    private int adjustWheelPosition(int pos) {
        if (pos < 1) return 0;
        if (pos > wheels.size()) return Math.max(0, wheels.size() - 1);
        return pos - 1;
    }

    /**
     * Maneja el estado de la aplicación e informa errores al usuario si es visible.
     * 
     * @param message Mensaje del fallo.
     */
    private void reportError(String message) {
        this.okState = false;
        if (this.isVisible) {
            JOptionPane.showMessageDialog(null, message, "Aviso - Slot Machine", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Recalcula y aplica la posición horizontal de TODAS las ruedas según su
     * orden actual en la lista, y refresca quién es la vecina izquierda de
     * cada una (hacemos esto para LeftyWheel) y la vista de todas las ruedas (para RainbowWheel). Se llama después de cualquier
     * addWheel/delWheel/swap, porque esas operaciones cambian el orden.
     */
    private void recalcWheelPositions() {
        for (int i = 0; i < wheels.size(); i++) {
            wheels.get(i).reposition(90 + (i * 60), 30);
        }
        updateLeftNeighbors();
        for (Wheel w : wheels) {
            w.setMachineWheels(wheels);
        }
    }

    /**
     * Informa a cada rueda quién es su vecina inmediata a la izquierda
     * (null para la primera rueda de la máquina).
     */
    private void updateLeftNeighbors() {
        for (int i = 0; i < wheels.size(); i++) {
            Wheel left = (i == 0) ? null : wheels.get(i - 1);
            wheels.get(i).setLeftNeighbor(left);
        }
    }

    /**
     * Verifica si hay condición ganadora y cambia el estado visual.
     */
    private void checkJackpotVisuals() {
        if (!isVisible) return;

        if (isJackpot()) {
            background.changeColor("yellow");
        } else {
            background.changeColor("magenta");
        }

        for (Wheel w : wheels) {
            w.makeVisible();
        }
    }

    /**
     * Agrega una rueda normal. Delega en la versión con tipo.
     * 
     * @param pos Número de la posición (iniciando en 1).
     */
    public void addWheel(int pos) {
        addWheel("normal", pos);
    }

    /**
     * Agrega una rueda del tipo indicado ("normal", "lefty", "rebel",
     * "rainbow") en la posición especificada. Si ya existen otras ruedas con
     * símbolos cargados, la rueda nueva recibe automáticamente la misma
     * secuencia de símbolos.
     * @param type Tipo de rueda a crear.
     * @param pos Número de la posición (iniciando en 1).
     */
    public void addWheel(String type, int pos) {
        int index = (wheels.isEmpty()) ? 0 : (pos > wheels.size() ? wheels.size() : adjustWheelPosition(pos));
        Wheel newWheel = createWheel(type, 90 + (index * 60), 30);

        if (!wheels.isEmpty()) {
            String[] sharedSymbols = wheels.get(0).getSymbols();
            for (int i = 0; i < sharedSymbols.length; i++) {
                newWheel.addSymbol(i + 1, sharedSymbols[i]);
            }
        }

        wheels.add(index, newWheel);
        recalcWheelPositions();
        if (isVisible) newWheel.makeVisible();

        this.okState = true;
        checkJackpotVisuals();
    }

    /**
     * Fábrica interna: crea la subclase de Wheel correspondiente al tipo.
     * Un tipo no reconocido crea una rueda normal.
     * 
     * @param type Tipo deseado.
     * @param x Coordenada X inicial.
     * @param y Coordenada Y inicial.
     * @return La rueda creada.
     */
    private Wheel createWheel(String type, int x, int y) {
        if (type.equalsIgnoreCase("lefty")) return new LeftyWheel(x, y);
        if (type.equalsIgnoreCase("rebel")) return new RebelWheel(x, y);
        if (type.equalsIgnoreCase("rainbow")) return new RainbowWheel(x, y);
        return new Wheel(x, y);
    }

    /**
     * Elimina la rueda ubicada en la posición ingresada.
     * Las ruedas rebeldes no se dejan eliminar.
     * 
     * @param pos Número de la posición de la rueda (iniciando en 1).
     */
    public void delWheel(int pos) {
        if (wheels.isEmpty()) {
            reportError("No hay ruedas registradas para eliminar.");
            return;
        }
        int idx = adjustWheelPosition(pos);
        if (wheels.get(idx).isRebel()) {
            reportError("La rueda es rebelde y no se deja eliminar.");
            return;
        }
        Wheel removed = wheels.remove(idx);
        removed.makeInvisible();
        recalcWheelPositions();
        this.okState = true;
        checkJackpotVisuals();
    }

    /**
     * Agrega un símbolo normal a la secuencia compartida. Delega en la
     * versión con tipo.
     * 
     * @param pos Posición en la secuencia (iniciando en 1).
     * @param color Color del símbolo a agregar.
     */
    public void addSymbol(int pos, String color) {
        addSymbol("normal", pos, color);
    }

    /**
     * Agrega un símbolo del tipo indicado ("normal", "ephemeral", "shy") a
     * la secuencia compartida por TODAS las ruedas de la máquina, en la
     * misma posición para cada una.
     * 
     * @param type Tipo de símbolo a crear.
     * @param pos Posición en la secuencia (iniciando en 1).
     * @param color Color CSS del símbolo a agregar.
     */
    public void addSymbol(String type, int pos, String color) {
        if (wheels.isEmpty()) {
            reportError("Debe agregar una rueda primero.");
            return;
        }
        boolean existsAlready = Arrays.asList(wheels.get(0).getSymbols())
                                       .stream()
                                       .anyMatch(c -> c.equalsIgnoreCase(color));
        if (existsAlready) {
            reportError("El color ya existe en la secuencia de símbolos.");
            return;
        }
        for (Wheel w : wheels) {
            w.addSymbol(type, pos, color);
        }
        this.okState = true;
        checkJackpotVisuals();
    }

    /**
     * Elimina la primera ocurrencia de un símbolo en todas las ruedas.
     * 
     * @param symbol Color del símbolo a remover.
     */
    public void delSymbol(String symbol) {
        boolean found = false;
        for (Wheel w : wheels) {
            if (w.delSymbol(symbol)) found = true;
        }

        if (!found) reportError("Símbolo no encontrado en las ruedas.");
        else {
            this.okState = true;
            checkJackpotVisuals();
        }
    }

    /**
     * Gira una rueda hasta posicionar el símbolo indicado en la zona visible.
     * 
     * @param wheel Número de la rueda a interactuar.
     * @param symbol Color del símbolo objetivo.
     */
    public void placeSymbol(int wheel, String symbol) {
        if (wheels.isEmpty()) {
            reportError("No hay ruedas disponibles.");
            return;
        }
        boolean success = wheels.get(adjustWheelPosition(wheel)).placeSymbol(symbol);
        if (!success) reportError("Símbolo no existe en esa rueda.");
        else {
            this.okState = true;
            checkJackpotVisuals();
        }
    }

    /**
     * Consulta los símbolos cargados en la primera rueda, en su orden fijo
     * (no cambia con los giros).
     * 
     * @return Arreglo de colores en orden de la rueda 1.
     */
    public String[] symbols() {
        this.okState = true;
        if (wheels.isEmpty()) return new String[0];
        return wheels.get(0).getSymbols();
    }

    /**
     * Consulta el número total de colores distintos visibles actualmente.
     * 
     * @return Cantidad de símbolos únicos.
     */
    public int distinctSymbols() {
        Set<String> distinct = new HashSet<>();
        for (Wheel w : wheels) {
            String color = w.getVisibleColor();
            if (!color.isEmpty()) distinct.add(color);
        }
        this.okState = true;
        return distinct.size();
    }

    /**
     * Retorna los colores de los símbolos visibles en la máquina, de izquierda a derecha.
     * 
     * @return Arreglo con lo que se ve actualmente.
     */
    public String[] configuration() {
        String[] config = new String[wheels.size()];
        for (int i = 0; i < wheels.size(); i++) {
            config[i] = wheels.get(i).getVisibleColor();
        }
        this.okState = true;
        return config;
    }

    /**
     * Valida si el estado visible corresponde al premio mayor (Jackpot).
     * 
     * @return true si todos los símbolos visibles son iguales.
     */
    public boolean isJackpot() {
        if (wheels.isEmpty() || wheels.get(0).getVisibleColor().isEmpty()) {
            this.okState = true;
            return false;
        }
        String firstColor = wheels.get(0).getVisibleColor();
        for (Wheel w : wheels) {
            if (!w.getVisibleColor().equalsIgnoreCase(firstColor)) {
                this.okState = true;
                return false;
            }
        }
        this.okState = true;
        return true;
    }

    /**
     * Muestra la máquina tragamonedas en pantalla.
     */
    public void makeVisible() {
        this.isVisible = true;
        this.background.makeVisible();
        for (Wheel w : wheels) w.makeVisible();
        this.okState = true;
        checkJackpotVisuals();
    }

    /**
     * Oculta la máquina tragamonedas de la pantalla.
     */
    public void makeInvisible() {
        this.isVisible = false;
        this.background.makeInvisible();
        for (Wheel w : wheels) w.makeInvisible();
        this.okState = true;
    }

    /**
     * Finaliza la máquina y cierra el simulador.
     */
    public void exit() {
        makeInvisible();
        System.exit(0);
    }

    /**
     * Indica el estado de finalización de la última orden ejecutada.
     * 
     * @return true si fue exitosa, false si hubo un error.
     */
    public boolean ok() {
        return this.okState;
    }
}