import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de unidad para el ciclo 3 de SlotMachine: SlotMachine(n) y
 * SlotMachineContest (solve/simulate). Todas corren en modo invisible
 * (no se llama makeVisible en ninguna prueba).
 * 
 * @author Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * @version 1.0
 */
public class SlotMachineContestTest {

    // SlotMachine(n)

    /**
     * Qué debería hacer: crear exactamente n ruedas y n símbolos.
     */
    @Test
    public void shouldCreateEqualNumberOfWheelsAndSymbols() {
        SlotMachine machine = new SlotMachine(7);

        assertEquals(7, machine.configuration().length); // n ruedas
        assertEquals(7, machine.symbols().length);        // n símbolos
    }

    /**
     * Qué NO debería hacer: arrancar ya en jackpot (el enunciado lo garantiza).
     * Se prueba muchas veces porque la configuración inicial es aleatoria.
     */
    @Test
    public void shouldNeverStartAlreadyWon() {
        for (int attempt = 0; attempt < 30; attempt++) {
            SlotMachine machine = new SlotMachine(5);
            assertFalse(machine.isJackpot());
        }
    }

    // SlotMachineContest.solveOn(machine, n)

    /**
     * Qué debería hacer: dejar la máquina en jackpot real tras resolverla,
     * para varios tamaños de n (incluyendo los extremos del problema: 3 y 50).
     */
    @Test
    public void shouldReachJackpotForVariousSizes() {
        int[] sizes = {3, 5, 10, 25, 50};
        for (int n : sizes) {
            SlotMachine machine = new SlotMachine(n);
            SlotMachineContest.solveOn(machine, n);
            assertTrue(machine.isJackpot(), "Falló con n=" + n);
        }
    }

    /**
     * Qué debería hacer: nunca superar el límite de 10 000 actions que
     * exige el problema, incluso en el caso más grande permitido (n=50).
     */
    @Test
    public void shouldStayWithinActionLimit() {
        SlotMachine machine = new SlotMachine(50);
        int[][] actions = SlotMachineContest.solveOn(machine, 50);

        assertTrue(actions.length <= 10000);
    }

    /**
     * Qué debería hacer: cada acción registrada debe tener exactamente el
     * formato {wheel, steps} — dos números, con wheel dentro de rango válido.
     */
    @Test
    public void shouldRecordActionsInValidFormat() {
        int n = 8;
        SlotMachine machine = new SlotMachine(n);
        int[][] actions = SlotMachineContest.solveOn(machine, n);

        for (int[] action : actions) {
            assertEquals(2, action.length);
            assertTrue(action[0] >= 1 && action[0] <= n);
        }
    }

    /**
     * Qué debería hacer: resolver consistentemente incluso en el caso
     * mínimo permitido por el problema (n=3).
     */
    @Test
    public void shouldSolveSmallestAllowedSize() {
        SlotMachine machine = new SlotMachine(3);
        SlotMachineContest.solveOn(machine, 3);

        assertTrue(machine.isJackpot());
    }
}