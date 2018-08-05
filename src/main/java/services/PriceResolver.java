package services;

import model.Price;
import model.PriceKey;

import java.util.*;
import java.util.stream.Collectors;

public class PriceResolver {

    public List<Price> mergePrices(List<Price> oldPrices, List<Price> newPrices) {
        if (oldPrices == null || newPrices == null) {
            throw new IllegalArgumentException("List of prices can't be null.");
        }
        newPrices.forEach(this::validatePrice);

        //grouping by unique key: productCode, number, department
        Map<PriceKey, List<Price>> oldPriceMap = oldPrices.stream().collect(Collectors.groupingBy(this::createKey));
        Map<PriceKey, List<Price>> newPriceMap = newPrices.stream().collect(Collectors.groupingBy(this::createKey));

        oldPriceMap.forEach((key, value) -> {
            List<Price> newPricesForKey = newPriceMap.get(key);
            if (newPricesForKey != null) {
                newPricesForKey.forEach(p -> addNewPrice(value, p));
            }
        });
        List<Price> resultPrices  = oldPriceMap.values().stream().flatMap(List::stream).collect(Collectors.toList());

        //add new prices which not found in old prices by key
        newPriceMap.entrySet().stream()
                .filter(e -> oldPriceMap.get(e.getKey()) == null || oldPriceMap.get(e.getKey()).isEmpty())
                .forEach(e -> resultPrices.addAll(e.getValue()));

        return resultPrices;
    }

    private PriceKey createKey(Price p) {
        return new PriceKey(p.getProductCode(), p.getNumber(), p.getDepartment());
    }

    private void addNewPrice(List<Price> oldPrices, Price newPrice) {
        List<Price> commonPeriodPrices = oldPrices.stream()
                .filter(p -> between(newPrice.getBegin(), p.getBegin(), p.getEnd())
                          || between(p.getBegin(), newPrice.getBegin(), newPrice.getEnd()))
                .collect(Collectors.toList());

        if (commonPeriodPrices.isEmpty()) {
            oldPrices.add(newPrice);
        } else {
            //increase price's period for prices with the same value
            commonPeriodPrices.stream().filter(p -> p.getValue() == newPrice.getValue())
                    .forEach(oldPrice -> {
                                oldPrice.setBegin(new Date(Math.min(oldPrice.getBegin().getTime(), newPrice.getBegin().getTime())));
                                oldPrice.setEnd(new Date(Math.max(oldPrice.getEnd().getTime(), newPrice.getEnd().getTime())));
                            }
                    );
            commonPeriodPrices.stream().filter(oldPrice -> oldPrice.getValue() != newPrice.getValue())
                    .forEach(oldPrice -> mergeTwoPricesWithDifferentValues(oldPrices, newPrice, oldPrice));

        }
    }

    private boolean between(Date betweenPoint, Date begin, Date end) {
        return !betweenPoint.before(begin) && betweenPoint.before(end);
    }

    private void mergeTwoPricesWithDifferentValues(List<Price> oldPrices, Price newPrice, Price oldPrice) {
        oldPrices.add(newPrice);
        if (oldPrice.getBegin().before(newPrice.getBegin())) {
            if (oldPrice.getEnd().after(newPrice.getEnd())) {
                oldPrices.add(new Price(oldPrice.getId(), oldPrice.getProductCode(), oldPrice.getNumber(),
                        oldPrice.getDepartment(), newPrice.getEnd(), oldPrice.getEnd(), oldPrice.getValue()));
            }
            oldPrice.setEnd(newPrice.getBegin());
        } else if (oldPrice.getEnd().after(newPrice.getEnd())) {
            oldPrice.setBegin(newPrice.getEnd());
        } else {
            oldPrices.remove(oldPrice);
        }
    }

    private void validatePrice(Price price) throws IllegalArgumentException {
        if (price.getBegin().compareTo(price.getEnd()) >= 0) {
            throw new IllegalArgumentException("The begin date should be before the end date of price.");
        }
        if (price.getValue() < 0) {
            throw new IllegalArgumentException("The value of price can't be less 0.");
        }
    }
}
