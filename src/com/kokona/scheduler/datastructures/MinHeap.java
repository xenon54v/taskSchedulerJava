package com.kokona.scheduler.datastructures;

import com.kokona.scheduler.model.Task;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MinHeap<T> {
    private List<T> heap;
    private Comparator<T> comparator;
    private int size;
    
    // Конструктор с компаратором (основной)
    public MinHeap(Comparator<T> comparator) {
        this.heap = new ArrayList<>();
        this.comparator = comparator;
        this.size = 0;
    }
    
    // Конструктор для обратной совместимости (только для Task)
    @SuppressWarnings("unchecked")
    public MinHeap() {
        this((Comparator<T>) (Comparator<?>) Comparator.comparingInt(t -> {
            if (t instanceof Task) {
                return ((Task) t).getExecutionTime();
            }
            throw new IllegalArgumentException("MinHeap() без параметров работает только с Task");
        }));
    }
    
    // Вставка элемента
    public void insert(T element) {
        heap.add(element);
        size++;
        heapifyUp(size - 1);
    }
    
    // Извлечение минимального элемента
    public T extractMin() {
        if (size == 0) {
            return null;
        }
        
        T min = heap.get(0);
        if (size == 1) {
            heap.clear();
            size = 0;
        } else {
            heap.set(0, heap.get(size - 1));
            heap.remove(size - 1);
            size--;
            heapifyDown(0);
        }
        return min;
    }
    
    // Просмотр минимального элемента без извлечения
    public T peekMin() {
        if (size == 0) {
            return null;
        }
        return heap.get(0);
    }
    
    // Проверка пустоты
    public boolean isEmpty() {
        return size == 0;
    }
    
    // Размер кучи
    public int getSize() {
        return size;
    }
    
    // Очистка кучи
    public void clear() {
        heap.clear();
        size = 0;
    }
    
    // Получение всех элементов (не нарушая порядок кучи)
    public List<T> getAllElements() {
        return new ArrayList<>(heap);
    }
    
    // Поддержка для копирования кучи
    public MinHeap<T> copy() {
        MinHeap<T> copy = new MinHeap<>(comparator);
        copy.heap = new ArrayList<>(heap);
        copy.size = size;
        return copy;
    }
    
    // Восстановление свойства кучи вверх
    private void heapifyUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            
            if (comparator.compare(heap.get(index), heap.get(parentIndex)) >= 0) {
                break;
            }
            
            swap(index, parentIndex);
            index = parentIndex;
        }
    }
    
    // Восстановление свойства кучи вниз
    private void heapifyDown(int index) {
        while (true) {
            int leftChild = 2 * index + 1;
            int rightChild = 2 * index + 2;
            int smallest = index;
            
            if (leftChild < size && 
                comparator.compare(heap.get(leftChild), heap.get(smallest)) < 0) {
                smallest = leftChild;
            }
            
            if (rightChild < size && 
                comparator.compare(heap.get(rightChild), heap.get(smallest)) < 0) {
                smallest = rightChild;
            }
            
            if (smallest == index) {
                break;
            }
            
            swap(index, smallest);
            index = smallest;
        }
    }
    
    // Обмен элементов
    private void swap(int i, int j) {
        T temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
    
    // Вывод содержимого кучи (для отладки)
    public void printHeap() {
        System.out.print("Heap (" + size + " elements): ");
        for (int i = 0; i < size; i++) {
            System.out.print(heap.get(i) + " ");
        }
        System.out.println();
    }
}