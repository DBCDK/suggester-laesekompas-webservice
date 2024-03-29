package dk.dbc.laesekompas.suggester.webservice.solr;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.solr.common.util.NamedList;

public interface SolrReader<T> {

    T get();

    interface ObjectReader<T> extends SolrReader<T> {

        <R> SolrReader<R> as(Class<R> clazz);

        long asLong();

        MapReader asMap();

        ListReader asList();
    }

    interface MapReader extends SolrReader<Object> {

        ObjectReader<Object> get(String name);

        MapReader take(String name, Consumer<ObjectReader<Object>> consumer);

        MapReader forEach(BiConsumer<String, ObjectReader<Object>> consumer);
    }

    interface ListReader extends SolrReader<Object> {

        ListReader forEach(Consumer<ObjectReader<Object>> consumer);
    }

    static <R> ObjectReader<R> of(R r) {
        return new O<>(r);
    }

    class O<T> implements ObjectReader<T> {

        private final T t;

        private O(T t) {
            this.t = t;
        }

        @Override
        public <R> SolrReader<R> as(Class<R> clazz) {
            if (t == null) {
                throw new IllegalStateException("Cannot convert null to " + clazz.getName());
            }
            if (clazz.isAssignableFrom(t.getClass())) {
                return (SolrReader<R>) this;
            }
            throw new IllegalStateException("Cannot convert " + t.getClass().getName() + " to a " + clazz.getName());
        }

        @Override
        public T get() {
            return t;
        }

        @Override
        public long asLong() {
            if (t instanceof Long)
                return (Long) t;
            if (t instanceof Integer)
                return (Integer) t;
            if (t instanceof Short)
                return (Short) t;
            throw new IllegalStateException("Cannot convert " + t.getClass().getName() + " to a long");
        }

        @Override
        public MapReader asMap() {
            if (t == null) {
                throw new IllegalStateException("Cannot convert null to map");
            }
            if (t instanceof NamedList) {
                return new M(( (NamedList<Object>) t ).asMap());
            }
            if (t instanceof Map) {
                return new M((Map<String, Object>) t);
            }
            throw new IllegalStateException("Cannot convert " + t.getClass().getName() + " to a map");
        }

        @Override
        public ListReader asList() {
            if (t == null) {
                throw new IllegalStateException("Cannot convert null to map");
            }
            if (t instanceof List) {
                return new L((List<Object>) t);
            }
            throw new IllegalStateException("Cannot convert " + t.getClass().getName() + " to a list");
        }

    }

    class L implements ListReader {

        private final List<Object> l;

        private L(List<Object> l) {
            this.l = l;
        }

        @Override
        public Object get() {
            return l;
        }

        @Override
        public ListReader forEach(Consumer<ObjectReader<Object>> consumer) {
            l.forEach(val -> consumer.accept(new O<>(val)));
            return this;
        }
    }

    class M implements MapReader {

        private final Map<String, Object> m;

        private M(Map<String, Object> m) {
            this.m = m;
        }

        @Override
        public ObjectReader<Object> get(String name) {
            return new O<>(m.get(name));
        }

        @Override
        public Object get() {
            return m;
        }

        @Override
        public MapReader take(String name, Consumer<ObjectReader<Object>> consumer) {
            Object val = m.get(name);
            if (val != null) {
                consumer.accept(new O<>(val));
            }
            return this;
        }

        @Override
        public MapReader forEach(BiConsumer<String, ObjectReader<Object>> consumer) {
            m.forEach((k, v) -> consumer.accept(k, new O(v)));
            return this;
        }
    }
}
