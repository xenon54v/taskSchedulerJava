package com.kokona.scheduler.datastructures;

import com.kokona.scheduler.model.Task;

public class MinHeap {
	private Task[] heap;
	private int size;
	private final int capacity;
	
	public MinHeap(int capacity) {
		this.capacity = capacity;
		this.heap = new Task[capacity];
		this.size = 0;
	}
	
	// плюс задачу в кучу
	
	public void insert(Task task) {
        if (size == capacity) {
            throw new IllegalStateException("Heap is full");
        }
        
     // добавляем элемент в конец
        heap[size] = task;
        
        // восстанавливаем свойства кучи "вверх"
        heapifyUp(size);
        size++;
	}
	
	// извлекаем задачу с наименьшим временем
	
	public Task extractMin() {
        if (size == 0) {
            throw new IllegalStateException("Heap is empty");
        }
        
        Task min = heap[0];
        heap[0] = heap[size - 1];
        size--;
        
        heapifyDown(0); // восстанавливаем св-во кучи "вниз"
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
            if (heap[index].getExecutionTime() >= heap[parentIndex].getExecutionTime()) {
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
                heap[leftChild].getExecutionTime() < heap[smallest].getExecutionTime()) {
                smallest = leftChild;
            }
            
            // сравниваем с правым потомком
            if (rightChild < size && 
                heap[rightChild].getExecutionTime() < heap[smallest].getExecutionTime()) {
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
        Task temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }
	
	// выводит содержимое кучи
	public void printHeap() {
        System.out.print("Heap: ");
        for (int i = 0; i < size; i++) {
            System.out.print(heap[i].getId() + "(" + heap[i].getExecutionTime() + ") ");
        }
        System.out.println();
    }
	
}
