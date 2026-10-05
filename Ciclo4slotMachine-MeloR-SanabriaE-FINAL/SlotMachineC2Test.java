import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de unidad para el ciclo 2 de SlotMachine:
 * swap, lock/unlock, spin(wheel,steps) y spin(setSymbols).
 * @author Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * @version 1.1
 */
public class SlotMachineC2Test {

    private SlotMachine machine;

    /**
     * Se ejecuta antes de cada prueba: crea una máquina nueva y vacía.
     * Nunca se llama makeVisible(), por lo que todas las pruebas corren
     * en modo invisible.
     */
    @BeforeEach
    public void setUp() {
        machine = new SlotMachine();
    }

    // swap(wheel1, wheel2)

    /**
     * Qué debería hacer: al intercambiar dos ruedas, cada una debe pasar a
     * mostrar el símbolo que antes mostraba la otra. 
     */
    @Test
    public void shouldSwapTwoWheelsRegardlessOfSymbolCount() {
        machine.addWheel(1);
        machine.addWheel(2);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue");
        machine.addSymbol(1, "green"); // secuencia compartida por ambas ruedas

        machine.placeSymbol(1, "red");  // rueda 1 muestra "red"
        machine.placeSymbol(2, "blue"); // rueda 2 muestra "blue"

        machine.swap(1, 2);

        assertTrue(machine.ok());
        // Tras el swap, cada posición debe mostrar lo que mostraba la otra.
        assertEquals("blue", machine.configuration()[0]);
        assertEquals("red", machine.configuration()[1]);
    }

    /**
     * Qué NO debería hacer: intercambiar ruedas si hay menos de dos
     * ruedas en la máquina (no hay con qué intercambiar).
     */
    @Test
    public void shouldNotSwapWhenLessThanTwoWheelsExist() {
        machine.addWheel(1); // solo una rueda

        machine.swap(1, 2);

        assertFalse(machine.ok());
    }

    /**
     * Qué debería hacer: si se piden posiciones fuera de rango, ajustarlas
     * al máximo/mínimo válido, en vez de fallar.
     */
    @Test
    public void shouldClampOutOfRangeSwapPositions() {
        machine.addWheel(1);
        machine.addWheel(2);
        machine.addSymbol(1, "red");
        machine.addSymbol(2, "blue"); // secuencia compartida por ambas ruedas

        machine.placeSymbol(1, "red");
        machine.placeSymbol(2, "blue");

        machine.swap(1, 100); // 100 se ajusta a la última rueda (2)

        assertTrue(machine.ok());
        assertEquals("blue", machine.configuration()[0]);
        assertEquals("red", machine.configuration()[1]);
    }

    /**
     * Qué debería hacer: prevenir el giro de una rueda fija incluso usando
     * spin(wheel) de UN solo argumento (sin steps), no solo la variante
     * spin(wheel, steps). Este caso existe porque encontramos que lock()
     * no estaba siendo respetado por esta sobrecarga en particular.
     */
    @Test
    public void shouldPreventSingleArgSpinOnLockedWheel() {
        machine.addWheel(1);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue");
        machine.placeSymbol(1, "red"); // estado conocido

        machine.lock(1);
        machine.spin(1); // spin(wheel) de un solo argumento

        assertFalse(machine.ok());
        assertEquals("red", machine.configuration()[0]); // no debió moverse
    }

    // lock(wheel) / unlock(wheel)

    /**
     * Qué debería hacer: una rueda bloqueada no debe moverse cuando
     * se le pide girar explícitamente.
     */
    @Test
    public void shouldPreventSpinOnLockedWheel() {
        machine.addWheel(1);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue");
        String visibleBefore = machine.configuration()[0]; // color visible inicial

        machine.lock(1);
        machine.spin(1, 2);

        assertFalse(machine.ok());
        assertEquals(visibleBefore, machine.configuration()[0]); // no se movió
    }

    /**
     * Qué debería hacer: tras un unlock, la rueda debe volver a girar
     * con normalidad.
     */
    @Test
    public void shouldAllowSpinAfterUnlock() {
        machine.addWheel(1);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue");

        machine.lock(1);
        machine.unlock(1);
        machine.spin(1, 1);

        assertTrue(machine.ok());
    }

    /**
     * Qué NO debería hacer: fijar una rueda si la máquina no tiene ninguna.
     */
    @Test
    public void shouldNotLockWhenNoWheelsExist() {
        machine.lock(1);

        assertFalse(machine.ok());
    }

    // spin(wheel, steps)

    /**
     * Qué debería hacer: girar la rueda indicada "steps" veces.
     * Con 3 símbolos y 3 pasos, la rueda vuelve a mostrar el mismo color
     * inicial (dio una vuelta completa alrededor del puntero visibleIndex).
     */
    @Test
    public void shouldSpinWheelExactStepsGiven() {
        machine.addWheel(1);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue");
        machine.addSymbol(1, "green");
        String visibleBefore = machine.configuration()[0];

        machine.spin(1, 3); // vuelta completa con 3 símbolos

        assertTrue(machine.ok());
        assertEquals(visibleBefore, machine.configuration()[0]);
    }

    /**
     * Qué NO debería hacer: girar una rueda si la máquina está vacía.
     */
    @Test
    public void shouldNotSpinWheelWhenMachineIsEmpty() {
        machine.spin(1, 3);

        assertFalse(machine.ok());
    }

    // spin() global  debe saltar ruedas bloqueadas

    /**
     * Qué debería hacer: mover las ruedas libres y dejar intacta la rueda
     * bloqueada. Cada rueda tiene su propio puntero visibleIndex, así que
     * aunque compartan la secuencia de símbolos, pueden mostrar cosas
     * distintas y moverse de forma independiente.
     */
    @Test
    public void shouldSkipLockedWheelsOnGlobalSpin() {
        machine.addWheel(1);
        machine.addWheel(2);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue"); // secuencia compartida por ambas ruedas

        machine.placeSymbol(1, "red");
        machine.placeSymbol(2, "blue");

        String lockedWheelColorBefore = machine.configuration()[0];
        machine.lock(1);
        machine.spin(); // gira todas las no bloqueadas

        assertTrue(machine.ok());
        assertEquals(lockedWheelColorBefore, machine.configuration()[0]); // rueda 1 no se movió
    }

    /**
     * Qué NO debería hacer: girar si todas las ruedas están bloqueadas
     * (no hay ninguna libre para mover).
     */
    @Test
    public void shouldNotSpinWhenAllWheelsAreLocked() {
        machine.addWheel(1);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue");

        machine.lock(1);
        machine.spin();

        assertFalse(machine.ok());
    }

    // spin(setSymbols)

    /**
     * Qué debería hacer: dejar la máquina exactamente en los colores
     * pedidos, cuando todos existen en la secuencia compartida.
     */
    @Test
    public void shouldSetExactConfigurationWhenAllColorsExist() {
        machine.addWheel(1);
        machine.addWheel(2);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue"); // secuencia compartida por ambas ruedas

        machine.spin(new String[]{"blue", "red"});

        assertTrue(machine.ok());
        assertArrayEquals(new String[]{"blue", "red"}, machine.configuration());
    }

    /**
     * Qué NO debería hacer: aplicar ningún cambio si uno de los colores
     * pedidos no existe en la secuencia compartida (todo o nada).
     */
    @Test
    public void shouldNotChangeAnyWheelWhenOneColorDoesNotExist() {
        machine.addWheel(1);
        machine.addWheel(2);
        machine.addSymbol(1, "red");
        machine.addSymbol(2, "green"); // secuencia compartida: red, green

        String[] before = machine.configuration();
        machine.spin(new String[]{"red", "purple"}); // "purple" no existe en la secuencia

        assertFalse(machine.ok());
        assertArrayEquals(before, machine.configuration()); // nada se movió
    }

    /**
     * Qué NO debería hacer: mover ninguna rueda si una está bloqueada
     * y su color objetivo no coincide con el que ya muestra.
     */
    @Test
    public void shouldNotChangeAnyWheelWhenLockedWheelCannotReachTarget() {
        machine.addWheel(1);
        machine.addWheel(2);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue"); // secuencia compartida por ambas ruedas

        machine.placeSymbol(1, "red");
        machine.placeSymbol(2, "blue");

        String[] before = machine.configuration();
        machine.lock(1); // rueda 1 fija mostrando "red"
        machine.spin(new String[]{"blue", "red"}); // pide cambiar la rueda bloqueada

        assertFalse(machine.ok());
        assertArrayEquals(before, machine.configuration()); // ni siquiera la rueda 2 se movió
    }

    /**
     * Qué NO debería hacer: aceptar un arreglo de colores cuyo tamaño
     * no coincide con el número de ruedas.
     */
    @Test
    public void shouldNotAcceptSymbolArrayWithWrongLength() {
        machine.addWheel(1);
        machine.addSymbol(1, "red");

        machine.spin(new String[]{"red", "blue"}); // 2 colores, 1 rueda

        assertFalse(machine.ok());
    }
}