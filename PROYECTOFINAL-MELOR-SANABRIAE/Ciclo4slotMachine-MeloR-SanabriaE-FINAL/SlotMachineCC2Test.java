import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * Prefijo asignado: MrSe (Melo-Rangel, Sanabria-Espinel; orden alfabético por primer apellido)
 * @version 1.1
 */
public class SlotMachineCC2Test {

    // swap(wheel1, wheel2)

    /**
     * Debería: al intercambiar dos ruedas, cada una debe pasar a mostrar
     * el símbolo que antes mostraba la otra. Como todas las ruedas
     * comparten siempre la misma secuencia de símbolos, se verifica con
     * configuration() (qué se ve en cada rueda), no con symbols().
     */
    @Test
    public void accordingMrSeShouldSwapWheelsRegardlessOfSymbolCount() {
        SlotMachine machine = new SlotMachine();
        machine.addWheel(1);
        machine.addWheel(2);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue");

        machine.placeSymbol(1, "red");
        machine.placeSymbol(2, "blue");

        machine.swap(1, 2);

        assertTrue(machine.ok());
        assertEquals("blue", machine.configuration()[0]);
        assertEquals("red", machine.configuration()[1]);
    }

    /**
     * No debería: hacer swap si la máquina tiene menos de dos ruedas
     * (no hay con qué intercambiar).
     */
    @Test
    public void accordingMrSeShouldRejectSwapWhenFewerThanTwoWheelsExist() {
        SlotMachine machine = new SlotMachine();
        machine.addWheel(1); // solo una rueda

        machine.swap(1, 2);

        assertFalse(machine.ok());
    }

    // spin(setSymbols)

    /**
     * No debería: aplicar ningún cambio si uno de los colores pedidos
     * no existe en la secuencia compartida (comportamiento todo o nada).
     */
    @Test
    public void accordingMrSeShouldRejectSpinSetSymbolsWhenColorMissing() {
        SlotMachine machine = new SlotMachine();
        machine.addWheel(1);
        machine.addWheel(2);
        machine.addSymbol(1, "red");
        machine.addSymbol(2, "green"); // secuencia compartida: red, green

        String[] before = machine.configuration();
        machine.spin(new String[]{"red", "purple"}); // "purple" no existe

        assertFalse(machine.ok());
        assertArrayEquals(before, machine.configuration()); // nada se movió
    }

    // lock(wheel) / unlock(wheel)

    /**
     * Debería: una rueda fijada (lock) no debe moverse ante un intento de
     * giro, y debe volver a poder girar después de un unlock.
     */
    @Test
    public void accordingMrSeShouldKeepLockedWheelFixedAndAllowSpinAfterUnlock() {
        SlotMachine machine = new SlotMachine();
        machine.addWheel(1);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue");
        machine.placeSymbol(1, "red"); // estado conocido

        machine.lock(1);
        machine.spin(1, 2); // intenta girar la rueda fija

        assertFalse(machine.ok());
        assertEquals("red", machine.configuration()[0]); // no debió moverse

        machine.unlock(1);
        machine.spin(1, 1); // ahora sí debe poder girar

        assertTrue(machine.ok());
    }

    // spin(wheel, steps)

    /**
     * Debería: si una rueda tiene N símbolos distintos y se gira exactamente
     * N pasos, debe volver a mostrar el mismo color visible inicial (dio
     * una vuelta completa).
     */
    @Test
    public void accordingMrSeShouldReturnToSameVisibleColorAfterFullRotation() {
        SlotMachine machine = new SlotMachine();
        machine.addWheel(1);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue");
        machine.addSymbol(1, "green");
        String before = machine.configuration()[0];

        machine.spin(1, 3); // vuelta completa: 3 símbolos, 3 pasos

        assertTrue(machine.ok());
        assertEquals(before, machine.configuration()[0]);
    }
}