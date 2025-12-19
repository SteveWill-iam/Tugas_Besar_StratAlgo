Test Case Dummy

==================================================================
Test Case 1

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


==================================================================
Test Case 2

void loadDummy() {
    totalWeeks = 2;

    instrumentsBase = Arrays.asList("Gitar", "Bass", "Keyboard", "Drum");
    instrumentCapacityPerWeek.clear();

    for (String inst : instrumentsBase) {
        int[] cap = {1, 1};
        instrumentCapacityPerWeek.put(inst, cap);
    }

    musicians.clear();
    musicians.add(new Musician("A", Arrays.asList("Gitar"),
            new boolean[]{false, true}));
    musicians.add(new Musician("B", Arrays.asList("Bass"),
            new boolean[]{false, true}));
    musicians.add(new Musician("C", Arrays.asList("Keyboard"),
            new boolean[]{false, true}));
    musicians.add(new Musician("D", Arrays.asList("Drum"),
            new boolean[]{false, true}));
    musicians.add(new Musician("E", Arrays.asList("Gitar"),
            new boolean[]{true, false}));
    musicians.add(new Musician("F", Arrays.asList("Bass"),
            new boolean[]{true, false}));
    musicians.add(new Musician("G", Arrays.asList("Keyboard"),
            new boolean[]{true, false}));
    musicians.add(new Musician("H", Arrays.asList("Drum"),
            new boolean[]{true, false}));

    prepareInstrumentSlots();
}


==================================================================
Test Case 3

void loadDummy() {
    totalWeeks = 2;

    instrumentsBase = Arrays.asList("Gitar", "Bass", "Keyboard", "Drum");
    instrumentCapacityPerWeek.clear();

    instrumentCapacityPerWeek.put("Gitar",    new int[]{2, 1});
    instrumentCapacityPerWeek.put("Bass",     new int[]{1, 1});
    instrumentCapacityPerWeek.put("Keyboard", new int[]{1, 2});
    instrumentCapacityPerWeek.put("Drum",     new int[]{1, 1});

    musicians.clear();
    musicians.add(new Musician("A", Arrays.asList("Gitar", "Bass"),
            new boolean[]{false, true}));
    musicians.add(new Musician("B", Arrays.asList("Gitar", "Bass"),
            new boolean[]{true, false}));
    musicians.add(new Musician("C", Arrays.asList("Keyboard"),
            new boolean[]{false, true}));
    musicians.add(new Musician("D", Arrays.asList("Keyboard"),
            new boolean[]{true, true}));
    musicians.add(new Musician("E", Arrays.asList("Drum"),
            new boolean[]{true, false}));
    musicians.add(new Musician("F", Arrays.asList("Drum"),
            new boolean[]{false, true}));
    musicians.add(new Musician("G", Arrays.asList("Gitar"),
            new boolean[]{true, false}));
    musicians.add(new Musician("H", Arrays.asList("Bass"),
            new boolean[]{true, false}));
    musicians.add(new Musician("I", Arrays.asList("Bass"),
            new boolean[]{false, true}));

    prepareInstrumentSlots();
}

