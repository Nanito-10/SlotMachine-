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
 * Invariante del sistema: TODAS las ruedas comparten siempre la misma
 * secuencia de símbolos y en el mismo orden (como en una tragamonedas real,
 * donde todos los rodillos tienen impresa la misma tira de figuras).
 * 
 * @author Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * @version 2.1
 */
public class SlotMachine {
    private List<Wheel> wheels;
    private boolean isVisible;
    private boolean okState;
    private Rectangle background;

    /**
     * Inicializa una máquina tragamonedas vacía y oculta por defecto.
     */
    public SlotMachine() {
        this.wheels = new ArrayList<>();
        this.isVisible = false;
        this.okState = true;
        this.background = new Rectangle();
        // Cabinete más ancho para que quepan varias ruedas en fila y toda
        // la máquina se vea completa dentro del lienzo agrandado.
        this.background.changeSize(300, 480);
        this.background.changeColor("magenta");
        this.background.moveHorizontal(10 - 70); // Reubica el cabinete cerca del borde del lienzo
        this.background.moveVertical(10 - 15);
    }

    private static final Random contestRng = new Random();

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
        this(); // reutiliza toda la inicialización del constructor vacío

        // Colores reales para que se vea bien en una demo (n<=6). Para n
        // mayor (hasta 50, como permite el problema de la maratón) usamos
        // identificadores únicos "s0","s1"...: Canvas los pintará negros,
        // pero eso no afecta al algoritmo: solve() nunca mira el color real,
        // solo compara igualdad de identificadores.
        String[] coloresReales = {"red", "blue", "green", "yellow", "magenta", "black"};
        List<String> secuencia = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            secuencia.add(i < coloresReales.length ? coloresReales[i] : "s" + i);
        }
        Collections.shuffle(secuencia, contestRng); // orden oculto y aleatorio

        for (int i = 0; i < n; i++) addWheel(i + 1);
        for (String simbolo : secuencia) addSymbol(1, simbolo); // secuencia compartida

        // Posición visible inicial aleatoria e independiente por rueda.
        // Si por azar ya saliera jackpot, se repite (el enunciado garantiza
        // que la configuración inicial nunca es ya ganadora).
        do {
            for (Wheel w : wheels) {
                int pasos = contestRng.nextInt(n);
                for (int p = 0; p < pasos; p++) w.spin();
            }
        } while (isJackpot());
    }

    /**
     * Revisamos si hay ruedas en nuestra maquina, sino mandamos error.
     * En otro caso añadimos esa rueda en lock y ponemos a un estado true para verificar que se hizo lock.
     * 
     * @param wheel Número de la rueda a fijar (iniciando en 1).
     */
    public void lock(int wheel) {
        if (wheels.isEmpty()) {
            reportError("No hay ruedas disponibles.");
            return;
        }
        wheels.get(adjustWheelPosition(wheel)).lock();
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
        // Paso 1: validar todo antes de mover nada
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
        // Paso 2: ya validado, ahora sí se aplica
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
        boolean seMovioAlguna = false;
        for (Wheel w : wheels) {
            if (!w.isLocked()) {
                w.spin();
                seMovioAlguna = true;
            }
        }
        if (!seMovioAlguna) {
            reportError("Todas las ruedas están fijas.");
            return;
        }
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
     * orden actual en la lista. Es necesario llamarlo después de cualquier
     * addWheel/delWheel, porque insertar o eliminar una rueda en medio de las
     * demás corre la posición que deberían tener las que quedan a su derecha.
     */
    private void recalcWheelPositions() {
        for (int i = 0; i < wheels.size(); i++) {
            wheels.get(i).reposition(90 + (i * 60), 30);
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

        // Con este ciclo obligamos a las ruedas a dibujarse por encima.
        for (Wheel w : wheels) {
            w.makeVisible();
        }
    }

    /**
     * Agrega una rueda a la máquina en la posición especificada.
     * Si ya existen otras ruedas con símbolos cargados, la rueda nueva recibe
     * automáticamente la MISMA secuencia de símbolos, en el mismo orden, para
     * mantener el invariante de que todas las ruedas (sin importar cuántas
     * sean, N en general) comparten siempre el mismo conjunto y orden.
     * 
     * @param pos Número de la posición (iniciando en 1).
     */
    public void addWheel(int pos) {
        int index = (wheels.isEmpty()) ? 0 : (pos > wheels.size() ? wheels.size() : adjustWheelPosition(pos));
        Wheel newWheel = new Wheel(90 + (index * 60), 30); // Posición X provisional

        if (!wheels.isEmpty()) {
            String[] sharedSymbols = wheels.get(0).getSymbols();
            for (int i = 0; i < sharedSymbols.length; i++) {
                newWheel.addSymbol(i + 1, sharedSymbols[i]);
            }
        }

        wheels.add(index, newWheel);
        recalcWheelPositions(); // Reubica todas las ruedas (importante porque pudimos insertar en medio)
        if (isVisible) newWheel.makeVisible();

        this.okState = true;
        checkJackpotVisuals();
    }

    /**
     * Elimina la rueda ubicada en la posición ingresada.
     * 
     * @param pos Número de la posición de la rueda (iniciando en 1).
     */
    public void delWheel(int pos) {
        if (wheels.isEmpty()) {
            reportError("No hay ruedas registradas para eliminar.");
            return;
        }
        Wheel removed = wheels.remove(adjustWheelPosition(pos));
        removed.makeInvisible();
        recalcWheelPositions(); // Reubica las ruedas restantes para cerrar el espacio dejado
        this.okState = true;
        checkJackpotVisuals();
    }

    /**
     * Adiciona un símbolo de color a la secuencia compartida por TODAS las
     * ruedas de la máquina, en la misma posición para cada una — así el
     * orden de símbolos se mantiene idéntico en las N ruedas, sin importar
     * cuántas haya.
     * 
     * @param pos Posición en la secuencia (iniciando en 1).
     * @param color Color CSS del símbolo a agregar.
     */
    public void addSymbol(int pos, String color) {
        if (wheels.isEmpty()) {
            reportError("Debe agregar una rueda primero.");
            return;
        }
        // Como todas las ruedas comparten la misma secuencia, basta revisar
        // la primera para saber si el color ya existe en todas.
        boolean existsAlready = Arrays.asList(wheels.get(0).getSymbols())
                                       .stream()
                                       .anyMatch(c -> c.equalsIgnoreCase(color));
        if (existsAlready) {
            reportError("El color ya existe en la secuencia de símbolos.");
            return;
        }
        for (Wheel w : wheels) {
            w.addSymbol(pos, color);
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
     * (no cambia con los giros). Como todas las ruedas comparten el mismo
     * orden, esta lista representa la secuencia de toda la máquina.
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
     * Es decir: cuál símbolo se está viendo en cada rueda en este momento.
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