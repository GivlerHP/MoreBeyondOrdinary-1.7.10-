package ru.givler.mbo.security;
import ru.givler.mbo.lootcontainer.LootContainerConfigValidator;
public class ActionValidationTest {
 public static void main(String[] args) {
  check(LootContainerConfigValidator.areActionsValid("[]"),"empty");
  check(LootContainerConfigValidator.areActionsValid("[{\"type\":\"spawn_entity\",\"chance\":100,\"entityId\":\"Zombie\",\"countExpr\":\"1\"}]"),"normal spawn");
  check(!LootContainerConfigValidator.areActionsValid("[{\"type\":\"spawn_entity\",\"chance\":100,\"entityId\":\"Zombie\",\"countExpr\":\"2147483647\"}]"),"excessive spawn");
  check(!LootContainerConfigValidator.areActionsValid("{}"),"wrong shape");
  check(!LootContainerConfigValidator.areActionsValid("["),"malformed");
  System.out.println("PASS: 5 action validation cases");
 }
 static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);}
}