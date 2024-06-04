/*
 * Copyright 2019 Maksim Zheravin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.matchingorder.orderbook;

import com.matchingorder.common.MatcherTradeEvent;
import com.matchingorder.common.Order;
import com.matchingorder.utils.SerializationUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;

import java.util.*;
import java.util.function.Consumer;

@Slf4j
@ToString
public final class OrdersBucketNaive implements Comparable<OrdersBucketNaive>, WriteBytesMarshallable {

    @Getter
    private final long price;

    private final LinkedHashMap<Long, Order> entries;

    @Getter
    private long totalVolume;

    public OrdersBucketNaive(final long price) {
        this.price = price;
        this.entries = new LinkedHashMap<>();
        this.totalVolume = 0;
    }

    public OrdersBucketNaive(BytesIn bytes) {
        this.price = bytes.readLong();
        this.entries = SerializationUtils.readLongMap(bytes, LinkedHashMap::new, Order::new);
        this.totalVolume = bytes.readLong();
    }

    /**
     * Put a new order into bucket
     *
     * @param order - order
     */
    public void put(Order order) {
        entries.put(order.orderId, order);
        totalVolume += order.size - order.filled;
    }

    /**
     * Remove order from the bucket
     *
     * @param orderId - order id
     * @param uid     - order uid
     * @return order if removed, or null if not found
     */
    public Order remove(long orderId, long uid) {
        Order order = entries.get(orderId);
//        log.debug("removing order: {}", order);
        if (order == null || order.uid != uid) {
            return null;
        }

        entries.remove(orderId);

        totalVolume -= order.size - order.filled;
        return order;
    }

    /**
     * Get number of orders in the bucket
     *
     * @return number of orders in the bucket
     */
    public int getNumOrders() {
        return entries.size();
    }

    public Order findOrder(long orderId) {
        return entries.get(orderId);
    }

    /**
     * Inefficient method - for testing only
     *
     * @return new array with references to orders, preserving execution queue order
     */
    public List<Order> getAllOrders() {
        return new ArrayList<>(entries.values());
    }


    /**
     * execute some action for each order (preserving execution queue order)
     *
     * @param consumer action consumer function
     */
    public void forEachOrder(Consumer<Order> consumer) {
        entries.values().forEach(consumer);
    }

    @Override
    public void writeMarshallable(BytesOut bytes) {
        bytes.writeLong(price);
        SerializationUtils.marshallLongMap(entries, bytes);
        bytes.writeLong(totalVolume);
    }

    @Override
    public int compareTo(OrdersBucketNaive other) {
        return Long.compare(this.getPrice(), other.getPrice());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                price,
                Arrays.hashCode(entries.values().toArray(new Order[0])));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (!(o instanceof OrdersBucketNaive)) return false;
        OrdersBucketNaive other = (OrdersBucketNaive) o;
        return price == other.getPrice()
                && getAllOrders().equals(other.getAllOrders());
    }

    @AllArgsConstructor
    public final class MatcherResult {
        public MatcherTradeEvent eventsChainHead;
        public MatcherTradeEvent eventsChainTail;
        public long volume;
        public List<Long> ordersToRemove;
    }

}
