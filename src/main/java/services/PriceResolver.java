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

        newPriceMap.forEach((key, value) -> {
            List<Price> oldPricesByKey = oldPriceMap.computeIfAbsent(key, (x -> new ArrayList<>()));
            value.forEach(newPrice -> mergeNewPriceToOld(oldPricesByKey, newPrice));
        });

        return oldPriceMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    private void mergeNewPriceToOld(List<Price> oldPrices, Price newPrice) {
        List<Price> commonPeriodPrices = oldPrices.stream()
                .filter(p -> between(newPrice.getBegin(), p.getBegin(), p.getEnd())
                        || between(p.getBegin(), newPrice.getBegin(), newPrice.getEnd()))
                .collect(Collectors.toList());

        if (commonPeriodPrices.isEmpty()) {
            oldPrices.add(newPrice);
        } else {
            List<Price> oldSameVal = commonPeriodPrices.stream()
                    .filter(p -> p.getValue() == newPrice.getValue()).collect(Collectors.toList());
            if (!oldSameVal.isEmpty()) {
                mergePricesWithTheSameValue(oldPrices, oldSameVal, newPrice);
            }

            commonPeriodPrices.stream().filter(oldPrice -> oldPrice.getValue() != newPrice.getValue())
                    .forEach(oldPrice -> mergeTwoPricesWithDifferentValues(oldPrices, newPrice, oldPrice));
        }
    }

    private PriceKey createKey(Price p) {
        return new PriceKey(p.getProductCode(), p.getNumber(), p.getDepartment());
    }

    private void mergePricesWithTheSameValue(List<Price> oldPrices, List<Price> oldSameVal, Price newPrice) {
        long minOld = oldSameVal.stream().mapToLong(p -> p.getBegin().getTime()).min().getAsLong();
        newPrice.setBegin(new Date(Math.min(minOld, newPrice.getBegin().getTime())));

        long maxOld = oldSameVal.stream().mapToLong(p -> p.getEnd().getTime()).max().getAsLong();
        newPrice.setEnd(new Date(Math.max(maxOld, newPrice.getEnd().getTime())));

        oldPrices.removeAll(oldSameVal);
        oldPrices.add(newPrice);
    }

    private boolean between(Date betweenPoint, Date begin, Date end) {
        return !betweenPoint.before(begin) && betweenPoint.before(end);
    }

    private void mergeTwoPricesWithDifferentValues(List<Price> resultPrices, Price newPrice, Price oldPrice) {
        resultPrices.add(newPrice);
        resultPrices.remove(oldPrice);

        Date beforeBegin = oldPrice.getBegin();
        Date beforeEnd = newPrice.getBegin();
        if (beforeBegin.before(beforeEnd)) {
            resultPrices.add(new Price(oldPrice.getId(), oldPrice.getProductCode(), oldPrice.getNumber(),
                    oldPrice.getDepartment(), beforeBegin, beforeEnd, oldPrice.getValue()));
        }

        Date afterBegin = newPrice.getEnd();
        Date afterEnd = oldPrice.getEnd();
        if (afterBegin.before(afterEnd)) {
            resultPrices.add(new Price(oldPrice.getId(), oldPrice.getProductCode(), oldPrice.getNumber(),
                    oldPrice.getDepartment(), afterBegin, afterEnd, oldPrice.getValue()));
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
