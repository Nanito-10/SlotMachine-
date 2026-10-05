import java.util.ArrayList;
import java.util.List;

/**
 * Resuelve y simula el problema de la maratón ICPC 2025 "Slot Machine".
 * Usa SlotMachine ÚNICAMENTE a través de tres puntos de entrada permitidos
 * como "testing tool": el constructor SlotMachine(n), spin(wheel,steps) y
 * distinctSymbols(). Nunca se consulta el color real de ningún símbolo,
 * igual que en el problema real, donde solo se conoce el conteo de
 * distintos, nunca los símbolos en sí.
 * 
 * @author Juan José Sanabria Espinel y Tomas Alejandro Melo Rangel
 * @version 1.2
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
        SlotMachine testMachine = new SlotMachine(n);
        return solveOn(testMachine, n);
    }

    /**
     * Crea una máquina de n ruedas, la hace visible, y resuelve el jackpot
     * EN VIVO sobre esa misma máquina: el usuario ve cada giro de las 3
     * fases del algoritmo, no solo el resultado final.
     * 
     * @param n Número de ruedas y símbolos de la máquina a simular.
     */
    public static void simulate(int n) {
        SlotMachine machine = new SlotMachine(n);
        machine.makeVisible();
        solveOn(machine, n);
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
        List<int[]> actions = new ArrayList<>();
        phase1MakeAllDistinct(machine, n, actions);
        int[] offset = phase2IdentifyPermutation(machine, n, actions);
        phase3AlignAll(machine, n, offset, actions);
        return actions.toArray(new int[0][]);
    }

    /**
     * FASE 1: logra que las n ruedas muestren n símbolos todos distintos.
     * La rueda 1 nunca se toca: es el punto de referencia fijo.
     * 
     * @param machine Máquina sobre la cual operar.
     * @param n Número de ruedas y símbolos.
     * @param actions Lista donde se registran las acciones aplicadas.
     */
    private static void phase1MakeAllDistinct(SlotMachine machine, int n, List<int[]> actions) {
        int record = machine.distinctSymbols();
        for (int wheel = 2; wheel <= n && record < n; wheel++) {
            for (int step = 0; step < n; step++) {
                machine.spin(wheel, 1);
                actions.add(new int[]{wheel, 1});

                int current = machine.distinctSymbols();
                if (current > record) {
                    record = current;
                    break;
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
     * @param actions Lista donde se registran las acciones aplicadas.
     * @return offset[w] = desplazamiento original de la rueda w respecto a la rueda 1.
     */
    private static int[] phase2IdentifyPermutation(SlotMachine machine, int n, List<int[]> actions) {
        int[] offset = new int[n + 1];
        boolean[] solved = new boolean[n + 1];
        solved[1] = true;

        for (int k = 1; k < n; k++) {
            machine.spin(1, 1);
            actions.add(new int[]{1, 1});

            for (int w = 2; w <= n; w++) {
                if (solved[w]) continue;

                machine.spin(w, n - 1);
                actions.add(new int[]{w, n - 1});

                if (machine.distinctSymbols() == n) {
                    offset[w] = k;
                    solved[w] = true;
                    break;
                } else {
                    machine.spin(w, 1);
                    actions.add(new int[]{w, 1});
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
     * @param actions Lista donde se registran las acciones aplicadas.
     */
    private static void phase3AlignAll(SlotMachine machine, int n, int[] offset, List<int[]> actions) {
        for (int w = 2; w <= n; w++) {
            int remaining = n - offset[w];
            machine.spin(w, remaining);
            actions.add(new int[]{w, remaining});
        }
    }
}