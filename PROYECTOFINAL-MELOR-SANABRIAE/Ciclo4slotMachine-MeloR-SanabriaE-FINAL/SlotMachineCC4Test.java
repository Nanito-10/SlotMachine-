import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de unidad COMPARTIDAS del ciclo 4.
 * Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * MrSe (Melo-Rangel, Sanabria-Espinel
 * @version 2.0
 */
public class SlotMachineCC4Test {

    /**
     * No debería: dejar que una rueda rebelde sea fijada (lock).
     */
    @Test
    public void accordingMrSeShouldNotLockARebelWheel() {
        SlotMachine machine = new SlotMachine();
        machine.addWheel("rebel", 1);

        machine.lock(1);

        assertFalse(machine.ok());
    }

    /**
     * No debería: dejar que una rueda rebelde sea intercambiada, ni como
     * primera ni como segunda rueda del swap; la configuración no cambia.
     */
    @Test
    public void accordingMrSeShouldNotSwapARebelWheel() {
        SlotMachine machine = new SlotMachine();
        machine.addWheel("rebel", 1);
        machine.addWheel("normal", 2);
        machine.addSymbol(1, "red");
        machine.addSymbol(2, "blue");
        machine.placeSymbol(2, "blue");
        String[] beforeConfiguration = machine.configuration();

        machine.swap(1, 2);
        assertFalse(machine.ok());
        machine.swap(2, 1);
        assertFalse(machine.ok());

        assertArrayEquals(beforeConfiguration, machine.configuration());
    }

    /**
     * No debería: dejar que una rueda rebelde sea eliminada; la máquina
     * conserva todas sus ruedas.
     */
    @Test
    public void accordingMrSeShouldNotDeleteARebelWheel() {
        SlotMachine machine = new SlotMachine();
        machine.addWheel("rebel", 1);
        machine.addWheel("normal", 2);
        machine.addSymbol(1, "red");

        machine.delWheel(1);

        assertFalse(machine.ok());
        assertEquals(2, machine.configuration().length);
    }

    /**
     * Debería: una rueda normal sí poder fijarse, intercambiarse y
     * eliminarse (contraste con la rebelde).
     */
    @Test
    public void accordingMrSeShouldAllowLockSwapAndDeleteOnNormalWheels() {
        SlotMachine machine = new SlotMachine();
        machine.addWheel("normal", 1);
        machine.addWheel("normal", 2);
        machine.addSymbol(1, "red");
        machine.addSymbol(2, "blue");

        machine.lock(1);
        assertTrue(machine.ok());
        machine.unlock(1);

        machine.swap(1, 2);
        assertTrue(machine.ok());

        machine.delWheel(1);
        assertTrue(machine.ok());
        assertEquals(1, machine.configuration().length);
    }

    /**
     * Debería: una rueda lefty con vecina a la izquierda copiar el símbolo
     * de esa vecina al girar, y por tanto lograr jackpot.
     */
    @Test
    public void accordingMrSeShouldLeftyWheelCopyItsLeftNeighborWhenSpun() {
        SlotMachine machine = new SlotMachine();
        machine.addWheel("normal", 1);
        machine.addWheel("lefty", 2);
        machine.addSymbol(1, "red");
        machine.addSymbol(2, "blue");
        machine.addSymbol(3, "green");
        machine.placeSymbol(1, "green");

        machine.spin(2, 1);

        assertTrue(machine.ok());
        assertEquals("green", machine.configuration()[1]);
        assertTrue(machine.isJackpot());
    }

    /**
     * Debería: una rueda lefty que es la primera (sin vecina izquierda)
     * comportarse como normal: tras una vuelta completa muestra lo mismo.
     */
    @Test
    public void accordingMrSeShouldLeftyWheelWithoutNeighborBehaveNormally() {
        SlotMachine machine = new SlotMachine();
        machine.addWheel("lefty", 1);
        machine.addSymbol(1, "red");
        machine.addSymbol(2, "blue");
        machine.addSymbol(3, "green");
        String beforeConfiguration = machine.configuration()[0];

        machine.spin(1, 1);
        assertNotEquals(beforeConfiguration, machine.configuration()[0]); // sí avanzó

        machine.spin(1, 2); // completa la vuelta (3 símbolos)
        assertEquals(beforeConfiguration, machine.configuration()[0]);
    }

    /**
     * Debería: tras un swap, la lefty copiar a su NUEVA vecina izquierda
     * (el vecino se recalcula cuando cambia el orden).
     */
    @Test
    public void accordingMrSeShouldLeftyWheelUseItsNewNeighborAfterSwap() {
        SlotMachine machine = new SlotMachine();
        machine.addWheel("normal", 1);
        machine.addWheel("normal", 2);
        machine.addWheel("lefty", 3);
        machine.addSymbol(1, "red");
        machine.addSymbol(2, "blue");
        machine.addSymbol(3, "green");
        machine.placeSymbol(1, "red");
        machine.placeSymbol(2, "blue");

        machine.swap(1, 2); // ahora: blue, red, lefty
        machine.spin(3, 1);

        assertEquals("red", machine.configuration()[2]); // copia a la rueda 2 actual
    }

    /**
     * Debería: una máquina con símbolos ephemeral y shy seguir
     * funcionando tras muchos giros: no pierde símbolos, no falla y los
     * colores visibles siguen siendo de la secuencia.
     */
    @Test
    public void accordingMrSeShouldKeepWorkingWithEphemeralAndShySymbols() {
        SlotMachine machine = new SlotMachine();
        machine.addWheel("normal", 1);
        machine.addSymbol("ephemeral", 1, "red");
        machine.addSymbol("shy", 2, "blue");
        machine.addSymbol("normal", 3, "green");

        machine.spin(1, 30);

        assertTrue(machine.ok());
        assertEquals(3, machine.symbols().length);
        assertTrue(java.util.Arrays.asList("red", "blue", "green")
                       .contains(machine.configuration()[0]));
    }
}
