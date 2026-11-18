package ru.givler.mbo.lootcontainer.action;

import ru.givler.mbo.security.WorldTickBudget;

public final class SpawnBudgetTest {
    public static void main(String[] args) {
        WorldTickBudget budget = new WorldTickBudget(512);
        Object world = new Object();
        check(budget.reserve(world, 10, 256) == 256);
        check(budget.reserve(world, 10, 250) == 250);
        check(budget.reserve(world, 10, 256) == 6);
        check(budget.reserve(world, 10, 1) == 0);
        check(budget.reserve(new Object(), 10, 256) == 256);
        check(budget.reserve(world, 11, 256) == 256);
        WorldTickBudget actions = new WorldTickBudget(128);
        check(actions.reserve(world, 11, 127) == 127);
        check(actions.reserve(world, 11, 8) == 1);
        check(actions.reserve(world, 11, 1) == 0);
        check(actions.reserve(world, 12, 8) == 8);
        System.out.println("PASS: shared spawn budget, world isolation and next tick reset");
    }

    private static void check(boolean valid) {
        if (!valid) throw new AssertionError("Unexpected spawn budget");
    }
}
