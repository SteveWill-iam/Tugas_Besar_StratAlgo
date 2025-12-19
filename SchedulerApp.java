
import java.util.*;

public class SchedulerApp {

    // ---------- simple data classes ----------
    static class Musician {

        String name;
        Set<String> skills;
        boolean[] availability; // index = week

        Musician(String name, List<String> skills, boolean[] availability) {
            this.name = name;
            this.skills = new HashSet<>(skills);
            this.availability = availability;
        }

        boolean canPlay(String instrumentBase, int week) {
            if (!skills.contains(instrumentBase)) {
                return false;
            }
            if (week < 0 || week >= availability.length) {
                return false;
            }
            return availability[week];
        }
    }

    static class WeekAssignment {

        // slotName (e.g. Guitar#1) -> musician name
        Map<String, String> assign = new LinkedHashMap<>();
    }

    static class Solution implements Comparable<Solution> {

        WeekAssignment[] weeks;
        int fairness; // higher -> better

        Solution(WeekAssignment[] weeks, int fairness) {
            this.weeks = weeks;
            this.fairness = fairness;
        }

        @Override
        public int compareTo(Solution o) {
            return Integer.compare(o.fairness, this.fairness); // descending
        }
    }

    // ---------- state ----------
    List<String> instrumentsBase = new ArrayList<>();
    Map<String, int[]> instrumentCapacityPerWeek = new LinkedHashMap<>(); // base -> array[week]
    List<List<String>> instrumentSlotsPerWeek = new ArrayList<>(); // per-week expanded slots
    List<Musician> musicians = new ArrayList<>();
    int totalWeeks = 0;

    // search controls/state
    PriorityQueue<Solution> solutionPQ = new PriorityQueue<>();
    long nodesVisited = 0;
    long maxNodesSearch = 200_000;
    int maxSolutionsToFind = 200;

    // ---------- utilities ----------
    void prepareInstrumentSlots() {
        instrumentSlotsPerWeek.clear();
        for (int w = 0; w < totalWeeks; w++) {
            List<String> slots = new ArrayList<>();
            for (String base : instrumentsBase) {
                int cap = 1;
                if (instrumentCapacityPerWeek.containsKey(base)) {
                    int[] arr = instrumentCapacityPerWeek.get(base);
                    if (arr != null && arr.length > w) {
                        cap = Math.max(1, arr[w]);
                    }
                }
                for (int i = 1; i <= cap; i++) {
                    slots.add(base + "#" + i);
                }
            }
            instrumentSlotsPerWeek.add(slots);
        }
    }

    String slotBase(String slotName) {
        int p = slotName.indexOf('#');
        return (p >= 0) ? slotName.substring(0, p) : slotName;
    }

    // ---------- input (dummy + manual) ----------
    void loadDummy() {
        totalWeeks = 4;

        instrumentsBase = Arrays.asList("Gitar", "Bass", "Keyboard", "Drum");
        instrumentCapacityPerWeek.clear();

        for (String inst : instrumentsBase) {
            int[] cap = {1, 1, 1, 1};
            instrumentCapacityPerWeek.put(inst, cap);
        }

        musicians.clear();
        musicians.add(new Musician("A", Arrays.asList("Gitar", "Bass"),
                new boolean[]{false, false, true, true}));
        musicians.add(new Musician("B", Arrays.asList("Gitar", "Bass"),
                new boolean[]{false, false, true, true}));
        musicians.add(new Musician("C", Arrays.asList("Keyboard"),
                new boolean[]{true, true, false, false}));
        musicians.add(new Musician("D", Arrays.asList("Keyboard"),
                new boolean[]{true, true, true, true}));
        musicians.add(new Musician("E", Arrays.asList("Drum"),
                new boolean[]{true, true, true, true}));
        musicians.add(new Musician("F", Arrays.asList("Drum"),
                new boolean[]{true, true, true, true}));
        musicians.add(new Musician("G", Arrays.asList("Gitar"),
                new boolean[]{true, true, true, true}));
        musicians.add(new Musician("H", Arrays.asList("Bass"),
                new boolean[]{true, true, true, true}));
        musicians.add(new Musician("I", Arrays.asList("Bass"),
                new boolean[]{true, true, true, true}));

        prepareInstrumentSlots();
    }

    void loadManualInput(Scanner scan) {
        System.out.print("Masukkan jumlah minggu: ");
        totalWeeks = Integer.parseInt(scan.nextLine().trim());

        System.out.print("Masukkan jumlah instrumen (minimal 4): ");
        int instCount = Integer.parseInt(scan.nextLine().trim());
        instrumentsBase = new ArrayList<>();
        instrumentCapacityPerWeek.clear();

        for (int i = 0; i < instCount; i++) {
            System.out.print("Nama instrumen ke-" + (i + 1) + ": ");
            String name = scan.nextLine().trim();
            instrumentsBase.add(name);
            int[] caps = new int[totalWeeks];
            for (int w = 0; w < totalWeeks; w++) {
                System.out.print("Capacity Minggu-" + (w + 1) + " (berapa pemain untuk instrumen ini tiap minggu) [default 1]: ");
                String c = scan.nextLine().trim();
                int cap = 1;
                try {
                    if (!c.isEmpty()) {
                        cap = Integer.parseInt(c);

                    }
                } catch (Exception e) {
                    cap = 1;
                }
                caps[w] = Math.max(1, cap);
            }
            instrumentCapacityPerWeek.put(name, caps);
        }

        prepareInstrumentSlots();

        System.out.println("========== Input Pemain ==========");
        System.out.print("Masukkan jumlah pemain: ");
        int playerCount = Integer.parseInt(scan.nextLine().trim());
        musicians.clear();
        for (int p = 0; p < playerCount; p++) {
            System.out.print("Nama pemain ke-" + (p + 1) + ": ");
            String name = scan.nextLine().trim();
            System.out.print("Instrument (pisahkan koma jika lebih dari satu, e.g. Guitar,Piano): ");
            String skillLine = scan.nextLine().trim();
            List<String> skills = new ArrayList<>();
            if (!skillLine.isEmpty()) {
                for (String s : skillLine.split(",")) {
                    skills.add(s.trim());
                }
            }
            boolean[] avail = new boolean[totalWeeks];
            for (int w = 0; w < totalWeeks; w++) {
                System.out.print("Apakah " + name + " tersedia di minggu " + (w + 1) + "? (y/n) [default y]: ");
                String a = scan.nextLine().trim();
                if (a.isEmpty() || a.equalsIgnoreCase("y")) {
                    avail[w] = true;
                } else {
                    avail[w] = false;
                }
            }
            musicians.add(new Musician(name, skills, avail));
            System.out.println("================================");
        }

        System.out.println("=== INPUT SELESAI ===");
    }

    // ---------- generate all choices for a single week (backtracking over slots) ----------
    List<WeekAssignment> generateWeekChoices(int week) {
        List<WeekAssignment> result = new ArrayList<>();
        backtrackWeekSlots(0, week, new WeekAssignment(), new HashSet<>(), result);
        return result;
    }

    void backtrackWeekSlots(int slotIdx, int week, WeekAssignment curr, Set<String> usedMusicians, List<WeekAssignment> result) {
        List<String> slots = instrumentSlotsPerWeek.get(week);
        if (slotIdx >= slots.size()) {
            WeekAssignment copy = new WeekAssignment();
            copy.assign.putAll(curr.assign);
            result.add(copy);
            return;
        }

        String slot = slots.get(slotIdx);
        String base = slotBase(slot);

        for (Musician m : musicians) {
            if (!m.canPlay(base, week)) {
                continue;
            }
            if (usedMusicians.contains(m.name)) {
                continue; // one musician only once per week

            }
            usedMusicians.add(m.name);
            curr.assign.put(slot, m.name);
            backtrackWeekSlots(slotIdx + 1, week, curr, usedMusicians, result);
            usedMusicians.remove(m.name);
            curr.assign.remove(slot);
            // bound: if we already collected many choices, optional early exit (not used here)
            if (result.size() > 5000) {
                return; // safety guard

            }
        }
    }

    // ---------- small forward-check used in bounded DFS ----------
    boolean quickForwardCheck(int depth, Integer[] weekOrder, WeekAssignment chosen) {
        for (int d = depth + 1; d < weekOrder.length; d++) {
            int futureWeek = weekOrder[d];
            for (String base : instrumentsBase) {
                boolean any = false;
                for (Musician m : musicians) {
                    if (m.canPlay(base, futureWeek)) {
                        any = true;
                        break;
                    }
                }
                if (!any) {
                    return false;
                }
            }
        }
        return true;
    }

    // ---------- bounded DFS across weeks (MRV ordering) ----------
    void generateAllSolutionsBounded() {
        nodesVisited = 0;
        solutionPQ.clear();

        int[] domainSizes = new int[totalWeeks];
        for (int w = 0; w < totalWeeks; w++) {
            List<WeekAssignment> ch = generateWeekChoices(w);
            domainSizes[w] = ch.size();
            ch.clear();
            if (domainSizes[w] == 0) {
                return; // impossible

            }
        }

        Integer[] weekOrder = new Integer[totalWeeks];
        for (int i = 0; i < totalWeeks; i++) {
            weekOrder[i] = i;
        }
        Arrays.sort(weekOrder, Comparator.comparingInt(a -> domainSizes[a])); // MRV

        WeekAssignment[] partial = new WeekAssignment[totalWeeks];
        dfsWeekOrderBounded(0, weekOrder, partial);
    }

    void dfsWeekOrderBounded(int depth, Integer[] weekOrder, WeekAssignment[] partial) {
        if (nodesVisited > maxNodesSearch) {
            return;
        }
        if (depth >= weekOrder.length) {
            // full assignment -> evaluate fairness
            WeekAssignment[] sol = Arrays.copyOf(partial, partial.length);
            int fair = computeFairness(sol);
            solutionPQ.offer(new Solution(sol, fair));
            if (solutionPQ.size() > maxSolutionsToFind) {
                solutionPQ.poll();
            }
            return;
        }

        int week = weekOrder[depth];
        List<WeekAssignment> choices = generateWeekChoices(week);
        nodesVisited += choices.size();
        if (choices.isEmpty()) {
            return;
        }

        for (WeekAssignment choice : choices) {
            partial[week] = choice;
            if (!quickForwardCheck(depth, weekOrder, choice)) {
                continue;
            }
            dfsWeekOrderBounded(depth + 1, weekOrder, partial);
            if (nodesVisited > maxNodesSearch) {
                break;
            }
        }
    }

    // ---------- Greedy fallback (guarantee some solution if possible) ----------
    WeekAssignment[] generateGreedySolution() {
        WeekAssignment[] schedule = new WeekAssignment[totalWeeks];
        for (int w = 0; w < totalWeeks; w++) {
            WeekAssignment wa = new WeekAssignment();
            Set<String> used = new HashSet<>();
            boolean fail = false;
            for (String slot : instrumentSlotsPerWeek.get(w)) {
                String base = slotBase(slot);
                boolean assigned = false;
                for (Musician m : musicians) {
                    if (!used.contains(m.name) && m.canPlay(base, w)) {
                        wa.assign.put(slot, m.name);
                        used.add(m.name);
                        assigned = true;
                        break;
                    }
                }
                if (!assigned) {
                    fail = true;
                    break;
                }
            }
            if (fail) {
                return null;
            }
            schedule[w] = wa;
        }
        return schedule;
    }

    // ---------- fairness / optimality heuristics ----------
    int countWeekSimilarity(WeekAssignment a, WeekAssignment b) {
        int same = 0;
        for (String slot : a.assign.keySet()) {
            if (b.assign.containsKey(slot) && a.assign.get(slot).equals(b.assign.get(slot))) {
                same++;
            }
        }
        return same;
    }

    int computeOptimality(WeekAssignment[] schedule) {
        int totalWeeks = schedule.length;
        int similarityCount = 0;
        int comparisons = 0;
        int maxSimilarity = 0;

        for (int w1 = 0; w1 < totalWeeks; w1++) {
            for (int w2 = w1 + 1; w2 < totalWeeks; w2++) {
                comparisons++;
                similarityCount += countWeekSimilarity(schedule[w1], schedule[w2]);
                int overlap = 0;
                for (String base : instrumentsBase) {
                    int cap1 = 1, cap2 = 1;
                    if (instrumentCapacityPerWeek.containsKey(base)) {
                        int[] arr = instrumentCapacityPerWeek.get(base);
                        if (arr != null && arr.length > w1) {
                            cap1 = Math.max(1, arr[w1]);
                        }
                        if (arr != null && arr.length > w2) {
                            cap2 = Math.max(1, arr[w2]);
                        }
                    }
                    overlap += Math.min(cap1, cap2);
                }
                maxSimilarity += overlap;
            }
        }

        if (comparisons == 0 || maxSimilarity == 0) {
            return 100;
        }
        double ratio = 1.0 - (similarityCount / (double) maxSimilarity);
        return (int) (ratio * 100);
    }

    int computeFairness(WeekAssignment[] schedule) {
        // simple fairness: prefer more diverse players across weeks and balanced usage
        int optimality = computeOptimality(schedule);
        // usage balance (how evenly players distributed) could be added; keep simple
        return optimality;
    }

    // ---------- printing ----------
    void printSolution(Solution sol, int index) {
        if (sol == null) {
            System.out.println("(no solution)");
            return;
        }
        System.out.println("\n=== Solusi ke-" + index + " (fairness=" + sol.fairness + ") ===");
        for (int w = 0; w < sol.weeks.length; w++) {
            WeekAssignment wa = sol.weeks[w];
            System.out.println("Week " + (w + 1) + ":");
            if (wa == null) {
                System.out.println("  (unassigned)");
                continue;
            }
            Map<String, List<String>> grouped = new LinkedHashMap<>();
            for (String slot : wa.assign.keySet()) {
                String base = slotBase(slot);
                grouped.putIfAbsent(base, new ArrayList<>());
                grouped.get(base).add(wa.assign.get(slot));
            }
            for (String base : instrumentsBase) {
                List<String> players = grouped.getOrDefault(base, Collections.emptyList());
                if (players.isEmpty()) {
                    System.out.println("  " + base + " -> (unassigned)");
                } else {
                    System.out.println("  " + base + " -> " + String.join(", ", players));
                }
            }
            System.out.println();
        }
    }

    // ---------- CLI ----------
    public static void main(String[] args) {
        SchedulerApp app = new SchedulerApp();
        Scanner scan = new Scanner(System.in);
        System.out.println("Pilihan input: (1) Dummy contoh, (2) Manual");
        System.out.print("Pilih: ");
        String choice = scan.nextLine().trim();
        if (choice.equals("2")) {
            app.loadManualInput(scan);
        } else {
            app.loadDummy();
        }

        // search for solutions
        app.generateAllSolutionsBounded();

        if (app.solutionPQ.isEmpty()) {
            System.out.println("Tidak ditemukan solusi otomatis. Mencoba greedy fallback...");
            WeekAssignment[] g = app.generateGreedySolution();
            if (g == null) {
                System.out.println("Tidak ditemukan solusi sama sekali.");
                return;
            } else {
                Solution sol = new Solution(g, app.computeFairness(g));
                app.printSolution(sol, 1);
                return;
            }
        }

        // print best solution first
        System.out.println("\n=== Solusi Otomatis Terbaik (pertama) ===");
        int counter = 1;
        app.printSolution(app.solutionPQ.peek(), counter);

        // interactive loop for more solutions
        while (true) {
            System.out.print("Tekan Enter untuk solusi selanjutnya, ketik 'q' lalu Enter untuk keluar: ");
            String in = scan.nextLine();
            if (in.equalsIgnoreCase("q")) {
                break;
            }
            Solution next = app.solutionPQ.poll();
            if (next == null) {
                System.out.println("Tidak ada solusi lagi.");
                continue;
            }
            counter++;
            app.printSolution(next, counter);
        }

        System.out.println("Program selesai.");
    }
}
