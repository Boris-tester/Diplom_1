package praktikum;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.experimental.runners.Enclosed;

import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class BurgerTest {

    public static class BasicOperationsTest {

        private Burger burger;

        @Before
        public void setUp() {
            burger = new Burger();
        }

        @Test
        public void setBuns_setsBunField() {
            Bun bun = mock(Bun.class);

            burger.setBuns(bun);

            assertSame("Bun должен быть установлен в поле bun", bun, burger.bun);
        }

        @Test
        public void addIngredient_addsIngredientToList() {
            Ingredient ingredient = mock(Ingredient.class);

            burger.addIngredient(ingredient);

            assertEquals(1, burger.ingredients.size());
            assertSame(ingredient, burger.ingredients.get(0));
        }

        @Test
        public void removeIngredient_removesIngredientByIndex() {
            Ingredient first = mock(Ingredient.class);
            Ingredient second = mock(Ingredient.class);

            burger.addIngredient(first);
            burger.addIngredient(second);

            burger.removeIngredient(0);

            assertEquals(1, burger.ingredients.size());
            assertSame(second, burger.ingredients.get(0));
        }

        @Test
        public void moveIngredient_movesIngredientToNewIndex() {
            Ingredient first = mock(Ingredient.class);
            Ingredient second = mock(Ingredient.class);
            Ingredient third = mock(Ingredient.class);

            burger.addIngredient(first);
            burger.addIngredient(second);
            burger.addIngredient(third);

            // переместим "second" (index 1) на позицию 0
            burger.moveIngredient(1, 0);

            assertEquals(Arrays.asList(second, first, third), burger.ingredients);
        }

        @Test
        public void getPrice_withNoIngredients_returnsBunPriceTimesTwo() {
            Bun bun = mock(Bun.class);
            when(bun.getPrice()).thenReturn(100f);

            burger.setBuns(bun);

            float price = burger.getPrice();

            assertEquals(200f, price, 0.0001f);
            verify(bun, times(1)).getPrice();
        }

        @Test
        public void getPrice_withIngredients_returnsBunTimesTwoPlusIngredientsSum() {
            Bun bun = mock(Bun.class);
            when(bun.getPrice()).thenReturn(100f);

            Ingredient ingredient1 = mock(Ingredient.class);
            when(ingredient1.getPrice()).thenReturn(50f);

            Ingredient ingredient2 = mock(Ingredient.class);
            when(ingredient2.getPrice()).thenReturn(70f);

            burger.setBuns(bun);
            burger.addIngredient(ingredient1);
            burger.addIngredient(ingredient2);

            float price = burger.getPrice();

            float expected = 100f * 2 + 50f + 70f; // 320
            assertEquals(expected, price, 0.0001f);

            verify(bun, atLeastOnce()).getPrice();
            verify(ingredient1, times(1)).getPrice();
            verify(ingredient2, times(1)).getPrice();
        }


        @Test
        public void getReceipt_withNoIngredients_containsHeaderFooterAndPrice() {
            Bun bun = mock(Bun.class);
            when(bun.getName()).thenReturn("black bun");
            when(bun.getPrice()).thenReturn(100f);

            burger.setBuns(bun);

            String receipt = burger.getReceipt();

            String header = String.format("(==== %s ====)%n", "black bun");
            assertTrue(receipt.startsWith(header));
            assertTrue(receipt.contains(header)); // в чеке заголовок повторяется в конце

            float expectedPrice = 200f;
            assertTrue(receipt.contains(String.format("%nPrice: %f%n", expectedPrice)));

            verify(bun, atLeastOnce()).getName();
            verify(bun, atLeastOnce()).getPrice();
        }
    }

    @RunWith(Parameterized.class)
    public static class ReceiptParameterizedTest {

        private final IngredientType type;
        private final String expectedLower;

        public ReceiptParameterizedTest(IngredientType type, String expectedLower) {
            this.type = type;
            this.expectedLower = expectedLower;
        }

        @Parameterized.Parameters(name = "{index}: type={0} -> {1}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {IngredientType.SAUCE, "sauce"},
                    {IngredientType.FILLING, "filling"}
            });
        }

        @Test
        public void getReceipt_containsIngredientLineWithLowercaseType() {
            Burger burger = new Burger();

            Bun bun = mock(Bun.class);
            when(bun.getName()).thenReturn("black bun");
            when(bun.getPrice()).thenReturn(100f);

            Ingredient ingredient = mock(Ingredient.class);
            when(ingredient.getType()).thenReturn(type);
            when(ingredient.getName()).thenReturn("hot sauce");
            when(ingredient.getPrice()).thenReturn(50f);

            burger.setBuns(bun);
            burger.addIngredient(ingredient);

            String receipt = burger.getReceipt();

            // строка ингредиента формируется так:
            // "= sauce hot sauce =\n" или "= filling hot sauce =\n"
            String expectedLine = String.format("= %s %s =%n", expectedLower, "hot sauce");
            assertTrue(receipt.contains(expectedLine));

            float expectedPrice = 250f; // bun*2 (200) + ingredient (50)
            assertTrue(receipt.contains(String.format("%nPrice: %f%n", expectedPrice)));

            verify(ingredient, atLeastOnce()).getType();
            verify(ingredient, atLeastOnce()).getName();
            verify(ingredient, atLeastOnce()).getPrice();
        }
    }
}
