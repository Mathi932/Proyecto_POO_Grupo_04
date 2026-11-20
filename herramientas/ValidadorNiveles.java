import com.upch.rescateselva.modelo.obstaculos.Obstaculo;
import com.upch.rescateselva.modelo.obstaculos.Rio;
import com.upch.rescateselva.modelo.obstaculos.Trampa;
import com.upch.rescateselva.modelo.personajes.AnimalAtrapado;
import com.upch.rescateselva.modelo.personajes.Jugador;
import com.upch.rescateselva.niveles.DisenoNiveles;
import com.upch.rescateselva.niveles.Nivel;
import com.upch.rescateselva.niveles.Plataforma;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Herramienta para revisar un nivel (no forma parte del juego).
 * Simula la física de Amaru cuadro por cuadro y busca, con búsqueda en anchura,
 * un camino sin recibir daño hasta el animal y de regreso a la salida.
 * Uso: java -cp target/classes:herramientas/out ValidadorNiveles 1   (nivel 1, 2 o 3)
 */
public class ValidadorNiveles {
    static final int VEL = 4, AN = 28, AL = 44, PASO = 24, YOFF = 60, YR = 700, VR = 33;
    static int W;
    static List<int[]> plats = new ArrayList<>();
    static List<int[]> traps = new ArrayList<>();
    static List<int[]> rios = new ArrayList<>();
    static final int TM = 6;

    static int[] xs, ys, vs, gs, par, act;
    static int n;

    static void push(int x, int y, int vy, int g, int p, int a) {
        if (n == xs.length) {
            int c = n * 2;
            xs = Arrays.copyOf(xs, c); ys = Arrays.copyOf(ys, c); vs = Arrays.copyOf(vs, c);
            gs = Arrays.copyOf(gs, c); par = Arrays.copyOf(par, c); act = Arrays.copyOf(act, c);
        }
        xs[n] = x; ys[n] = y; vs[n] = vy; gs[n] = g; par[n] = p; act[n] = a; n++;
    }

    static boolean inter(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    // devuelve null si falla, o {x,y,vy,g}
    static int[] step(int x, int y, int vy, int g, int dx, int jump) {
        int prevX = x, prevY = y;
        x += dx * VEL;
        if (jump == 1 && g == 1) { vy = -15; g = 0; }
        vy = Math.min(vy + 1, 16);
        y += vy; g = 0;
        if (y > 540 + 120) return null;
        int prevPies = prevY + AL;
        for (int[] p : plats) {
            int top = p[1], bottom = top + p[3];
            if (!inter(x, y, AN, AL, p[0], top, p[2], p[3])) continue;
            if (vy >= 0 && prevPies <= top + PASO) { y = top - AL; vy = 0; g = 1; }
            else if (vy < 0 && prevY >= bottom - 2) { y = bottom; if (vy < 0) vy = 0; }
            else if (prevX + AN <= p[0] + 4) x = p[0] - AN;
            else if (prevX >= p[0] + p[2] - 4) x = p[0] + p[2];
            else { y = top - AL; vy = 0; g = 1; }
        }
        if (x < 0) x = 0; else if (x + AN > W) x = W - AN;
        for (int[] t : traps) if (inter(x, y, AN, AL, t[0] - TM, t[1] - TM, t[2] + 2 * TM, t[3] + 2 * TM)) return null;
        for (int[] r : rios) { if (x + AN > r[0] && x < r[0] + r[2] && y + AL > r[1] + 8) return null; }
        return new int[] {x, y, vy, g};
    }

    static java.util.BitSet seen;
    static int idx(int x, int y, int vy, int g) { return (((x * YR) + (y + YOFF)) * VR + (vy + 16)) * 2 + g; }

    // BFS completo desde un estado; devuelve índice del primer nodo que cumple 'meta' (o -1)
    interface Meta { boolean ok(int x, int y, int vy, int g); }
    static int bfs(int sx, int sy, Meta meta) {
        seen = new java.util.BitSet((W + 80) * YR * VR * 2);
        xs = new int[1 << 20]; ys = new int[1 << 20]; vs = new int[1 << 20]; gs = new int[1 << 20]; par = new int[1 << 20]; act = new int[1 << 20]; n = 0;
        push(sx, sy, 0, 1, -1, 0); seen.set(idx(sx, sy, 0, 1));
        int found = -1;
        for (int h = 0; h < n; h++) {
            if (found < 0 && meta != null && meta.ok(xs[h], ys[h], vs[h], gs[h])) found = h;
            for (int dx = -1; dx <= 1; dx++) for (int j = 0; j <= 1; j++) {
                int[] r = step(xs[h], ys[h], vs[h], gs[h], dx, j);
                if (r == null) continue;
                if (r[1] < -YOFF || r[1] >= YR - YOFF) continue;
                int k = idx(r[0], r[1], r[2], r[3]);
                if (seen.get(k)) continue;
                seen.set(k); push(r[0], r[1], r[2], r[3], h, dx * 2 + j + 2);
            }
        }
        return found;
    }

    // acciones: act = dx*2 + j + 2  => dx = (act-2)>>1 con j = (act-2)&1  (dx puede ser -1: (-2+j)); decodificar con floorDiv
    static int[] dec(int a) { int v = a - 2; int dx = Math.floorDiv(v, 2); int j = v - dx * 2; return new int[] {dx, j}; }

    static List<int[]> acciones(int node) {
        LinkedList<int[]> a = new LinkedList<>();
        while (par[node] >= 0) { a.addFirst(dec(act[node])); node = par[node]; }
        return a;
    }

    public static void main(String[] args) {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        int nivel = args.length > 0 ? Integer.parseInt(args[0]) : 1;
        Jugador j = new Jugador(100, Nivel.SUELO_Y - AL);
        Nivel nv = DisenoNiveles.construir(nivel, j, new ArrayList<>());
        W = nv.getAnchoMundo();
        for (Plataforma p : nv.getPlataformas()) {
            plats.add(new int[] {p.getX(), p.getTop(), p.getAncho(), p.getAlto(),
                p.getTipo() == Plataforma.Tipo.TRONCO ? 1 : p.getTipo() == Plataforma.Tipo.FRAGIL ? 2 : 0});
        }
        for (Obstaculo o : nv.getObstaculos()) {
            Rectangle b = o.getBounds();
            if (o instanceof Trampa) traps.add(new int[] {b.x, b.y, b.width, b.height});
            else if (o instanceof Rio) rios.add(new int[] {b.x, b.y, b.width, b.height});
        }
        AnimalAtrapado an = nv.getAnimales().get(0);
        final int ax = an.getX() + an.getAncho() / 2, ay = an.getY() + an.getAlto();
        int porcentaje = 100 * ax / W;
        System.out.println("=== Nivel " + nivel + " (" + W + " px de largo) ===");
        System.out.println("Animal en x=" + ax + " (" + porcentaje + " % del nivel)" + (porcentaje < 85 ? "  <-- AVISO: conviene ponerlo más al final" : ""));

        int meta = bfs(100, Nivel.SUELO_Y - AL, (x, y, vy, g) -> g == 1 && Math.abs(x + 14 - ax) <= 55 && Math.abs(y + AL - ay) <= 40);
        boolean[] ok = new boolean[plats.size()];
        for (int h = 0; h < n; h++) {
            if (gs[h] != 1) continue;
            for (int i = 0; i < plats.size(); i++) {
                int[] p = plats.get(i);
                if (ys[h] + AL == p[1] && xs[h] + AN > p[0] && xs[h] < p[0] + p[2]) ok[i] = true;
            }
        }
        int malas = 0;
        for (int i = 0; i < ok.length; i++) {
            int[] p = plats.get(i);
            if (!ok[i] && p[1] != 520) {
                malas++;
                System.out.println("PROBLEMA: no se puede llegar a la plataforma x=" + p[0] + " top=" + p[1] + " ancho=" + p[2]);
            }
        }
        System.out.println(malas == 0 ? "OK: todas las plataformas se pueden alcanzar." : "Hay " + malas + " plataforma(s) inalcanzable(s).");
        if (meta < 0) {
            System.out.println("PROBLEMA: no existe un camino sin daño hasta el animal (revisa trampas, fosos y saltos muy largos).");
            return;
        }
        List<int[]> ida = acciones(meta);
        System.out.println("OK: hay un camino sin daño hasta el animal (" + ida.size() / 60 + " s aprox.).");

        int sx = xs[meta], sy = ys[meta];
        int regreso = bfs(sx, sy, (x, y, vy, g) -> g == 1 && x <= 100);
        List<int[]> vuelta = new ArrayList<>();
        if (regreso < 0) {
            System.out.println(nivel == 3 ? "Regreso: no se exige en el nivel 3 (después va al campamento del jefe)."
                    : "PROBLEMA: no hay camino sin daño de regreso a la salida.");
        } else {
            vuelta = acciones(regreso);
            System.out.println("OK: hay un camino sin daño de regreso a la salida (" + vuelta.size() / 60 + " s aprox.).");
        }

        Jugador j2 = new Jugador(100, Nivel.SUELO_Y - AL);
        Nivel n2 = DisenoNiveles.construir(nivel, j2, new ArrayList<>());
        n2.getEnemigos().clear();
        for (int[] a : ida) { j2.mover(a[0], a[1] == 1 ? -1 : 0); j2.update(); n2.actualizar(); }
        boolean igual = j2.getX() == sx && j2.getY() == sy && j2.getVida() == 100;
        System.out.println((igual ? "OK" : "AVISO") + ": al repetir el camino en el juego real, Amaru llega a x=" + j2.getX() + " con " + j2.getVida() + " de vida.");
        if (!vuelta.isEmpty()) {
            for (AnimalAtrapado a : n2.getAnimales()) a.marcarLiberado();
            for (int i = 0; i < 3; i++) { j2.update(); n2.actualizar(); }
            n2.getEnemigos().clear();
            for (int[] a : vuelta) { j2.mover(a[0], a[1] == 1 ? -1 : 0); j2.update(); n2.actualizar(); if (n2.estaCompleto()) break; }
            System.out.println((n2.estaCompleto() ? "OK" : "PROBLEMA") + ": al repetir la vuelta en el juego real el nivel " + (n2.estaCompleto() ? "se completa" : "NO se completa") + " con " + j2.getVida() + " de vida.");
        }
        System.out.println("Nota: no se simulan los enemigos; solo trampas, fosos, río y plataformas.");
    }
}
