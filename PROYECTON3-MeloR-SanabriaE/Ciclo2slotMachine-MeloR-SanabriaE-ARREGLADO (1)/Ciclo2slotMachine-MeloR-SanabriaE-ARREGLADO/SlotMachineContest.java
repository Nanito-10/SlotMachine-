import java.util.ArrayList;
import java.util.List;

/**
 * Resuelve y simula el problema de la maratón ICPC 2025 "Slot Machine".
 * Usa SlotMachine unicamente a través de tres puntos de entrada permitidos
 * como "testing tool": el constructor SlotMachine(n), spin(wheel,steps) y
 * distinctSymbols(). Nunca se consulta el color real de ningún símbolo,
 * igual que en el problema real, donde solo se conoce el conteo de
 * distintos, nunca los símbolos en sí.
 * 
 * @author Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * @version 1.1
 */
public class SlotMachineContest {

    /**
     * Encuentra la secuencia de acciones necesarias para ganar el jackpot en
     * una máquina nueva de n ruedas y n símbolos. La máquina permanece
     * invisible durante todo el proceso (requisito de diseño).
     * 
     * @param n Número de ruedas y símbolos de la máquina a resolver.
     * @return Secuencia de acciones {wheel, steps}, en el orden en que deben aplicarse.
     */
    public static int[][] solve(int n) {
        SlotMachine testMachine = new SlotMachine(n); // arranca invisible
        return solveOn(testMachine, n);
    }

    /**
     * Crea una máquina de n ruedas, la resuelve OCULTA (rol de testing tool,
     * igual que solve()), y solo la revela al final ya ganada, igual que en
     * el problema real, donde nunca ves las ruedas mientras las manipulas a
     * ciegas, solo el resultado cuando por fin ganas.
     * 
     * @param n Número de ruedas y símbolos de la máquina a simular.
     */
    public static void simulate(int n) {
        SlotMachine machine = new SlotMachine(n); // arranca invisible
        solveOn(machine, n); // se resuelve oculta 
        machine.makeVisible(); // se revela ya en jackpot 
    }

    /**
     * Resuelve una máquina ya existente (creada por quien llama), en vez de
     * crear una nueva. Útil para pruebas de unidad: permite crear la
     * máquina, resolverla, y verificar isJackpot() directamente sobre ella.
     * 
     * @param machine Máquina a resolver (invisible o visible; no se cambia su visibilidad aquí).
     * @param n Número de ruedas y símbolos de esa máquina.
     * @return Secuencia de acciones {wheel, steps} aplicadas, en orden.
     */
    public static int[][] solveOn(SlotMachine machine, int n) {
        List<int[]> acciones = new ArrayList<>();
        fase1DistinguirTodas(machine, n, acciones);
        int[] offset = fase2IdentificarPermutacion(machine, n, acciones);
        fase3AlinearTodas(machine, n, offset, acciones);
        return acciones.toArray(new int[0][]);
    }

    /**
     * FASE 1: logra que las n ruedas muestren n símbolos todos distintos.
     * La rueda 1 nunca se toca: es el punto de referencia fijo.
     * 
     * @param machine Máquina sobre la cual operar.
     * @param n Número de ruedas y símbolos.
     * @param acciones Lista donde se registran las acciones aplicadas.
     */
    private static void fase1DistinguirTodas(SlotMachine machine, int n, List<int[]> acciones) {
        int record = machine.distinctSymbols();
        for (int wheel = 2; wheel <= n && record < n; wheel++) {
            for (int paso = 0; paso < n; paso++) {
                machine.spin(wheel, 1);
                acciones.add(new int[]{wheel, 1});

                int actual = machine.distinctSymbols();
                if (actual > record) {
                    record = actual;
                    break; // esta rueda ya no choca con ninguna otra; siguiente rueda
                }
            }
        }
    }

    /**
     * FASE 2: identifica, para cada rueda (excepto la 1), cuál era su
     * desplazamiento original respecto a la rueda 1, usando el truco de
     * "llenar el hueco": al mover la rueda 1 hacia adelante se genera un
     * hueco; solo la rueda candidata correcta, al retroceder un paso, cae
     * exactamente en ese hueco y restaura el máximo de símbolos distintos.
     * 
     * @param machine Máquina sobre la cual operar (ya con símbolos distintos).
     * @param n Número de ruedas y símbolos.
     * @param acciones Lista donde se registran las acciones aplicadas.
     * @return offset[w] = desplazamiento original de la rueda w respecto a la rueda 1.
     */
    private static int[] fase2IdentificarPermutacion(SlotMachine machine, int n, List<int[]> acciones) {
        int[] offset = new int[n + 1];
        boolean[] resuelta = new boolean[n + 1];
        resuelta[1] = true;

        for (int k = 1; k < n; k++) {
            machine.spin(1, 1); // rueda 1 avanza un paso más
            acciones.add(new int[]{1, 1});

            for (int w = 2; w <= n; w++) {
                if (resuelta[w]) continue;

                machine.spin(w, n - 1); // "retroceder un paso" = avanzar n-1 (no hay giro negativo)
                acciones.add(new int[]{w, n - 1});

                if (machine.distinctSymbols() == n) {
                    offset[w] = k;
                    resuelta[w] = true;
                    break; // encontramos a quién pertenecía este desplazamiento
                } else {
                    machine.spin(w, 1); // deshacer: completar la vuelta, vuelve a su lugar
                    acciones.add(new int[]{w, 1});
                }
            }
        }
        return offset;
    }

    /**
     * FASE 3: con el desplazamiento original de cada rueda ya conocido, un
     * solo giro por rueda basta para alcanzar exactamente lo que ahora
     * muestra la rueda 1: esto es el jackpot.
     * 
     * @param machine Máquina sobre la cual operar.
     * @param n Número de ruedas y símbolos.
     * @param offset Desplazamientos calculados en la fase 2.
     * @param acciones Lista donde se registran las acciones aplicadas.
     */
    private static void fase3AlinearTodas(SlotMachine machine, int n, int[] offset, List<int[]> acciones) {
        for (int w = 2; w <= n; w++) {
            int faltante = n - offset[w];
            machine.spin(w, faltante);
            acciones.add(new int[]{w, faltante});
        }
    }
}