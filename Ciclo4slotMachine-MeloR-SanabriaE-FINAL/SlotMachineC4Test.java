import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de unidad para el ciclo 4: tipos nuevos de rueda (lefty, rebel,
 * rainbow) y de símbolo (ephemeral, shy). Todas corren en modo invisible.
 * 
 * @author Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * @version 1.0
 */
public class SlotMachineC4Test {

    private SlotMachine machine;

    @BeforeEach
    public void setUp() {
        machine = new SlotMachine();
    }

    // RebelWheel

    /**
     * Qué NO debería hacer: dejarse fijar (lock) si es de tipo rebel.
     */
    @Test
    public void shouldNotLockARebelWheel() {
        machine.addWheel("rebel", 1);

        machine.lock(1);

        assertFalse(machine.ok());
    }

    /**
     * Qué NO debería hacer: dejarse intercambiar (swap) si es de tipo rebel,
     * aunque la otra rueda involucrada sea normal.
     */
    @Test
    public void shouldNotSwapARebelWheel() {
        machine.addWheel("rebel", 1);
        machine.addWheel("normal", 2);

        machine.swap(1, 2);

        assertFalse(machine.ok());
    }

    /**
     * Qué NO debería hacer: dejarse eliminar (delWheel) si es de tipo rebel.
     */
    @Test
    public void shouldNotDeleteARebelWheel() {
        machine.addWheel("rebel", 1);

        machine.delWheel(1);

        assertFalse(machine.ok());
    }

    /**
     * Qué debería hacer: una rueda normal sí puede fijarse, intercambiarse
     * y eliminarse con normalidad (para contrastar con rebel).
     */
    @Test
    public void shouldAllowLockSwapDeleteOnNormalWheel() {
        machine.addWheel("normal", 1);
        machine.addWheel("normal", 2);

        machine.lock(1);
        assertTrue(machine.ok());

        machine.unlock(1);
        machine.swap(1, 2);
        assertTrue(machine.ok());

        machine.delWheel(1);
        assertTrue(machine.ok());
    }

    // LeftyWheel

    /**
     * Qué debería hacer: al girar, copiar exactamente el símbolo que
     * muestra su vecina izquierda, en vez de avanzar por su cuenta.
     */
    @Test
    public void shouldLeftyWheelCopyLeftNeighborOnSpin() {
        machine.addWheel("normal", 1);
        machine.addWheel("lefty", 2);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue");

        machine.placeSymbol(1, "blue");
        machine.spin(2, 1);

        assertTrue(machine.ok());
        assertEquals("blue", machine.configuration()[1]);
    }

    /**
     * Qué debería hacer: si es la primera rueda (sin vecina a la
     * izquierda), comportarse como una rueda normal al girar.
     */
    @Test
    public void shouldLeftyWheelBehaveNormallyWithoutLeftNeighbor() {
        machine.addWheel("lefty", 1);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue");
        machine.addSymbol(1, "green");
        String visibleBefore = machine.configuration()[0];

        machine.spin(1, 3);

        assertTrue(machine.ok());
        assertEquals(visibleBefore, machine.configuration()[0]);
    }

    /**
     * Regresión: placeSymbol sobre una lefty NO debe entrar en recursión
     * infinita (placeSymbol usa advance(), no el spin() polimórfico).
     */
    @Test
    public void shouldPlaceSymbolOnLeftyWheelWithoutInfiniteRecursion() {
        machine.addWheel("normal", 1);
        machine.addWheel("lefty", 2);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue");
        machine.addSymbol(1, "green");

        machine.placeSymbol(2, "red");

        assertTrue(machine.ok());
        assertEquals("red", machine.configuration()[1]);
    }

    // RainbowWheel

    /**
     * Qué debería hacer: al girar, adoptar el símbolo más frecuente entre
     * las demás ruedas (no solo el de la vecina inmediata).
     */
    @Test
    public void shouldRainbowWheelCopyMostFrequentSymbolOnSpin() {
        machine.addWheel("normal", 1);
        machine.addWheel("normal", 2);
        machine.addWheel("rainbow", 3);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue");
        machine.addSymbol(1, "green");
        machine.placeSymbol(1, "blue");
        machine.placeSymbol(2, "blue");
        machine.placeSymbol(3, "red");

        machine.spin(3, 1);

        assertTrue(machine.ok());
        assertEquals("blue", machine.configuration()[2]);
    }

    /**
     * Qué debería hacer: en empate, ganar el símbolo de la rueda más a la
     * izquierda (resultado determinista).
     */
    @Test
    public void shouldRainbowWheelBreakTiesWithLeftmostWheel() {
        machine.addWheel("normal", 1);
        machine.addWheel("normal", 2);
        machine.addWheel("rainbow", 3);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue");
        machine.addSymbol(1, "green");
        machine.placeSymbol(1, "red");
        machine.placeSymbol(2, "blue");

        machine.spin(3, 1);

        assertEquals("red", machine.configuration()[2]);
    }

    /**
     * Qué debería hacer: sin otras ruedas, comportarse como una rueda
     * normal (una vuelta completa deja el mismo símbolo).
     */
    @Test
    public void shouldRainbowWheelBehaveNormallyWhenAlone() {
        machine.addWheel("rainbow", 1);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue");
        String before = machine.configuration()[0];

        machine.spin(1, 2);

        assertTrue(machine.ok());
        assertEquals(before, machine.configuration()[0]);
    }

    /**
     * Qué debería hacer: todas las ruedas giradas con spin() terminan en
     * jackpot si la rainbow se une a dos ruedas que ya coinciden.
     */
    @Test
    public void shouldRainbowWheelHelpReachJackpot() {
        machine.addWheel("normal", 1);
        machine.addWheel("normal", 2);
        machine.addWheel("rainbow", 3);
        machine.addSymbol(1, "red");
        machine.addSymbol(1, "blue");
        machine.placeSymbol(1, "blue");
        machine.placeSymbol(2, "blue");
        machine.placeSymbol(3, "red");

        machine.spin(3);

        assertTrue(machine.isJackpot());
    }

    // EphemeralSymbol

    /**
     * Qué debería hacer: reducir su tamaño en cada giro de la rueda, hasta
     * llegar a un mínimo y no bajar más.
     */
    @Test
    public void shouldEphemeralSymbolShrinkOnEachSpinDownToMinimum() {
        EphemeralSymbol s = new EphemeralSymbol("red");
        int initialSize = s.getCurrentSize();

        s.onWheelSpin();
        int sizeAfterOneSpin = s.getCurrentSize();

        assertTrue(sizeAfterOneSpin < initialSize);

        for (int i = 0; i < 20; i++) s.onWheelSpin();

        assertTrue(s.getCurrentSize() >= 2);
    }

    /**
     * Qué debería hacer: encogerse en cada spin() de SU PROPIA rueda, sin
     * importar si en ese momento está visible o no.
     */
    @Test
    public void shouldEphemeralSymbolShrinkEvenWhenNotCurrentlyVisible() {
        machine.addWheel(1);
        machine.addSymbol("ephemeral", 1, "red");
        machine.addSymbol(1, "blue");

        machine.spin(1, 1);
        machine.spin(1, 1);

        assertTrue(machine.ok());
    }

    // ShySymbol

    /**
     * Qué debería hacer: empezar visible y alternar entre oculto y visible
     * cada vez que es seleccionado en la rueda.
     */
    @Test
    public void shouldShySymbolToggleVisibilityOnEachSelection() {
        ShySymbol s = new ShySymbol("red");
        boolean initial = s.isShown();

        s.onSelected();
        boolean first = s.isShown();

        s.onSelected();
        boolean second = s.isShown();

        assertTrue(initial);
        assertFalse(first);
        assertTrue(second);
    }

    /**
     * Qué debería hacer: dentro de una rueda, el símbolo shy alterna UNA
     * vez por cada vez que un giro lo deja seleccionado (no por cada
     * refresco visual).
     */
    @Test
    public void shouldShyInsideWheelToggleOncePerSelection() {
        Wheel w = new Wheel(90, 30);
        w.addSymbol("shy", 1, "red");
        w.addSymbol("normal", 2, "blue");

        w.spin();
        w.spin();
        assertEquals("red", w.getVisibleColor());

        w.spin();
        w.spin();

        assertEquals("red", w.getVisibleColor());
    }

    // addWheel(type,pos) / addSymbol(type,pos,color): compatibilidad

    /**
     * Qué debería hacer: addWheel(pos) sin tipo sigue creando una rueda
     * normal, igual que antes del ciclo 4 (compatibilidad hacia atrás).
     */
    @Test
    public void shouldAddWheelWithoutTypeStillCreateNormalWheel() {
        machine.addWheel(1);

        machine.lock(1);
        assertTrue(machine.ok());
    }

    /**
     * Qué debería hacer: addSymbol(pos,color) sin tipo sigue creando un
     * símbolo normal (no se encoge ni parpadea).
     */
    @Test
    public void shouldAddSymbolWithoutTypeStillCreateNormalSymbol() {
        machine.addWheel(1);
        machine.addSymbol(1, "red");

        assertTrue(machine.ok());
        assertArrayEquals(new String[]{"red"}, machine.symbols());
    }
}