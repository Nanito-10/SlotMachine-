import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Clase COLECTIVA de pruebas de unidad para el Ciclo 3 
 *
 * Authors: Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * Prefijo asignado: MrSe
 */
public class SlotMachineContestCTest {

    // ---- SlotMachine(n)
    /**
     * Debería: SlotMachine(n) crea exactamente n ruedas y n símbolos,
     * para varios tamaños distintos.
     */
    @Test
    public void accordingMrSeShouldCreateEqualWheelsAndSymbolsForAnySize() {
        int[] tamanos = {3, 5, 10};
        for (int n : tamanos) {
            SlotMachine machine = new SlotMachine(n);
            assertEquals(n, machine.configuration().length);
            assertEquals(n, machine.symbols().length);
        }
    }

    /**
     * Debería: los n símbolos creados por SlotMachine(n) son todos
     * DISTINTOS entre sí.
     */
    @Test
    public void accordingMrSeShouldCreateNSymbolsAllDistinct() {
        int n = 8;
        SlotMachine machine = new SlotMachine(n);
        Set<String> distinct = new HashSet<>(Arrays.asList(machine.symbols()));

        assertEquals(n, distinct.size());
    }

    /**
     * No debería: SlotMachine(n) nunca debe arrancar ya en jackpot. Se repite varias
     * veces porque la configuración inicial es aleatoria.
     */
    @Test
    public void accordingMrSeShouldNeverStartAlreadyInJackpot() {
        for (int intento = 0; intento < 20; intento++) {
            SlotMachine machine = new SlotMachine(4);
            assertFalse(machine.isJackpot());
        }
    }

    // ---- SlotMachineContest.solve(n) 

    /**
     * Debería: solve(n) devuelve una solución válida para TODOS los tamaños
     * que permite el problema (de n=3 a n=50): respeta el límite de 10 000
     * acciones y cada acción tiene el formato {wheel, steps}. Cubre a la vez
     * el tamaño mínimo, los intermedios y el máximo.
     */
    @Test
    public void accordingMrSeShouldSolveForEverySizeFromThreeToFifty() {
        for (int n = 3; n <= 50; n++) {
            assertValidSolution(n);
        }
    }

    /**
     * Debería: solve(n) devolver una solución válida en ejecuciones repetidas.
     * Como cada SlotMachine(n) arranca con una configuración inicial distinta
     * (aleatoria), se comprueba que no funcione solo en un caso con suerte.
     */
    @Test
    public void accordingMrSeShouldSolveRepeatedlyBecauseInitialStateIsRandom() {
        for (int intento = 0; intento < 20; intento++) {
            assertValidSolution(5);
        }
    }

    /**
     * Debería: solve(n) respeta el límite de 10 000 acciones y cada acción
     * tiene el formato {wheel, steps}, con wheel dentro de rango, en el
     * tamaño mas grande permitido por el problema (n=50), el caso donde es
     * más fácil que una implementación se pase del límite de acciones.
     */
    @Test
    public void accordingMrSeShouldSolveWithinActionLimitForLargestSize() {
        assertValidSolution(50);
    }

    /**
     * Debería: como SlotMachine(n) nunca arranca ya ganada, solve(n)
     * siempre debe devolver al menos una acción 
     */
    @Test
    public void accordingMrSeShouldReturnNonEmptyActionsWhenMachineNotAlreadyWon() {
        int[][] acciones = SlotMachineContest.solve(6);

        assertTrue(acciones.length > 0);
    }

    /**
     * Debería: ninguna acción devuelta por solve(n) puede tener un número de
     * pasos fuera del rango que permite el problema (entre -1 000 000 000 y 1 000 000 000).
     * Se acepta cualquier signo, porque el enunciado permite girar en ambos
     * sentidos y otra solución correcta podría usar pasos negativos.
     */
    @Test
    public void accordingMrSeShouldProduceActionsWithStepsWithinAllowedRange() {
        int[][] acciones = SlotMachineContest.solve(10);

        for (int[] accion : acciones) {
            assertTrue(accion[1] >= -1000000000 && accion[1] <= 1000000000);
        }
    }

    /**
     * Debería: una máquina recién creada con SlotMachine(n) muestra entre 2 y n
     * símbolos distintos. Nunca 1, porque nunca arranca en jackpot, ni más de
     * n, porque solo hay n ruedas. Se repite porque la configuración es aleatoria.
     */
    @Test
    public void accordingMrSeShouldStartWithBetweenTwoAndNDistinctSymbols() {
        int[] tamanos = {3, 5, 10};
        for (int n : tamanos) {
            for (int intento = 0; intento < 10; intento++) {
                SlotMachine machine = new SlotMachine(n);
                int distintos = machine.distinctSymbols();

                assertTrue(distintos >= 2 && distintos <= n);
            }
        }
    }

    /**
     * Debería: todo color que muestra una rueda en configuration() pertenece
     * a la secuencia de symbols() de la máquina (las ruedas comparten la
     * misma secuencia y no aparecen símbolos ajenos).
     */
    @Test
    public void accordingMrSeShouldOnlyShowSymbolsBelongingToTheMachine() {
        int n = 7;
        SlotMachine machine = new SlotMachine(n);
        Set<String> permitidos = new HashSet<>(Arrays.asList(machine.symbols()));

        for (String visible : machine.configuration()) {
            assertTrue(permitidos.contains(visible));
        }
    }

    /**
     * Verifica que solve(n) respete el límite de 10 000 acciones y que cada
     * acción tenga el formato {wheel, steps}, con wheel dentro de rango.
     * Lo comparten las pruebas de solve(n).
     *
     * @param n Número de ruedas y símbolos de la máquina a resolver.
     */
    private void assertValidSolution(int n) {
        int[][] acciones = SlotMachineContest.solve(n);

        assertTrue(acciones.length <= 10000);
        for (int[] accion : acciones) {
            assertEquals(2, accion.length);
            assertTrue(accion[0] >= 1 && accion[0] <= n);
        }
    }
}
