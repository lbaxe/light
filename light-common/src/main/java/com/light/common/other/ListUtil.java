package com.light.common.other;

import java.util.*;
import java.util.stream.Collectors;

public class ListUtil {
    /**
     * 1、step<=0 return null 2、list empty return null
     * 
     * @param list 需要分页的list
     * @param step 分页的步长
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> pageList(List<T> list, int step) {
        if (step <= 0) {
            return null;
        }
        if (list == null || list.isEmpty()) {
            return null;
        }
        List<List<T>> lists = new ArrayList<>();
        int size = list.size();
        if (size > step) {
            int page = size % step != 0 ? (size / step + 1) : size / step;
            for (int i = 0; i < page; i++) {
                int end = (i + 1) * step;
                lists.add(list.subList(i * step, end > size ? size : end));
            }
        } else {
            lists.add(list);
        }
        return lists;
    }

    public static <T> PageListIterator pageListIterator(Collection<T> collection, int step) {
        return new PageListIterator(collection, step);
    }

    public static <K, V> PageMapIterator pageMapIterator(Map<K, V> map, int step) {
        return new PageMapIterator(map, step);
    }

    /**
     * 1、step<=0 return null 2、list empty return null
     *
     * @param list 需要分页的list
     * @param step 分页的步长
     * @return java.util.List<java.util.List < T>>
     * @throws BizException
     * @createTime 2020/02/24 0024
     * @author Luban
     */
    public static <K, V> List<Map<K, V>> pageList(Map<K, V> map, int step) {
        if (step <= 0) {
            return null;
        }
        if (map == null || map.isEmpty()) {
            return null;
        }
        List<Map<K, V>> lists = new ArrayList<>();
        int size = map.size();
        Set<Map.Entry<K, V>> entrySet = map.entrySet();
        int page = 1;
        Map<K, V> subMap = new HashMap<>();
        lists.add(subMap);
        for (Map.Entry<K, V> entry : entrySet) {
            if (page * step != size && subMap.size() == step) {
                subMap = new HashMap<>();
                lists.add(subMap);
                page++;
            }
            subMap.put(entry.getKey(), entry.getValue());
        }

        return lists;
    }

    public static class PageListIterator<T> {
        /**
         * 总页数
         */
        private int totalPage = 0;

        /**
         * index of next page to return
         */
        private int nextPage = 0;

        /**
         * 每页的大小
         */
        private int pageSize = 0;

        /**
         * 每页默认大小
         */
        private static final int DEFAULT_PAGE_SIZE = 100;

        private Collection<T> collection = null;

        public PageListIterator(Collection<T> collection) {
            this(collection, DEFAULT_PAGE_SIZE);
        }

        public PageListIterator(Collection<T> collection, int pageSize) {
            if (pageSize <= 0) {
                throw new IllegalArgumentException("Paging size must be greater than zero.");
            }
            if (null == collection) {
                throw new NullPointerException("Paging resource list must be not null.");
            }
            this.pageSize = pageSize;
            this.collection = Collections.unmodifiableCollection(collection);
            this.totalPage =
                collection.size() % pageSize != 0 ? (collection.size() / pageSize + 1) : collection.size() / pageSize;
        }

        /**
         * 返回是否还有下一页数据
         *
         * @return
         */
        public boolean hasNext() {
            return nextPage != totalPage;
        }

        public List<T> next() {
            if (nextPage >= totalPage) {
                throw new NoSuchElementException();
            }
            int i = nextPage;
            nextPage = nextPage + 1;
            return collection.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList());
        }

        /**
         * 获取分页后，总的页数
         *
         * @return
         */
        public int getTotalPage() {
            return totalPage;
        }

        /**
         * 返回当前剩余页数
         *
         * @return
         */
        public int getSurplusPage() {
            return totalPage - nextPage;
        }
    }

    public static class PageMapIterator<K, V> {
        /**
         * 总页数
         */
        private int totalPage = 0;

        /**
         * index of next page to return
         */
        private int nextPage = 0;

        /**
         * 每页的大小
         */
        private int pageSize = 0;

        /**
         * 每页默认大小
         */
        private static final int DEFAULT_PAGE_SIZE = 100;

        private Map<K, V> map = null;

        private Set<Map.Entry<K, V>> entrySet = null;

        public PageMapIterator(Map<K, V> map) {
            this(map, DEFAULT_PAGE_SIZE);
        }

        public PageMapIterator(Map<K, V> map, int pageSize) {
            if (pageSize <= 0) {
                throw new IllegalArgumentException("Paging size must be greater than zero.");
            }
            if (null == map) {
                throw new NullPointerException("Paging resource list must be not null.");
            }
            this.pageSize = pageSize;
            this.map = Collections.unmodifiableMap(map);
            this.entrySet = Collections.unmodifiableSet(map.entrySet());
            this.totalPage = map.size() % pageSize != 0 ? (map.size() / pageSize + 1) : map.size() / pageSize;
        }

        /**
         * 返回是否还有下一页数据
         *
         * @return
         */
        public boolean hasNext() {
            return nextPage != totalPage;
        }

        public Map<K, V> next() {
            if (nextPage >= totalPage) {
                throw new NoSuchElementException();
            }
            int i = nextPage;
            nextPage = nextPage + 1;
            return entrySet.stream().skip(i * pageSize).limit(pageSize)
                .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue(), (k1, k2) -> k2));
        }

        /**
         * 获取分页后，总的页数
         *
         * @return
         */
        public int getTotalPage() {
            return totalPage;
        }

        /**
         * 返回当前剩余页数
         *
         * @return
         */
        public int getSurplusPage() {
            return totalPage - nextPage;
        }
    }
}
