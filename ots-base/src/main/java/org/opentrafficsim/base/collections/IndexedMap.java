package org.opentrafficsim.base.collections;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;

import org.djutils.exceptions.Throw;

/**
 * Map implementation that uses an internal array of values. Values are stored and found through the fixed and unique index that
 * each instance of the key supplies. Indices may be negative, in which case an offset will be applied to all indices. This
 * implementation does not support {@code null} keys.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class IndexedMap<K extends Indexed, V> extends AbstractMap<K, V> implements SortedKeySetMap<K, V>
{

    /** Initial size. */
    private static final int INIT_SIZE = 5;

    /** Entries. */
    private Map.Entry<K, V>[] entries;

    /** Index offset. */
    private int baseIndex;

    /** Size. */
    private int size;

    /** Entry set view. */
    private transient Set<Map.Entry<K, V>> entrySet;

    /** Key set view. */
    private transient SortedSet<K> keySet;

    /** Values view. */
    private transient Collection<V> values;

    /**
     * Constructor.
     */
    public IndexedMap()
    {
        this(INIT_SIZE);
    }

    /**
     * Constructor.
     * @param initialSize initial size
     */
    @SuppressWarnings("unchecked")
    public IndexedMap(final int initialSize)
    {
        this.entries = (Map.Entry<K, V>[]) new Map.Entry<?, ?>[initialSize];
    }

    /**
     * Constructor.
     * @param map map that is copied in to this map
     */
    public IndexedMap(final Map<K, V> map)
    {
        this(map, INIT_SIZE);
    }

    /**
     * Constructor.
     * @param map map that is copied in to this map
     * @param initialSize initial size
     */
    @SuppressWarnings("unchecked")
    public IndexedMap(final Map<K, V> map, final int initialSize)
    {
        this.entries = (Map.Entry<K, V>[]) new Map.Entry<?, ?>[map.size() > initialSize ? map.size() : initialSize];
        for (Entry<K, V> entry : map.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Ensures capacity for index.
     * @param index index
     */
    @SuppressWarnings("unchecked")
    protected final void ensureCapacity(final int index)
    {
        int requiredBefore = Math.max(0, this.baseIndex - index);
        int requiredAfter = Math.max(0, index - this.baseIndex - this.entries.length + 1);
        if (requiredBefore == 0 && requiredAfter == 0)
        {
            return;
        }
        int requiredLength = this.entries.length + requiredBefore + requiredAfter;
        int newLength = Math.max(requiredLength, this.entries.length + this.entries.length / 2);
        Map.Entry<K, V>[] newEntries = (Map.Entry<K, V>[]) new Map.Entry<?, ?>[newLength];
        int extra = newLength - requiredLength;
        // existing array at end, start or middle of new array depending on where the growth was required
        int extraBefore =
                requiredBefore > 0 && requiredAfter == 0 ? extra : requiredBefore == 0 && requiredAfter > 0 ? 0 : extra / 2;
        int copyOffset = requiredBefore + extraBefore;
        int newBaseIndex = this.baseIndex - copyOffset;
        System.arraycopy(this.entries, 0, newEntries, copyOffset, this.entries.length);
        this.entries = newEntries;
        this.baseIndex = newBaseIndex;
    }

    @Override
    public int size()
    {
        return this.size;
    }

    @Override
    public boolean containsKey(final Object key)
    {
        if (!(key instanceof Indexed indexed))
        {
            return false;
        }
        int index = indexed.index() - this.baseIndex;
        if (index < 0 || index >= this.entries.length)
        {
            return false;
        }
        Entry<K, V> entry = this.entries[index];
        return entry != null && entry.getKey().equals(key);
    }

    @Override
    public V get(final Object key)
    {
        if (key instanceof Indexed indexed)
        {
            int index = indexed.index() - this.baseIndex;
            if (index < 0 || index >= this.entries.length)
            {
                return null;
            }
            Entry<K, V> entry = this.entries[index];

            if (entry != null)
            {
                K storedKey = entry.getKey();
                if (storedKey == key || storedKey.equals(key))
                {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public V put(final K key, final V value)
    {
        Throw.whenNull(key, "key");
        ensureCapacity(key.index());
        int index = key.index() - this.baseIndex;
        Entry<K, V> old = this.entries[index];
        this.entries[index] = new AbstractMap.SimpleEntry<>(key, value);
        if (old == null)
        {
            this.size++;
            return null;
        }
        return old.getValue();
    }

    @Override
    public V remove(final Object key)
    {
        Throw.whenNull(key, "key");
        if (key instanceof Indexed indexed)
        {
            int index = indexed.index() - this.baseIndex;
            if (index < 0 || index >= this.entries.length)
            {
                return null;
            }
            Entry<K, V> entry = this.entries[index];
            if (entry != null && entry.getKey().equals(key))
            {
                this.entries[index] = null;
                this.size--;
                return entry.getValue();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void clear()
    {
        this.entries = (Map.Entry<K, V>[]) new Map.Entry<?, ?>[INIT_SIZE];
        this.size = 0;
        this.baseIndex = 0;
    }

    @Override
    public SortedSet<K> keySet()
    {
        if (this.keySet == null)
        {
            this.keySet = new KeySetView<>(this);
        }
        return this.keySet;
    }

    @Override
    public Collection<V> values()
    {
        if (this.values == null)
        {
            this.values = new AbstractCollection<>()
            {
                @Override
                public int size()
                {
                    return IndexedMap.this.size;
                }

                @Override
                public Iterator<V> iterator()
                {
                    // must use entrySet() as this method is values()
                    Iterator<Map.Entry<K, V>> it = IndexedMap.this.entrySet().iterator();
                    return new Iterator<>()
                    {
                        @Override
                        public boolean hasNext()
                        {
                            return it.hasNext();
                        }

                        @Override
                        public V next()
                        {
                            return it.next().getValue();
                        }

                        @Override
                        public void remove()
                        {
                            it.remove();
                        }
                    };
                }

                @Override
                public void clear()
                {
                    IndexedMap.this.clear();
                }
            };
        }
        return this.values;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet()
    {
        if (this.entrySet == null)
        {
            this.entrySet = new AbstractSet<>()
            {
                @Override
                public int size()
                {
                    return IndexedMap.this.size;
                }

                @Override
                public Iterator<Map.Entry<K, V>> iterator()
                {
                    return new Iterator<>()
                    {
                        private int index = findNext(0);

                        private int lastReturned = -1;

                        private int findNext(final int from)
                        {
                            int i = from;
                            while (i < IndexedMap.this.entries.length && IndexedMap.this.entries[i] == null)
                            {
                                i++;
                            }
                            return i;
                        }

                        @Override
                        public boolean hasNext()
                        {
                            return this.index < IndexedMap.this.entries.length;
                        }

                        @Override
                        public Map.Entry<K, V> next()
                        {
                            Throw.when(!hasNext(), NoSuchElementException.class, "No next element.");
                            this.lastReturned = this.index;
                            Map.Entry<K, V> result = IndexedMap.this.entries[this.index];
                            this.index = findNext(this.index + 1);
                            return result;
                        }

                        @Override
                        public void remove()
                        {
                            Throw.when(this.lastReturned < 0, IllegalStateException.class, "No element to remove.");
                            if (IndexedMap.this.entries[this.lastReturned] != null)
                            {
                                IndexedMap.this.entries[this.lastReturned] = null;
                                IndexedMap.this.size--;
                            }
                            this.lastReturned = -1;
                        }

                    };
                }

                @Override
                public boolean contains(final Object o)
                {
                    if (!(o instanceof Map.Entry<?, ?> e))
                    {
                        return false;
                    }
                    if (!IndexedMap.this.containsKey(e.getKey()))
                    {
                        return false;
                    }
                    return Objects.equals(IndexedMap.this.get(e.getKey()), e.getValue());
                }

                @Override
                public boolean remove(final Object o)
                {
                    if (!(o instanceof Map.Entry<?, ?> e))
                    {
                        return false;
                    }
                    if (!contains(o))
                    {
                        return false;
                    }
                    IndexedMap.this.remove(e.getKey());
                    return true;
                }

                @Override
                public void clear()
                {
                    IndexedMap.this.clear();
                }
            };
        }
        return this.entrySet;
    }

    @Override
    public Comparator<? super K> comparator()
    {
        return null;
    }

    @Override
    public K firstKey()
    {
        Throw.when(this.size == 0, NoSuchElementException.class, "No element.");
        return this.entries[aboveOrAt(0)].getKey();
    }

    @Override
    public K lastKey()
    {
        Throw.when(this.size == 0, NoSuchElementException.class, "No element.");
        return this.entries[belowOrAt(this.entries.length - 1)].getKey();
    }

    /**
     * Returns the first valid entry index at or below the given index.
     * @param index index
     * @return the first valid entry index at or below the given index, or -1 if no such element
     */
    private int belowOrAt(final int index)
    {
        int i = index;
        while (i >= 0 && this.entries[i] == null)
        {
            i--;
        }
        return i;
    }

    /**
     * Returns the first valid entry index at or above the given index.
     * @param index index
     * @return the first valid entry index at or above the given index, or -1 if no such element
     */
    private int aboveOrAt(final int index)
    {
        int i = index;
        while (i < this.entries.length && this.entries[i] == null)
        {
            i++;
        }
        return i >= this.entries.length ? -1 : i;
    }

    @Override
    public IndexedSubMap<K, V> subMap(final K fromKey, final K toKey)
    {
        Throw.whenNull(fromKey, "fromKey");
        Throw.whenNull(toKey, "toKey");
        return new IndexedSubMap<>(this, fromKey.index(), toKey.index());
    }

    @Override
    public IndexedSubMap<K, V> headMap(final K toKey)
    {
        Throw.whenNull(toKey, "toKey");
        return new IndexedSubMap<>(this, null, toKey.index());
    }

    @Override
    public IndexedSubMap<K, V> tailMap(final K fromKey)
    {
        Throw.whenNull(fromKey, "fromKey");
        return new IndexedSubMap<>(this, fromKey.index(), null);
    }

    /**
     * Sorted view on the key set.
     * @param <T> the type of keys maintained by this map
     * @param <U> the type of mapped values
     */
    private static class KeySetView<T, U> extends AbstractSet<T> implements SortedSet<T>
    {

        /** Map of which this is the key set view. */
        private final SortedKeySetMap<T, U> map;

        /**
         * Constructor.
         * @param map map of which this is the key set view
         */
        KeySetView(final SortedKeySetMap<T, U> map)
        {
            this.map = map;
        }

        @Override
        public Comparator<? super T> comparator()
        {
            return this.map.comparator();
        }

        @Override
        public T first()
        {
            return this.map.firstKey();
        }

        @Override
        public T last()
        {
            return this.map.lastKey();
        }

        @Override
        public SortedSet<T> headSet(final T toElement)
        {
            return this.map.headMap(toElement).keySet();
        }

        @Override
        public SortedSet<T> tailSet(final T fromElement)
        {
            return this.map.tailMap(fromElement).keySet();
        }

        @Override
        public SortedSet<T> subSet(final T fromElement, final T toElement)
        {
            return this.map.subMap(fromElement, toElement).keySet();
        }

        @Override
        public int size()
        {
            return this.map.size();
        }

        @Override
        public Iterator<T> iterator()
        {
            // must use entrySet() as this KeySetView is the response to keySet() (i.e. that would give infinite recursion)
            Iterator<Map.Entry<T, U>> it = this.map.entrySet().iterator();
            return new Iterator<>()
            {
                @Override
                public boolean hasNext()
                {
                    return it.hasNext();
                }

                @Override
                public T next()
                {
                    return it.next().getKey();
                }

                @Override
                public void remove()
                {
                    it.remove();
                }
            };
        }

        @Override
        public boolean contains(final Object o)
        {
            return this.map.containsKey(o);
        }

        @Override
        public boolean remove(final Object o)
        {
            return this.map.remove(o) != null;
        }

        @Override
        public void clear()
        {
            this.map.clear();
        }

    }

    /**
     * Sub-map implementation.
     * @param <K> the type of keys maintained by this map
     * @param <V> the type of mapped values
     */
    private static class IndexedSubMap<K extends Indexed, V> extends AbstractMap<K, V> implements SortedKeySetMap<K, V>
    {

        /** Parent map. */
        private final IndexedMap<K, V> parent;

        /** From index. */
        private final Integer fromIndex; // inclusive, null = no lower bound

        /** To index. */
        private final Integer toIndex; // exclusive, null = no upper bound

        /** Key set view. */
        private transient SortedSet<K> keySet;

        /**
         * Constructor.
         * @param parent parent
         * @param fromIndex from index, inclusive, {@code null} mean no lower bound
         * @param toIndex to index, exclusive, {@code null} mean no upper bound
         */
        IndexedSubMap(final IndexedMap<K, V> parent, final Integer fromIndex, final Integer toIndex)
        {
            Throw.when(fromIndex != null && toIndex != null && fromIndex > toIndex, IllegalArgumentException.class,
                    "fromIndex > toIndex");
            this.parent = parent;
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
        }

        /**
         * Check the range of the key.
         * @param key key
         * @return whether the key is in range
         */
        private boolean inRange(final K key)
        {
            Throw.whenNull(key, "key");
            int index = key.index();
            return (this.fromIndex == null || index >= this.fromIndex) && (this.toIndex == null || index < this.toIndex);
        }

        @Override
        public Comparator<? super K> comparator()
        {
            return null;
        }

        @Override
        public int size()
        {
            int count = 0;
            int start = this.fromIndex == null ? 0 : Math.max(0, this.fromIndex - this.parent.baseIndex);
            int end = this.toIndex == null ? this.parent.entries.length
                    : Math.min(this.parent.entries.length, this.toIndex - this.parent.baseIndex);
            for (int i = start; i < end; i++)
            {
                if (this.parent.entries[i] != null)
                {
                    count++;
                }
            }
            return count;
        }

        @Override
        public boolean containsKey(final Object key)
        {
            if (!(key instanceof Indexed))
            {
                return false;
            }
            @SuppressWarnings("unchecked")
            K k = (K) key;
            return inRange(k) && this.parent.containsKey(k);
        }

        @Override
        public V get(final Object key)
        {
            if (!(key instanceof Indexed))
            {
                return null;
            }
            @SuppressWarnings("unchecked")
            K k = (K) key;
            return inRange(k) ? this.parent.get(k) : null;
        }

        @Override
        public V put(final K key, final V value)
        {
            Throw.whenNull(key, "key");
            Throw.when(!inRange(key), IllegalArgumentException.class, "Key outside submap range.");
            return this.parent.put(key, value);
        }

        @Override
        public V remove(final Object key)
        {
            if (!(key instanceof Indexed))
            {
                return null;
            }
            @SuppressWarnings("unchecked")
            K k = (K) key;
            return inRange(k) ? this.parent.remove(k) : null;
        }

        @Override
        public void clear()
        {
            int start = startArrayIndex();
            int end = endArrayIndex();
            for (int i = start; i < end; i++)
            {
                Entry<K, V> entry = this.parent.entries[i];
                if (entry != null)
                {
                    this.parent.size--;
                }
                this.parent.entries[i] = null;
            }
            if (this.parent.size == 0)
            {
                this.parent.baseIndex = 0;
            }
        }

        @Override
        public K firstKey()
        {
            int start = startArrayIndex();
            int end = endArrayIndex();
            for (int i = start; i < end; i++)
            {
                Entry<K, V> entry = this.parent.entries[i];
                if (entry != null)
                {
                    return entry.getKey();
                }
            }
            throw new NoSuchElementException();
        }

        @Override
        public K lastKey()
        {
            int start = startArrayIndex();
            int end = endArrayIndex();
            for (int i = end - 1; i >= start; i--)
            {
                Entry<K, V> entry = this.parent.entries[i];
                if (entry != null)
                {
                    return entry.getKey();
                }
            }
            throw new NoSuchElementException();
        }

        /**
         * Returns the start slot.
         * @return the start slot
         */
        private int startArrayIndex()
        {
            return this.fromIndex == null ? 0 : Math.max(0, this.fromIndex - this.parent.baseIndex);
        }

        /**
         * Returns the end slot.
         * @return the end slot
         */
        private int endArrayIndex()
        {
            return this.toIndex == null ? this.parent.entries.length
                    : Math.min(this.parent.entries.length, this.toIndex - this.parent.baseIndex);
        }

        @Override
        public SortedKeySetMap<K, V> subMap(final K fromKey, final K toKey)
        {
            Throw.whenNull(fromKey, "fromKey");
            Throw.whenNull(toKey, "toKey");
            return new IndexedSubMap<>(this.parent, startKeyIndex(fromKey.index()), endKeyIndex(toKey.index()));
        }

        @Override
        public SortedKeySetMap<K, V> headMap(final K toKey)
        {
            Throw.whenNull(toKey, "toKey");
            return new IndexedSubMap<>(this.parent, this.fromIndex, endKeyIndex(toKey.index()));
        }

        @Override
        public SortedKeySetMap<K, V> tailMap(final K fromKey)
        {
            Throw.whenNull(fromKey, "fromKey");
            return new IndexedSubMap<>(this.parent, startKeyIndex(fromKey.index()), this.toIndex);
        }

        /**
         * Returns the start index for a new lower bound.
         * @param index bound index
         * @return the start index
         */
        private int startKeyIndex(final int index)
        {
            return this.fromIndex == null ? index : Math.max(this.fromIndex, index);
        }

        /**
         * Returns the end index for a new upper bound.
         * @param index bound index
         * @return the end index
         */
        private int endKeyIndex(final int index)
        {
            return this.toIndex == null ? index : Math.min(this.toIndex, index);
        }

        @Override
        public SortedSet<K> keySet()
        {
            if (this.keySet == null)
            {
                this.keySet = new KeySetView<>(this);
            }
            return this.keySet;
        }

        @Override
        public Set<Map.Entry<K, V>> entrySet()
        {
            return new AbstractSet<>()
            {
                @Override
                public int size()
                {
                    return IndexedSubMap.this.size();
                }

                @Override
                public Iterator<Map.Entry<K, V>> iterator()
                {
                    return new Iterator<>()
                    {
                        private int index = findNext(IndexedSubMap.this.fromIndex == null ? 0
                                : Math.max(0, IndexedSubMap.this.fromIndex - IndexedSubMap.this.parent.baseIndex));

                        private int lastReturned = -1;

                        private int findNext(final int start)
                        {
                            int end = IndexedSubMap.this.toIndex == null ? IndexedSubMap.this.parent.entries.length
                                    : Math.min(IndexedSubMap.this.parent.entries.length,
                                            IndexedSubMap.this.toIndex - IndexedSubMap.this.parent.baseIndex);
                            for (int i = start; i < end; i++)
                            {
                                if (IndexedSubMap.this.parent.entries[i] != null)
                                {
                                    return i;
                                }
                            }
                            return end;
                        }

                        @Override
                        public boolean hasNext()
                        {
                            return this.index < (IndexedSubMap.this.toIndex == null ? IndexedSubMap.this.parent.entries.length
                                    : Math.min(IndexedSubMap.this.parent.entries.length,
                                            IndexedSubMap.this.toIndex - IndexedSubMap.this.parent.baseIndex));
                        }

                        @Override
                        public Map.Entry<K, V> next()
                        {
                            if (!hasNext())
                            {
                                throw new NoSuchElementException();
                            }
                            this.lastReturned = this.index;
                            Map.Entry<K, V> result = IndexedSubMap.this.parent.entries[this.index];
                            this.index = findNext(this.index + 1);
                            return result;
                        }

                        @Override
                        public void remove()
                        {
                            if (this.lastReturned < 0)
                            {
                                throw new IllegalStateException();
                            }
                            IndexedSubMap.this.parent.remove(IndexedSubMap.this.parent.entries[this.lastReturned].getKey());
                            this.lastReturned = -1;
                        }
                    };
                }

                @Override
                public boolean contains(final Object o)
                {
                    if (!(o instanceof Map.Entry<?, ?> e))
                    {
                        return false;
                    }
                    if (!IndexedSubMap.this.containsKey(e.getKey()))
                    {
                        return false;
                    }
                    return Objects.equals(IndexedSubMap.this.get(e.getKey()), e.getValue());
                }

                @Override
                public boolean remove(final Object o)
                {
                    if (!(o instanceof Map.Entry<?, ?> e))
                    {
                        return false;
                    }
                    if (!contains(o))
                    {
                        return false;
                    }
                    IndexedSubMap.this.remove(e.getKey());
                    return true;
                }

                @Override
                public void clear()
                {
                    IndexedSubMap.this.clear();
                }
            };
        }

    }

}
