package com.kokona.scheduler.datastructures;

import com.kokona.scheduler.model.Task;
import java.util.ArrayList;
import java.util.List;

public class MinHeap {
	private List<Task> heap;
	private int size;
	
	public MinHeap() {
		this.heap = new ArrayList<>();
		this.size = 0;
	}
	
	public MinHeap(int initialCapacity) {
		this.heap = new ArrayList<>(initialCapacity);
		this.size = 0;
	}
	
	// плюс задачу в кучу
	
	public void insert(Task task) {
        // добавляем элемент в конец
        heap.add(task);
        size++;
        
        // восстанавливаем свойства кучи "вверх"
        heapifyUp(size - 1);
    }
    
    // извлекаем задачу с наименьшим временем
    public Task extractMin() {
        if (size == 0) {
            throw new IllegalStateException("Heap is empty");
        }
        
        Task min = heap.get(0);
        // перемещаем последний элемент в корень
        heap.set(0, heap.get(size - 1));
        heap.remove(size - 1); // удаляем последний элемент
        size--;
        
        if (size > 0) {
            heapifyDown(0); // восстанавливаем св-во кучи "вниз"
        }
        return min;
    }
    
    // проверка пуста ли куча
    public boolean isEmpty() {
        return size == 0;
    }
    
    // текущее кол-во элементов в куче
    public int getSize() {
        return size;
    }
    
    // поднимаем элемент вверх
    private void heapifyUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2; // индекс родителя
            
            // если элемент уже больше или равен родителю - остановка
            if (heap.get(index).getExecutionTime() >= heap.get(parentIndex).getExecutionTime()) {
                break;
            }
            
            // меняем местами с родителем
            swap(index, parentIndex);
            index = parentIndex; // переходим к родителю
        }
    }
    
    // опускаем элемент вниз
    private void heapifyDown(int index) {
        while (true) {
            int leftChild = 2 * index + 1;  // левый потомок
            int rightChild = 2 * index + 2; // правый потомок
            int smallest = index;           // предполагаем, что текущий элемент наименьший
            
            // сравниваем с левым потомком
            if (leftChild < size && 
                heap.get(leftChild).getExecutionTime() < heap.get(smallest).getExecutionTime()) {
                smallest = leftChild;
            }
            
            // сравниваем с правым потомком
            if (rightChild < size && 
                heap.get(rightChild).getExecutionTime() < heap.get(smallest).getExecutionTime()) {
                smallest = rightChild;
            }
            
            // если наименьший элемент - текущий, останавливаемся
            if (smallest == index) {
                break;
            }
            
            // меняем местами с наименьшим потомком
            swap(index, smallest);
            index = smallest; // переходим к потомку
        }
    }
    
    // свэпаем два элемента в куче
    private void swap(int i, int j) {
        Task temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
    
    // выводит содержимое кучи
    public void printHeap() {
        System.out.print("Heap: ");
        for (int i = 0; i < size; i++) {
            System.out.print(heap.get(i).getId() + "(" + heap.get(i).getExecutionTime() + ") ");
        }
        System.out.println();
    }
}
