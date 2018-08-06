import model.Price;
import org.junit.Before;
import org.junit.Test;
import services.PriceResolver;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.MatcherAssert.assertThat;

public class PriceResolverTest {

    private PriceResolver priceService = new PriceResolver();

    DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

    String productCode1 = "productCode1";
    String productCode2 = "productCode2";
    String productCode3 = "productCode3";

    long firstDepartment = 1;
    long secondDepartment = 2;

    Price price11_1001_3101;
    Price price12_1001_2001;
    Price price12_1501_2501;
    Price price12_0501_1501;
    Price price12_0501_3001;
    Price price12_1501_1601;
    Price price12_1502_2002;
    Price price12_0601_1501;
    Price price11_2001_2002;
    Price price12_1601_2501;
    Price price21_0101_3101;
    Price price21_1201_1301;
    Price price31_2002_1503;
    Price price31_0502_2503;

    List<Price> oldPrices = new ArrayList<>();
    List<Price> newPrices = new ArrayList<>();

    @Before
    public void init() {
        createPrices();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowNullPrices() {
        priceService.mergePrices(null, null);
    }

    @Test
    public void shouldMergeOnlyOldPrices() {
        oldPrices.add(price11_1001_3101);

        List<Price> prices =  priceService.mergePrices(oldPrices, newPrices);

        assertThat(prices.size(), is(1));
        assertThat(prices, contains(price11_1001_3101));
    }

    @Test
    public void shouldMergeOnlyNewPrices() {
        newPrices.add(price11_1001_3101);

        List<Price> prices =  priceService.mergePrices(oldPrices, newPrices);

        assertThat(prices.size(), is(1));
        assertThat(prices, contains(price11_1001_3101));
    }

    @Test
    public void shouldMergeNotCommonPrices() {
        oldPrices.add(price12_1001_2001);
        newPrices.add(price12_1502_2002);

        List<Price> prices =  priceService.mergePrices(oldPrices, newPrices);

        assertThat(prices.size(), is(2));
        assertThat(prices, containsInAnyOrder(price12_1001_2001, price12_1502_2002));

        oldPrices = Collections.singletonList(price12_1502_2002);
        newPrices = Collections.singletonList(price12_1001_2001);

        prices =  priceService.mergePrices(oldPrices, newPrices);

        assertThat(prices.size(), is(2));
        assertThat(prices, containsInAnyOrder(price12_1001_2001, price12_1502_2002));
    }

    @Test
    public void shouldAddUniqueNewPrices() {
        oldPrices.add(price11_1001_3101);
        oldPrices.add(price21_0101_3101);
        newPrices.add(price31_2002_1503);

        List<Price> prices =  priceService.mergePrices(oldPrices, newPrices);

        assertThat(prices.size(), is(3));
        assertThat(prices, containsInAnyOrder(price11_1001_3101, price21_0101_3101, price31_2002_1503));
    }

    @Test
    public void shouldMergeWithTheSamePrices() {
        oldPrices.add(price12_1001_2001);
        newPrices.add(price12_1501_2501);
        List<Price> prices = priceService.mergePrices(oldPrices, newPrices);

        assertThat(prices.size(), is(1));
        assertTrue(prices.stream().anyMatch(p -> p.getBegin().equals(price12_1001_2001.getBegin()) && p.getEnd().equals(price12_1501_2501.getEnd())));

        newPrices = Collections.singletonList(price12_0501_1501);
        prices = priceService.mergePrices(oldPrices, newPrices);

        assertThat(prices.size(), is(1));
        assertTrue(prices.stream().anyMatch(p -> p.getBegin().equals(price12_0501_1501.getBegin()) && p.getEnd().equals(price12_1001_2001.getEnd())));

        newPrices = Collections.singletonList(price12_0501_3001);
        prices = priceService.mergePrices(oldPrices, newPrices);

        assertThat(prices.size(), is(1));
        assertTrue(prices.stream().anyMatch(p -> p.getBegin().equals(price12_0501_3001.getBegin()) && p.getEnd().equals(price12_0501_3001.getEnd())));

        newPrices = Collections.singletonList(price12_1501_1601);
        prices = priceService.mergePrices(oldPrices, newPrices);

        assertThat(prices.size(), is(1));
        assertTrue(prices.stream().anyMatch(p -> p.getBegin().equals(price12_1001_2001.getBegin()) && p.getEnd().equals(price12_1001_2001.getEnd())));
    }

    @Test
    public void shouldMergeThreeWithTheSamePrices() {
        oldPrices.add(price12_1001_2001);
        newPrices.add(price12_1501_2501);
        newPrices.add(price12_0501_1501);
        List<Price> prices = priceService.mergePrices(oldPrices, newPrices);

        assertThat(prices.size(), is(1));
        assertTrue(prices.stream().anyMatch(p -> p.getBegin().equals(price12_0501_1501.getBegin()) && p.getEnd().equals(price12_1501_2501.getEnd())));
    }

    @Test
    public void shouldMergeWideNewTheSamePrices() {
        oldPrices.add(price12_0501_1501);
        oldPrices.add(price12_1501_1601);
        newPrices.add(price12_0501_3001);
        List<Price> prices = priceService.mergePrices(oldPrices, newPrices);

        assertThat(prices.size(), is(1));
        assertTrue(prices.stream().anyMatch(p -> p.getBegin().equals(price12_0501_3001.getBegin()) && p.getEnd().equals(price12_0501_3001.getEnd())));
    }

    @Test
    public void shouldMergeEdgeConditionSamePrices() {
        oldPrices.add(price12_0501_1501);
        newPrices.add(price12_1501_1601);
        List<Price> prices = priceService.mergePrices(oldPrices, newPrices);

        assertThat(prices.size(), is(1));
        assertTrue(prices.stream().anyMatch(p -> p.getBegin().equals(price12_0501_1501.getBegin()) && p.getEnd().equals(price12_1501_1601.getEnd())));

        oldPrices = Collections.singletonList(price12_1501_1601);
        newPrices = Collections.singletonList(price12_0501_1501);
        prices = priceService.mergePrices(oldPrices, newPrices);

        assertThat(prices.size(), is(1));
        assertTrue(prices.stream().anyMatch(p -> p.getBegin().equals(price12_0501_1501.getBegin()) && p.getEnd().equals(price12_1501_1601.getEnd())));
    }

    @Test
    public void shouldMergeWithDifferentPrices() {
        oldPrices.add(price12_1001_2001);
        newPrices.add(price12_0601_1501);

        List<Price> prices = priceService.mergePrices(oldPrices, newPrices);

        assertThat(prices.size(), is(2));
        assertTrue(prices.stream().anyMatch(p -> p.getBegin().equals(price12_0601_1501.getBegin()) && p.getEnd().equals(price12_0601_1501.getEnd())
                && p.getValue() == price12_0601_1501.getValue()));
        assertTrue(prices.stream().anyMatch(p -> p.getBegin().equals(price12_0601_1501.getEnd()) && p.getEnd().equals(price12_1001_2001.getEnd())
                && p.getValue() == price12_1001_2001.getValue()));
    }

    @Test
    public void shouldMergeWideNewPrices() {
        oldPrices.add(price31_2002_1503);
        newPrices.add(price31_0502_2503);
        List<Price> prices = priceService.mergePrices(oldPrices, newPrices);

        assertThat(prices.size(), is(1));
        assertThat(prices, contains(price31_0502_2503));
    }

    @Test
    public void shouldMergeAllPrices() {
        oldPrices.add(price11_1001_3101);
        oldPrices.add(price12_1001_2001);
        oldPrices.add(price21_0101_3101);

        newPrices.add(price11_2001_2002);
        newPrices.add(price12_1601_2501);
        newPrices.add(price21_1201_1301);
        Date oldEnd = price21_0101_3101.getEnd();

        List<Price> prices = priceService.mergePrices(oldPrices, newPrices);

        assertThat(prices.size(), is(6));

        assertTrue(prices.stream().anyMatch(p -> p.getProductCode().equals(productCode1) && p.getNumber() == 1
                && p.getBegin().equals(price11_1001_3101.getBegin()) && p.getEnd().equals(price11_2001_2002.getEnd()) && p.getValue() == price11_1001_3101.getValue()));

        assertTrue(prices.stream().anyMatch(p -> p.getProductCode().equals(productCode1) && p.getNumber() == 2
                && p.getBegin().equals(price12_1001_2001.getBegin()) && p.getEnd().equals(price12_1601_2501.getBegin()) && p.getValue() == price12_1001_2001.getValue()));

        assertTrue(prices.stream().anyMatch(p -> p.getProductCode().equals(productCode1) && p.getNumber() == 2
                && p.getBegin().equals(price12_1601_2501.getBegin()) && p.getEnd().equals(price12_1601_2501.getEnd()) && p.getValue() == price12_1601_2501.getValue()));

        assertTrue(prices.stream().anyMatch(p -> p.getProductCode().equals(productCode2) && p.getNumber() == 1
                && p.getBegin().equals(price21_0101_3101.getBegin()) && p.getEnd().equals(price21_1201_1301.getBegin()) && p.getValue() == price21_0101_3101.getValue()));

        assertTrue(prices.stream().anyMatch(p -> p.getProductCode().equals(productCode2) && p.getNumber() == 1
                && p.getBegin().equals(price21_1201_1301.getBegin()) && p.getEnd().equals(price21_1201_1301.getEnd()) && p.getValue() == price21_1201_1301.getValue()));

        assertTrue(prices.stream().anyMatch(p -> p.getProductCode().equals(productCode2) && p.getNumber() == 1
                && p.getBegin().equals(price21_1201_1301.getEnd()) && p.getEnd().equals(oldEnd) && p.getValue() == price21_0101_3101.getValue()));
    }

    private void createPrices() {
        try {
            price11_1001_3101 = new Price(1, productCode1, 1, firstDepartment, formatter.parse("01.01.2013 00:00:00"), formatter.parse("31.01.2013 00:00:00"), 11000F);
            price12_1001_2001 = new Price(2, productCode1, 2, firstDepartment, formatter.parse("10.01.2013 00:00:00"), formatter.parse("20.01.2013 23:59:59"), 99000F);

            price12_1501_2501 = new Price(3, productCode1, 2, firstDepartment, formatter.parse("15.01.2013 00:00:00"), formatter.parse("25.01.2013 23:59:59"), 99000F);
            price12_0501_1501 = new Price(4, productCode1, 2, firstDepartment, formatter.parse("05.01.2013 00:00:00"), formatter.parse("15.01.2013 23:59:59"), 99000F);
            price12_0501_3001 = new Price(5, productCode1, 2, firstDepartment, formatter.parse("5.01.2013 00:00:00"), formatter.parse("30.01.2013 23:59:59"), 99000F);
            price12_1501_1601 = new Price(6, productCode1, 2, firstDepartment, formatter.parse("15.01.2013 00:00:00"), formatter.parse("16.01.2013 23:59:59"), 99000F);
            price12_1502_2002 = new Price(7, productCode1, 2, firstDepartment, formatter.parse("15.02.2013 00:00:00"), formatter.parse("20.02.2013 23:59:59"), 99000F);

            price11_2001_2002 = new Price(8, productCode1, 1, firstDepartment, formatter.parse("20.01.2013 00:00:00"), formatter.parse("20.02.2013 23:59:59"), 11000F);
            price12_1601_2501 = new Price(9, productCode1, 2, firstDepartment, formatter.parse("16.01.2013 00:00:00"), formatter.parse("25.01.2013 23:59:59"), 92000F);

            price21_0101_3101 = new Price(10, productCode2, 1, secondDepartment, formatter.parse("01.01.2013 00:00:00"), formatter.parse("31.01.2013 00:00:00"), 5000F);
            price21_1201_1301 = new Price(11, productCode2, 1, secondDepartment, formatter.parse("12.01.2013 00:00:00"), formatter.parse("13.01.2013 00:00:00"), 4000F);

            price31_2002_1503 = new Price(12, productCode3, 2, secondDepartment, formatter.parse("20.02.2013 00:00:00"), formatter.parse("15.03.2013 00:00:00"), 7000F);
            price31_0502_2503 = new Price(12, productCode3, 2, secondDepartment, formatter.parse("05.02.2013 00:00:00"), formatter.parse("25.03.2013 00:00:00"), 6000F);

            price12_0601_1501 = new Price(13, productCode1, 2, firstDepartment, formatter.parse("06.01.2013 00:00:00"), formatter.parse("15.01.2013 23:59:59"), 88000F);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
